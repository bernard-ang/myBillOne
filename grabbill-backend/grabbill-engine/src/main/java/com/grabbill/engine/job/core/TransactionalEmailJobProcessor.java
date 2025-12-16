package com.grabbill.engine.job.core;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.event.DsnScanJobEvent;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponseComponent;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.service.*;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.core.utils.SmsUtils;
import com.grabbill.engine.job.JobProcessingException;
import com.grabbill.engine.service.RFCPatterns;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class TransactionalEmailJobProcessor extends AbstractJobProcessor {

    @Value("${transactional-email.batch-size}")
    private int BATCH_SIZE;

    @Value("${transactional-email.batch-send-interval}")
    private int BATCH_SEND_INTERVAL;

    @Value("${unsubscribe-email-link.url}")
    private String UNSUBSCRIBE_EMAIL_LINK_URL;

    @Value("${open-email-link.url}")
    private String EMAIL_OPENED_LINK_URL;

    @Value("${embedded-link-click.url}")
    private String URL_LINK_CLICKED_URL;

    @Value("${file.external.url}")
    private String FILE_EXTERNAL_URL;

    @Autowired
    @Qualifier("transactionalEmailTypeService")
    private BaseTypeService<TransactionalEmailType, TransactionalEmailActivity> txetService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("transactionalEmailIndexRowService")
    private BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> txeirService;

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService txerService;

    @Autowired
    private WhatsAppService whatsAppService;


    @Override
    void processInternal(final Job targetJob) throws JobProcessingException {
        // mark activity as processing
        TransactionalEmailActivity targetActivity = txeaService.markAsProcessing(targetJob.getActivityId());
        TransactionalEmailType targetType = targetActivity.getTransactionalEmailType();
        logProcessing(targetJob, targetType.getId());


        targetActivity = txeaService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getTransactionalEmailType();
        Account account = targetType.getAccount();

        Optional<RefreshTemplateResponse> template = Optional.empty();
        WhatsAppSession session;

        if (targetActivity.getSendWhatsAppMessage()) {
            session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            String whatsappTemplateName = targetType.getWhatsAppTemplateName();
            List<RefreshTemplateResponse> templates = session.refreshTemplate();
            template = templates.stream().filter(item -> item.getName().equalsIgnoreCase(whatsappTemplateName)).findAny();

            if (template.isPresent()) {
                RefreshTemplateResponse currentTemplate = template.get();
                targetActivity.setWhatsAppTemplateName(currentTemplate.getName());
                targetActivity.setWhatsAppBodyContent(currentTemplate.getBodyText());
                targetActivity.setWhatsAppDocument(currentTemplate.getHeaderComponent().isPresent());
                targetActivity.setWhatsAppFooterContent(currentTemplate.getFooterText());
                targetActivity.setWhatsAppButton(currentTemplate.getButtonText());
            }
        }

        Map<Long, Exception> indexRowIdToExceptionMap;
        Optional<MailServer> mailServerOptional = mailServerService.getByAccount(account);
        if (mailServerOptional.isPresent()) {
            MailServer mailServer = mailServerOptional.get();
            if (!mailServer.isCustomServer()) {
                indexRowIdToExceptionMap = processIndexRowsInternal(targetType, targetActivity, smtpService);

            } else {
                SmtpService customSmtpService = CustomSmtpService.getInstance(
                        mailServer.getSmtpHost(),
                        mailServer.getSmtpPort(),
                        mailServer.getSmtpEncryption(),
                        mailServer.getSmtpUsername(),
                        mailServer.getSmtpPassword(),
                        this.messageIdGenerator
                );
                indexRowIdToExceptionMap = processIndexRowsInternal(targetType, targetActivity, customSmtpService);
            }

        } else {
            indexRowIdToExceptionMap = processIndexRowsInternal(targetType, targetActivity, smtpService);
        }


        // mark activity as processed completely
        targetActivity.setStatus(ProcessStatus.COMPLETED);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        OffsetDateTime expectedPurgeTimestamp = targetType.isAutoPurge() ?
                targetJob.getCreatedTimestamp().plusDays(targetType.getAutoPurgeByDays()) : null;
        targetActivity.setExpectedPurgedTimestamp(expectedPurgeTimestamp);
        TransactionalEmailActivity updatedActivity = txeaService.save(targetActivity);


        // update activity statistics
        int sent = 0;
        int bounced = 0;
        int bouncedSkip = 0;
        int unsubscribedSkip = 0;
        for (TransactionalEmailIndexRow indexRow : updatedActivity.getTransactionalEmailIndexRows()) {

            TransactionalEmailRecord record = indexRow.getTransactionalEmailRecord();

            // update record(s) with sent error
            if (!indexRowIdToExceptionMap.isEmpty() && indexRowIdToExceptionMap.containsKey(indexRow.getId())) {
                Exception exception = indexRowIdToExceptionMap.get(indexRow.getId());
                record.setMessage((exception.getMessage().length() < 255) ?
                        exception.getMessage() : exception.getMessage().substring(0, 255));
                record.setStatus(ProcessStatus.ERROR);

                if (template.isPresent()) {
                    // construct email parameter map, perform value interpolation on email content
                    Map<String, String> parameterMap = indexRowHelper.buildTransactionalEmailParameterMap(indexRow, targetType.getTransactionalEmailIndexFields());
                    Map<String, TransactionalEmailFile> fileMap = targetActivity.getTransactionalEmailFiles().stream()
                            .collect(Collectors.toMap(TransactionalEmailFile::getName, Function.identity()));

                    processWhatsappMessage(record.getTransactionalEmailIndexRow(), record, targetActivity, targetType, parameterMap, fileMap, template.get());
                }

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                String message = stringWriter.toString();
                log.warn(message);

                String statusCode = null;
                Matcher rfc1893Matcher = RFCPatterns.RFC1893_PATTERN.matcher(message);
                if (rfc1893Matcher.find()) {
                    statusCode = rfc1893Matcher.group(rfc1893Matcher.groupCount());

                } else {
                    Matcher rfc2821Matcher = RFCPatterns.RFC2821_PATTERN.matcher(message);
                    if (rfc2821Matcher.find()) {
                        statusCode = rfc2821Matcher.group(rfc2821Matcher.groupCount());
                    }
                }

                if (statusCode != null) {
                    if (statusCode.startsWith("5")) {
                        record.setEmailStatusHardBounce(true);
                        boolean hasBouncedEmail = bouncedEmailService.hasBouncedEmail(
                                targetJob.getAccount().getId(),
                                indexRow.getText1()
                        );

                        if (!hasBouncedEmail) {
                            BouncedEmail bouncedEmail = new BouncedEmail();
                            bouncedEmail.setDomainType(getSupportedActivityType());
                            bouncedEmail.setEmail(indexRow.getText1());
                            bouncedEmail.setTypeId(targetType.getId());
                            bouncedEmail.setTypeName(targetType.getName());
                            bouncedEmail.setActivityId(targetActivity.getId());
                            bouncedEmail.setActivityName(targetActivity.getName());
                            bouncedEmail.setDsnStatusCode(statusCode);
                            bouncedEmail.setReason(exception.getMessage());
                            bouncedEmail.setCreatedBy(CREATED_BY);
                            bouncedEmail.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
                            bouncedEmail.setAccount(account);
                            bouncedEmailService.save(bouncedEmail);
                        }

                    } else if (statusCode.startsWith("4")) {
                        record.setEmailStatusSoftBounce(true);
                    }

                    // unknown failure, marks as soft bounce?
                } else {
                    record.setEmailStatusSoftBounce(true);
                }

                bounced++;
            }

            // accumulate statistical data
            if (ProcessStatus.COMPLETED.equals(record.getStatus())) {
                if (record.isEmailStatusSkipUnsubscribed()) {
                    unsubscribedSkip++;

                } else if (record.isEmailStatusSkipBounced()) {
                    bouncedSkip++;

                } else {
                    sent++;
                }
            }
        }

        // update statistic data
        updatedActivity.setEmailStatusSent(sent);
        updatedActivity.setEmailStatusBounced(bounced);
        updatedActivity.setEmailStatusBouncedSkip(bouncedSkip);
        updatedActivity.setEmailStatusUnsubscribedSkip(unsubscribedSkip);
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processed. Total plan usage is [sent=" + sent
                + ", bounced=" + bounced
                + ", bouncedSkip=" + bouncedSkip
                + ", unsubscribedSkip=" + unsubscribedSkip + "].");

        // if not archive, purge files
        if (!targetType.isArchive()) {
            for (TransactionalEmailIndexRow indexRow : updatedActivity.getTransactionalEmailIndexRows()) {
                indexRow.setTransactionalEmailFile(null);
            }

            for (TransactionalEmailFile targetFile : updatedActivity.getTransactionalEmailFiles()) {
                fileStorageService.delete(
                        account,
                        FileObjectType.TRANSACTIONAL_EMAIL,
                        updatedActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            updatedActivity.getTransactionalEmailFiles().clear();
            updatedActivity = txeaService.saveAndFlush(updatedActivity);

        } else {
            log.info("Total storage used is [" + updatedActivity.getStorageSize() + "] bytes.");
        }


        Integer accountId = targetJob.getAccount().getId();
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(accountId);
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setTotalTransactionalEmailSent(targetUsageStatistic.getTotalTransactionalEmailSent() + updatedActivity.getEmailStatusSent());
            targetUsageStatistic.setTotalStorageUsed(targetUsageStatistic.getTotalStorageUsed() + updatedActivity.getStorageSize());
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + accountId + "].");
        }


        // update type with last sent by and sent date; update all index fields as hard referenced
        TransactionalEmailType updatedType = txetService.getByActivity(updatedActivity);
        updatedType.setLastSentBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastSentDate(updatedActivity.getLastModifiedDate());
        for (TransactionalEmailIndexField targetIndexField : updatedType.getTransactionalEmailIndexFields()) {
            targetIndexField.setHardRef(true);
        }
        txetService.save(updatedType);


        // schedule Delivery Status Notification scan job
        DsnScanJobEvent jobEvent = new DsnScanJobEvent();
        jobEvent.setDomainType(getSupportedActivityType());
        jobEvent.setAccountId(targetJob.getAccount().getId());
        jobEvent.setActivityId(updatedActivity.getId());
        jobEvent.setExpireAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(jobProperties.getDsnScanJobEventLifespan()));
        jobEventPublisher.publish(jobEvent);


        // schedule auto purge job (if applicable)
        scheduleAutoPurgeJobIfSet(
                updatedType,
                updatedType.getId(),
                updatedActivity.getId(),
                updatedActivity.getName(),
                expectedPurgeTimestamp
        );
    }

    private Map<Long, Exception> processIndexRowsInternal(
            final TransactionalEmailType targetType,
            final TransactionalEmailActivity targetActivity,
            final SmtpService targetSmtpService
    ) throws JobProcessingException {

        Account account = targetType.getAccount();
        Map<String, TransactionalEmailFile> fileMap = targetActivity.getTransactionalEmailFiles().stream()
                .collect(Collectors.toMap(TransactionalEmailFile::getName, Function.identity()));
        Map<Long, Exception> indexRowIdToExceptionMap = new HashMap<>();
        List<MimeMessage> batchMessages = new ArrayList<>();
        Map<String, EmbeddedLink> urlToEmbeddedLinkMap = generateUrlToEmbeddedLinkMap(
                DomainType.TRANSACTIONAL_EMAIL, targetType.getId(), targetActivity.getId(), targetActivity.getEmailContent());

        Optional<RefreshTemplateResponse> template = Optional.empty();
        WhatsAppSession session = null;

        if (targetActivity.getSendWhatsAppMessage()) {
            session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            String whatsappTemplateName = targetType.getWhatsAppTemplateName();
            List<RefreshTemplateResponse> templates = session.refreshTemplate();
            template = templates.stream().filter(item -> item.getName().equalsIgnoreCase(whatsappTemplateName)).findAny();

            if (template.isPresent()) {
                RefreshTemplateResponse currentTemplate = template.get();
                targetActivity.setWhatsAppTemplateName(currentTemplate.getName());
                targetActivity.setWhatsAppBodyContent(currentTemplate.getBodyText());
                targetActivity.setWhatsAppDocument(currentTemplate.getHeaderComponent().isPresent());
                targetActivity.setWhatsAppFooterContent(currentTemplate.getFooterText());
                targetActivity.setWhatsAppButton(currentTemplate.getButtonText());
            }
        }

        long storageSize = 0;
        for (TransactionalEmailIndexRow indexRow : targetActivity.getTransactionalEmailIndexRows()) {
            String email = indexRow.getText1();
            String filename = indexRow.getText2();
            String password = indexRow.getText3();

            TransactionalEmailRecord record = indexRow.getTransactionalEmailRecord();

            // IMPORTANT: skipping those index row with record (already processed)
            if (record == null) {
                record = new TransactionalEmailRecord();
                record.setName(email);
                record.setPriority(targetActivity.getPriority());
                record.setTransactionalEmailActivity(targetActivity);
                record.setTransactionalEmailIndexRow(indexRow);

                try {

                    // mark record as "skipped - unsubscribed" if email in unsubscribed email list
                    if (unsubscribedEmailService.isUnsubscribed(
                            account.getId(),
                            email,
                            getSupportedActivityType(),
                            targetType.getId()
                    )) {
                        record.setEmailStatusSkipUnsubscribed(true);


                        // mark record as "skipped - bounced" if email in bounced email list
                    } else if (bouncedEmailService.hasBouncedEmail(
                            account.getId(),
                            email
                    )) {
                        record.setEmailStatusSkipBounced(true);


                        // otherwise, process and send the email
                    } else {

                        MimeMessage message = targetSmtpService.createMimeMessage(indexRow.getId());
                        message.setFrom(new InternetAddress(targetActivity.getEmailFrom(), targetActivity.getEmailFromName(), "UTF-8"));
                        message.setRecipients(Message.RecipientType.TO, email);
                        message.setHeader("Disposition-Notification-To", targetActivity.getEmailFrom());
                        message.setHeader("Return-Receipt-To", targetActivity.getEmailFrom());

                        // construct email parameter map, perform value interpolation on email content
                        Map<String, String> parameterMap = indexRowHelper.buildTransactionalEmailParameterMap(indexRow, targetType.getTransactionalEmailIndexFields());

                        // process email subject
                        Handlebars handlebars = new Handlebars();
                        String rawEmailSubject = targetActivity.getEmailSubject();
                        Template subjectTemplate = handlebars.compileInline(rawEmailSubject);
                        String processedEmailSubject = subjectTemplate.apply(parameterMap);
                        message.setSubject(processedEmailSubject);

                        UnsubscribedEmailLink unsubscribedEmailLink = unsubscribedEmailLinkService.generate(account, targetType, targetActivity, email);
                        String unsubscribeEmailLink = UNSUBSCRIBE_EMAIL_LINK_URL + unsubscribedEmailLink.getLinkId();
                        parameterMap.put("unsubscribe_link", unsubscribeEmailLink);

                        // process email content
                        String rawEmailContent = targetActivity.getEmailContent();
                        Document htmlDocument = Jsoup.parse(rawEmailContent);
                        htmlDocument.select("a").forEach(element -> {
                            if (element.hasAttr("href")) {
                                String hrefAttr = element.attributes().get("href");

                                if (!hrefAttr.equals("{{unsubscribe_link}}") && isValidURL(hrefAttr)) {
                                    EmbeddedLink targetEmbeddedLink = urlToEmbeddedLinkMap.get(hrefAttr);
                                    String urlPath = "trxemail/" + targetActivity.getId()
                                            + "/urls/" + targetEmbeddedLink.getId()
                                            + "/rows/" + indexRow.getId();
                                    String encodedPath = Base64.getEncoder().encodeToString(urlPath.getBytes(StandardCharsets.UTF_8));
                                    element.attr("href", URL_LINK_CLICKED_URL + "/" + encodedPath);
                                }
                            }
                        });
                        String parsedEmailContent = htmlDocument.html();
                        Template contentTemplate = handlebars.compileInline(parsedEmailContent);
                        String processedEmailContent = contentTemplate.apply(parameterMap);
                        record.setEmailContent(processedEmailContent);

                        // append email open link image before close body tag
                        String outboundProcessedEmailContent = processedEmailContent;
                        if (processedEmailContent.contains("</body>")) {
                            String emailOpenedImageTag = "<img id='grb' src='" + EMAIL_OPENED_LINK_URL + "/trxemail/" + indexRow.getId() + "/pixel.png" + "'/></body>";
                            outboundProcessedEmailContent = processedEmailContent.replace("</body>", emailOpenedImageTag);
                        }

                        // process the attachment file
                        if (targetType.isHasAttachment()) {
                            if (fileMap.containsKey(filename)) {

                                TransactionalEmailFile file = fileMap.get(filename);
                                byte[] fileBytes = fileStorageService.download(
                                        targetType.getAccount(),
                                        FileObjectType.TRANSACTIONAL_EMAIL,
                                        targetActivity.getId().toString(),
                                        filename
                                );
                                storageSize += file.getFileSize();

                                if (targetType.isPasswordProtected()) {
                                    try {
                                        AccessPermission accessPermission = new AccessPermission();
                                        StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(
                                                password,
                                                password,
                                                accessPermission
                                        );
                                        protectionPolicy.setEncryptionKeyLength(256);
                                        protectionPolicy.setPermissions(accessPermission);

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        PDDocument document = PDDocument.load(fileBytes);
                                        document.protect(protectionPolicy);
                                        document.save(baos);
                                        fileBytes = baos.toByteArray();
                                        document.close();

                                    } catch (IOException e) {
                                        record.setMessage("Failed to password protect [" + filename + "]: " + e.getMessage());
                                        throw new GrabbillException(record.getMessage(), e);
                                    }
                                }

                                BodyPart messageContent = new MimeBodyPart();
                                messageContent.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");

                                BodyPart messageAttachment = new MimeBodyPart();
                                ByteArrayDataSource dataSource = new ByteArrayDataSource(fileBytes, file.getFileType());
                                messageAttachment.setDataHandler(new DataHandler(dataSource));
                                messageAttachment.setFileName(file.getName());

                                Multipart multipart = new MimeMultipart();
                                multipart.addBodyPart(messageContent);
                                multipart.addBodyPart(messageAttachment);
                                message.setContent(multipart);
                                indexRow.setTransactionalEmailFile(file);

                                // file not found
                            } else {
                                record.setMessage("File [" + filename + "] does not exist!");
                                throw new GrabbillException(record.getMessage());
                            }

                            // no attachment
                        } else {
                            message.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");
                        }

                        batchMessages.add(message);
                        if (batchMessages.size() >= BATCH_SIZE) {
                            batchSend(targetSmtpService, batchMessages, indexRowIdToExceptionMap, BATCH_SEND_INTERVAL);
                        }
                        record.setEmailStatusSent(true);
                    }

                    // done processing record
                    record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    record.setStatus(ProcessStatus.COMPLETED);

                } catch (Exception e) {
                    log.error("Unknown error while processing index row", e);
                    record.setStatus(ProcessStatus.ERROR);
                    record.setMessage((e.getMessage().length() < 255) ?
                            e.getMessage() : e.getMessage().substring(0, 255));

                    if (template.isPresent()) {
                        // construct email parameter map, perform value interpolation on email content
                        Map<String, String> parameterMap = indexRowHelper.buildTransactionalEmailParameterMap(indexRow, targetType.getTransactionalEmailIndexFields());

                        processWhatsappMessage(record.getTransactionalEmailIndexRow(), record, targetActivity, targetType, parameterMap, fileMap, template.get());
                    }
                }


                // link record to index row
                TransactionalEmailRecord savedRecord = txerService.save(record);
                indexRow.setTransactionalEmailType(targetType);
                indexRow.setTransactionalEmailRecord(savedRecord);
                txeirService.save(indexRow);
            }
        }

        if (!batchMessages.isEmpty()) {
            batchSend(targetSmtpService, batchMessages, indexRowIdToExceptionMap, BATCH_SEND_INTERVAL);
        }

        targetActivity.setStorageSize(storageSize);

        return indexRowIdToExceptionMap;
    }

    private TransactionalEmailRecord processWhatsappMessage(
            TransactionalEmailIndexRow indexRow,
            TransactionalEmailRecord record,
            TransactionalEmailActivity targetActivity,
            TransactionalEmailType targetType,
            Map<String, String> parameterMap,
            Map<String, TransactionalEmailFile> fileMap,
            RefreshTemplateResponse template) {

        // TODO: error handling

        if (targetActivity.getSendWhatsAppMessage() && ProcessStatus.ERROR.equals(record.getStatus())) {
            if (template == null) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Whatsapp template not found");
                return record;
            }

            String mobileNo = indexRow.getText4();
            if (!SmsUtils.isValidWhatsAppPhoneNumber(mobileNo)) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Invalid phone number");
                return record;
            }

            // Construct body content
            String bodyText = template.getBodyText();

            if (bodyText == null) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Template body not found");
                return record;
            }

            String finalBodyContent = bodyText;
            List<String> parameters = new ArrayList<>();
            for (TransactionalEmailWhatsappTemplateParam param : targetType.getTransactionalEmailWhatsappTemplateParams()) {
                String paramValue = parameterMap.get(param.getField());
                finalBodyContent = finalBodyContent.replace(param.getIndex(), paramValue);
                parameters.add(paramValue);
            }

            if (finalBodyContent.length() > 1024) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Template body more than maximum allowed size 1024");
                return record;
            }

            TransactionalEmailRecord updatedRecord = txerService.save(record);

            // Construct header content
            Optional<RefreshTemplateResponseComponent> headerComponentOptional = template.getHeaderComponent();

            String headerDocumentUrl = null;
            String filename = indexRow.getText2();

            if (headerComponentOptional.isPresent() && fileMap.containsKey(filename)) {
                TransactionalEmailFile transactionalEmailFile = fileMap.get(filename);
                headerDocumentUrl = FILE_EXTERNAL_URL
                        .replace("{domain}", "tx")
                        .replace("{activityId}", targetActivity.getId().toString())
                        .replace("{fileId}", transactionalEmailFile.getId().toString())
                        .replace("{filename}", transactionalEmailFile.getName());
            }

            Optional<RefreshTemplateResponseComponent> buttonComponentOptional = template.getButtonsComponent();
            List<String> ackParameters = null;
            if (buttonComponentOptional.isPresent()) {
                ackParameters = new ArrayList<>();
                ackParameters.add("tx-" + updatedRecord.getId().toString());
            }

            Account account = targetType.getAccount();
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            SendMessageResponse sendMessageResponse = session.sendTemplateMessage(template.getName(), template.getLanguage(), mobileNo, headerDocumentUrl, parameters, ackParameters);
            updatedRecord.setWhatsappMessageId(sendMessageResponse.getMessages().get(0).getId());
            updatedRecord.setWhatsappBodyContent(finalBodyContent);

            return updatedRecord;
        }

        return record;
    }

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        TransactionalEmailActivity targetActivity = txeaService.getById(targetJob.getActivityId()).get();

        // NOT manually purged before
        if (targetActivity.getPurgedTimestamp() == null) {

            for (TransactionalEmailIndexRow indexRow : targetActivity.getTransactionalEmailIndexRows()) {
                indexRow.setTransactionalEmailFile(null);
            }

            int totalFileSize = 0;
            TransactionalEmailType targetType = targetActivity.getTransactionalEmailType();
            for (TransactionalEmailFile targetFile : targetActivity.getTransactionalEmailFiles()) {
                totalFileSize += targetFile.getFileSize();
                fileStorageService.delete(
                        targetType.getAccount(),
                        FileObjectType.TRANSACTIONAL_EMAIL,
                        targetActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            targetActivity.getTransactionalEmailFiles().clear();

            targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            targetActivity.setPurgedBy(CREATED_BY);
            txeaService.saveAndFlush(targetActivity);

            // update storage usage statistic
            updateStorageUsageStatisticForPurgedFileSize(targetJob.getAccount().getId(), totalFileSize);
        }
    }

    @Override
    void startPrepareInternal(final Job job) throws JobProcessingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void endPrepareInternal(final Job job) throws JobProcessingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    BaseActivity getBaseActivity(final long id) {
        return txeaService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.TRANSACTIONAL_EMAIL;
    }

}
