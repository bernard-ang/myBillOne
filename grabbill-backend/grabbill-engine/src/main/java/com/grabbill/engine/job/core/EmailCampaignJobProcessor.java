package com.grabbill.engine.job.core;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.event.DsnScanJobEvent;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.engine.job.JobProcessingException;
import com.grabbill.engine.service.RFCPatterns;
import lombok.extern.slf4j.Slf4j;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class EmailCampaignJobProcessor extends AbstractJobProcessor {

    @Value("${email-campaign.batch-size}")
    private int BATCH_SIZE;

    @Value("${email-campaign.batch-send-interval}")
    private int BATCH_SEND_INTERVAL;

    @Value("${unsubscribe-email-link.url}")
    private String UNSUBSCRIBE_EMAIL_LINK_URL;

    @Value("${open-email-link.url}")
    private String EMAIL_OPENED_LINK_URL;

    @Value("${embedded-link-click.url}")
    private String URL_LINK_CLICKED_URL;

    @Autowired
    @Qualifier("emailCampaignTypeService")
    private BaseTypeService<EmailCampaignType, EmailCampaignActivity> ectService;

    @Autowired
    @Qualifier("emailCampaignActivityService")
    private BaseActivityService<EmailCampaignType, EmailCampaignActivity> ecaService;

    @Autowired
    @Qualifier("emailCampaignIndexRowService")
    private BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> ecirService;

    @Autowired
    @Qualifier("emailCampaignRecordService")
    private BaseRecordService<EmailCampaignRecord> ecrService;



    @Override
    void processInternal(final Job targetJob) throws JobProcessingException {
        // mark activity as processing
        EmailCampaignActivity targetActivity = ecaService.markAsProcessing(targetJob.getActivityId());
        EmailCampaignType targetType = targetActivity.getEmailCampaignType();
        logProcessing(targetJob, targetType.getId());


        targetActivity = ecaService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getEmailCampaignType();

        Map<Long, Exception> indexRowIdToExceptionMap;
        Optional<MailServer> mailServerOptional = mailServerService.getByAccount(targetType.getAccount());
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
        EmailCampaignActivity updatedActivity = ecaService.save(targetActivity);


        // update activity statistics
        int sent = 0;
        int bounced = 0;
        int bouncedSkip = 0;
        int unsubscribedSkip = 0;
        for (EmailCampaignIndexRow indexRow : updatedActivity.getEmailCampaignIndexRows()) {

            EmailCampaignRecord record = indexRow.getEmailCampaignRecord();

            // update record(s) with sent error
            if (!indexRowIdToExceptionMap.isEmpty() && indexRowIdToExceptionMap.containsKey(indexRow.getId())) {
                Exception exception = indexRowIdToExceptionMap.get(indexRow.getId());
                record.setMessage((exception.getMessage().length() < 255) ?
                        exception.getMessage() : exception.getMessage().substring(0, 255));
                record.setStatus(ProcessStatus.ERROR);

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
                            bouncedEmail.setAccount(targetType.getAccount());
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

        Integer accountId = targetJob.getAccount().getId();
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(accountId);
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setTotalEmailCampaignSent(targetUsageStatistic.getTotalEmailCampaignSent() + updatedActivity.getEmailStatusSent());
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + accountId + "].");
        }

        // update type with last sent by and sent date
        EmailCampaignType updatedType = ectService.getByActivity(updatedActivity);
        updatedType.setLastSentBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastSentDate(updatedActivity.getLastModifiedDate());
        ectService.save(updatedType);

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

    private Map<Long, Exception> processIndexRowsInternal (
            final EmailCampaignType targetType,
            final EmailCampaignActivity targetActivity,
            final SmtpService targetSmtpService
    ) throws JobProcessingException {

        Account account = targetType.getAccount();
        List<EmailCampaignFile> files = targetActivity.getEmailCampaignFiles();
        Map<Long, Exception> indexRowIdToExceptionMap = new HashMap<>();
        List<MimeMessage> batchMessages = new ArrayList<>();

        Map<String, EmbeddedLink> urlToEmbeddedLinkMap = generateUrlToEmbeddedLinkMap(
                DomainType.EMAIL_CAMPAIGN, targetType.getId(), targetActivity.getId(), targetActivity.getEmailContent());
        for (EmailCampaignIndexRow indexRow : targetActivity.getEmailCampaignIndexRows()) {
            String email = indexRow.getText1();

            EmailCampaignRecord record = indexRow.getEmailCampaignRecord();

            // IMPORTANT: skipping those index row with record (already processed)
            if (record == null) {
                record = new EmailCampaignRecord();
                record.setName(email);
                record.setPriority(targetActivity.getPriority());
                record.setEmailCampaignActivity(targetActivity);
                record.setEmailCampaignIndexRow(indexRow);


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
                        Map<String, String> parameterMap = buildParameterMap(indexRow, targetActivity.getEmailCampaignIndexFields());

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
                                    String urlPath = "ecemail/" + targetActivity.getId()
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
                            String emailOpenedImageTag = "<img id='grb' src='" + EMAIL_OPENED_LINK_URL + "/ecemail/" + indexRow.getId() + "/pixel.png" + "'/></body>";
                            outboundProcessedEmailContent = processedEmailContent.replace("</body>", emailOpenedImageTag);
                        }

                        // process the attachment file
                        if (targetType.isHasAttachment()) {
                            Multipart multipart = new MimeMultipart();
                            BodyPart messageContent = new MimeBodyPart();
                            messageContent.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");
                            multipart.addBodyPart(messageContent);

                            for (EmailCampaignFile file : files) {
                                byte[] fileBytes = fileStorageService.download(
                                        targetType.getAccount(),
                                        FileObjectType.EMAIL_CAMPAIGN,
                                        targetActivity.getId().toString(),
                                        file.getName()
                                );

                                ByteArrayDataSource dataSource = new ByteArrayDataSource(fileBytes, file.getFileType());
                                BodyPart messageAttachment = new MimeBodyPart();
                                messageAttachment.setDataHandler(new DataHandler(dataSource));
                                messageAttachment.setFileName(file.getName());

                                multipart.addBodyPart(messageAttachment);
                            }

                            message.setContent(multipart);

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
                }


                // link record to index row
                EmailCampaignRecord savedRecord = ecrService.save(record);
                indexRow.setEmailCampaignType(targetType);
                indexRow.setEmailCampaignRecord(savedRecord);
                ecirService.save(indexRow);
            }
        }

        if (!batchMessages.isEmpty()) {
            batchSend(targetSmtpService, batchMessages, indexRowIdToExceptionMap, BATCH_SEND_INTERVAL);
        }

        return indexRowIdToExceptionMap;
    }

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        EmailCampaignActivity targetActivity = ecaService.getById(targetJob.getActivityId()).get();

        // NOT manually purged before
        if (targetActivity.getPurgedTimestamp() == null) {

            EmailCampaignType targetType = targetActivity.getEmailCampaignType();
            for (EmailCampaignFile targetFile : targetActivity.getEmailCampaignFiles()) {
                fileStorageService.delete(
                        targetType.getAccount(),
                        FileObjectType.EMAIL_CAMPAIGN,
                        targetActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            targetActivity.getEmailCampaignFiles().clear();

            targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            targetActivity.setPurgedBy(CREATED_BY);
            ecaService.saveAndFlush(targetActivity);
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
        return ecaService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.EMAIL_CAMPAIGN;
    }

}
