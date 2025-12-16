package com.grabbill.engine.job.dsn;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.BounceType;
import com.grabbill.core.model.DataType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.event.DsnScanJobEvent;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponseComponent;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.service.*;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.core.utils.SmsUtils;
import com.grabbill.engine.job.JobProcessingException;
import com.grabbill.engine.model.DsnDetails;
import com.grabbill.engine.service.DsnBodyPartHelper;
import com.sun.mail.imap.IMAPFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.grabbill.core.service.ImapService.INBOX;
import static com.grabbill.core.service.ImapService.PROTOCOL_VALUE;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class DsnScanJobProcessorImpl implements DsnScanJobProcessor {

    @Value("${spring.mail.properties.mail.imap.host}")
    private String defaultImapHost;

    @Value("${spring.mail.properties.mail.imap.port}")
    private int defaultImapPort;

    @Value("${spring.mail.username}")
    private String defaultImapUsername;

    @Value("${spring.mail.password}")
    private String defaultImapPassword;

    @Value("${file.external.url}")
    private String FILE_EXTERNAL_URL;


    @Autowired
    private BouncedEmailService bouncedEmailService;

    @Autowired
    private DsnBodyPartHelper dsnBodyPartHelper;

    @Autowired
    private DsnScanJobEventPublisher jobEventPublisher;

    @Autowired
    private ImapService imapService;

    @Autowired
    private MailServerService mailServerService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    IndexRowHelper indexRowHelper;

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService txerService;

    @Autowired
    @Qualifier("emailCampaignIndexRowService")
    private BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> ecirService;

    @Autowired
    @Qualifier("transactionalEmailIndexRowService")
    private BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> txeirService;


    @Override
    @Transactional
    public void process(final DsnScanJobEvent dsnScanJobEvent) throws JobProcessingException {
        dsnScanJobEvent.setLastExecutedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Store store = null;
        try {

            String host, username, password;
            int port;

            // decide to use custom / default grabbill IMAP configurations
            Optional<MailServer> mailServerOptional = mailServerService.getByAccountId(dsnScanJobEvent.getAccountId());
            if (mailServerOptional.isPresent() && mailServerOptional.get().isCustomServer()) {
                MailServer mailServer = mailServerOptional.get();
                host = mailServer.getImapHost();
                port = mailServer.getImapPort();
                username = mailServer.getImapUsername();
                password = mailServer.getImapPassword();

            } else {
                host = defaultImapHost;
                port = defaultImapPort;
                username = defaultImapUsername;
                password = defaultImapPassword;
            }


            // open connection to mailbox
            Properties properties = imapService.buildImapProperties(host, port);
            Session session = Session.getDefaultInstance(properties, null);
            store = session.getStore(PROTOCOL_VALUE);
            store.connect(username, password);
            IMAPFolder inbox = (IMAPFolder) store.getFolder(INBOX);
            if (!inbox.isOpen()) {
                inbox.open(Folder.READ_WRITE);
            }


            // 1. load unread & new messages from last execution time from the mailbox
            //    (minor optimisation, instead of loading entire folder)
            SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, Date.from(dsnScanJobEvent.getLastExecutedAt().minusDays(3).toInstant()));
            SearchTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN),false);
            Message[] messages = inbox.search(new AndTerm(unseen, newerThan));


            // 2. process each of the email messages
            if (messages != null) {
                if (DomainType.TRANSACTIONAL_EMAIL.equals(dsnScanJobEvent.getDomainType())) {
                    processTransactionalEmailMessages(dsnScanJobEvent, messages);

                } else if (DomainType.EMAIL_CAMPAIGN.equals(dsnScanJobEvent.getDomainType())) {
                    processEmailCampaignMessages(dsnScanJobEvent, messages);
                }
            }


            try {
                store.close();
            } catch (MessagingException ex) {
                log.error("Failed to close session store!", ex);
            }


            // republish the job if not expired
            if (OffsetDateTime.now(ZoneOffset.UTC).isBefore(dsnScanJobEvent.getExpireAt())) {
                jobEventPublisher.publish(dsnScanJobEvent);
            }

        } catch (Exception e) {
            log.error("DSN Job processing failed, skip republish for next scan!", e);
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException ex) {
                    log.error("Failed to close session store!", ex);
                }
            }
        }
    }

    private void processTransactionalEmailMessages(
            final DsnScanJobEvent dsnScanJobEvent,
            final Message[] messages
    ) {
        for (Message message : messages) {
            try {

                // DSN message is always an instance of MimeMultipart!
                // skipping the rest irrelevant messages type
                if (message.getContent() instanceof MimeMultipart) {

                    MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                    Set<BodyPart> bodyParts = dsnBodyPartHelper.extractBodyParts(mimeMultipart);

                    if (isDeliveryStatusNotificationEmail(bodyParts)) {

                        DsnDetails dsnDetails = extractDsnDetails(bodyParts);

                        // 2.1. retrieve indexRowId from messageID and using it to retrieve the target index row
                        TransactionalEmailIndexRow targetIndexRow;
                        if (!messageIdGenerator.isGrabbillMessageId(dsnDetails.getMessageId())) {
                            // skip processing if grabbill's messageID is not found
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;

                        } else {
                            String indexRowId = messageIdGenerator.parse(dsnDetails.getMessageId());
                            Optional<TransactionalEmailIndexRow> targetIndexRowOptional =
                                    txeirService.getById(Long.parseLong(indexRowId));
                            if (!targetIndexRowOptional.isPresent()) {
                                // skip processing if unable to map back to indexRow
                                message.setFlag(Flags.Flag.SEEN, false);
                                continue;
                            }

                            targetIndexRow = targetIndexRowOptional.get();
                        }


                        // 2.2. verify indexRow is mapped to a valid record
                        TransactionalEmailRecord targetRecord = targetIndexRow.getTransactionalEmailRecord();
                        // KLUDGE: index row not persisted yet to DB completely?! Don't worry,
                        //         it will be picked up and executed again in next run when record is ready
                        if (targetRecord == null) {
                            // skip processing as target record is not "ready" to be processed
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;
                        }


                        // 2.3. verify indexRow's account is equals to dsnScanJobEvent's account
                        TransactionalEmailActivity targetActivity = targetIndexRow.getTransactionalEmailActivity();
                        TransactionalEmailType targetType = targetIndexRow.getTransactionalEmailType();
                        Account targetAccount = targetType.getAccount();
                        if (!targetAccount.getId().equals(dsnScanJobEvent.getAccountId())) {
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;
                        }


                        // 2.4. translate status code to hard / soft bounce
                        BounceType bounceType;
                        if (dsnDetails.getStatusCode().startsWith("5")) {
                            bounceType = BounceType.HARD_BOUNCE;

                        } else if (dsnDetails.getStatusCode().startsWith("4")) {
                            bounceType = BounceType.SOFT_BOUNCE;

                        } else {
                            // skip processing, DSN status code is not bounced type
                            // e.g. status code start with 2 / 3, which depicts messages sent and received
                            // by reciepient
                            continue;
                        }


                        // 2.5. load record by indexRow, update record and bounce status with reason;
                        targetRecord.setEmailDsnReceivedConfirmation(true);
                        targetRecord.setDsnProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                        targetRecord.setEmailDsnMessage(dsnDetails.getReason());
                        if (BounceType.HARD_BOUNCE.equals(bounceType)) {
                            targetRecord.setEmailStatusHardBounce(true);

                        } else {
                            targetRecord.setEmailStatusSoftBounce(true);
                        }
                        targetActivity.setEmailStatusBounced(targetActivity.getEmailStatusBounced() + 1);
                        targetIndexRow = txeirService.save(targetIndexRow);
                        this.processWhatsappMessage(targetIndexRow, targetRecord);


                        // 2.6. when hard bounce, add to BouncedEmail list
                        if (BounceType.HARD_BOUNCE.equals(bounceType)) {
                            boolean hasBouncedEmail = bouncedEmailService.hasBouncedEmail(
                                    targetAccount.getId(),
                                    targetIndexRow.getText1()
                            );

                            if (!hasBouncedEmail) {
                                BouncedEmail bouncedEmail = new BouncedEmail();
                                bouncedEmail.setDomainType(dsnScanJobEvent.getDomainType());
                                bouncedEmail.setEmail(targetIndexRow.getText1());
                                bouncedEmail.setTypeId(targetType.getId());
                                bouncedEmail.setTypeName(targetType.getName());
                                bouncedEmail.setActivityId(targetActivity.getId());
                                bouncedEmail.setActivityName(targetActivity.getName());
                                bouncedEmail.setDsnStatusCode(dsnDetails.getStatusCode());
                                bouncedEmail.setReason(dsnDetails.getReason());
                                bouncedEmail.setCreatedBy(CREATED_BY);
                                bouncedEmail.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
                                bouncedEmail.setAccount(targetAccount);
                                bouncedEmailService.save(bouncedEmail);
                            }
                        }


                        // 2.7. update email message as seen / read, so it will not be read a second time
                        message.setFlag(Flags.Flag.SEEN, true);

                    }
                }

            } catch (IOException | MessagingException e) {
                log.error("Transactional email dsn message processing failed!", e);
            }
        }
    }

    private void processEmailCampaignMessages(
            final DsnScanJobEvent dsnScanJobEvent,
            final Message[] messages
    ) {
        for (Message message : messages) {
            try {

                // DSN message is always an instance of MimeMultipart!
                // skipping the rest irrelevant messages type
                if (message.getContent() instanceof MimeMultipart) {

                    MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                    Set<BodyPart> bodyParts = dsnBodyPartHelper.extractBodyParts(mimeMultipart);

                    if (isDeliveryStatusNotificationEmail(bodyParts)) {

                        DsnDetails dsnDetails = extractDsnDetails(bodyParts);

                        // 2.1. retrieve indexRowId from messageID and using it to retrieve the target index row
                        EmailCampaignIndexRow targetIndexRow;
                        if (!messageIdGenerator.isGrabbillMessageId(dsnDetails.getMessageId())) {
                            // skip processing if grabbill's messageID is not found
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;

                        } else {
                            String indexRowId = messageIdGenerator.parse(dsnDetails.getMessageId());
                            Optional<EmailCampaignIndexRow> targetIndexRowOptional =
                                    ecirService.getById(Long.parseLong(indexRowId));
                            if (!targetIndexRowOptional.isPresent()) {
                                // skip processing if unable to map back to indexRow
                                message.setFlag(Flags.Flag.SEEN, false);
                                continue;
                            }

                            targetIndexRow = targetIndexRowOptional.get();
                        }


                        // 2.2. verify indexRow is mapped to a valid record
                        EmailCampaignRecord targetRecord = targetIndexRow.getEmailCampaignRecord();
                        // KLUDGE: index row not persisted yet to DB completely?! Don't worry,
                        //         it will be picked up and executed again in next run when record is ready
                        if (targetRecord == null) {
                            // skip processing as target record is not "ready" to be processed
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;
                        }


                        // 2.3. verify indexRow's account is equals to dsnScanJobEvent's account
                        EmailCampaignActivity targetActivity = targetIndexRow.getEmailCampaignActivity();
                        EmailCampaignType targetType = targetIndexRow.getEmailCampaignType();
                        Account targetAccount = targetType.getAccount();
                        if (!targetAccount.getId().equals(dsnScanJobEvent.getAccountId())) {
                            message.setFlag(Flags.Flag.SEEN, false);
                            continue;
                        }


                        // 2.4. translate status code to hard / soft bounce
                        BounceType bounceType;
                        if (dsnDetails.getStatusCode().startsWith("5")) {
                            bounceType = BounceType.HARD_BOUNCE;

                        } else if (dsnDetails.getStatusCode().startsWith("4")) {
                            bounceType = BounceType.SOFT_BOUNCE;

                        } else {
                            // skip processing, DSN status code is not bounced type
                            // e.g. status code start with 2 / 3, which depicts messages sent and received
                            // by reciepient
                            continue;
                        }


                        // 2.5. load record by indexRow, update record and bounce status with reason;
                        targetRecord.setEmailDsnReceivedConfirmation(true);
                        targetRecord.setDsnProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                        targetRecord.setEmailDsnMessage(dsnDetails.getReason());
                        if (BounceType.HARD_BOUNCE.equals(bounceType)) {
                            targetRecord.setEmailStatusHardBounce(true);

                        } else {
                            targetRecord.setEmailStatusSoftBounce(true);
                        }
                        targetActivity.setEmailStatusBounced(targetActivity.getEmailStatusBounced() + 1);
                        targetIndexRow = ecirService.save(targetIndexRow);


                        // 2.6. when hard bounce, add to BouncedEmail list
                        if (BounceType.HARD_BOUNCE.equals(bounceType)) {
                            boolean hasBouncedEmail = bouncedEmailService.hasBouncedEmail(
                                    targetAccount.getId(),
                                    targetIndexRow.getText1()
                            );

                            if (!hasBouncedEmail) {
                                BouncedEmail bouncedEmail = new BouncedEmail();
                                bouncedEmail.setDomainType(dsnScanJobEvent.getDomainType());
                                bouncedEmail.setEmail(targetIndexRow.getText1());
                                bouncedEmail.setTypeId(targetType.getId());
                                bouncedEmail.setTypeName(targetType.getName());
                                bouncedEmail.setActivityId(targetActivity.getId());
                                bouncedEmail.setActivityName(targetActivity.getName());
                                bouncedEmail.setDsnStatusCode(dsnDetails.getStatusCode());
                                bouncedEmail.setReason(dsnDetails.getReason());
                                bouncedEmail.setCreatedBy(CREATED_BY);
                                bouncedEmail.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
                                bouncedEmail.setAccount(targetAccount);
                                bouncedEmailService.save(bouncedEmail);
                            }
                        }


                        // 2.7. update email message as seen / read, so it will not be read a second time
                        message.setFlag(Flags.Flag.SEEN, true);

                    }
                }

            } catch (IOException | MessagingException e) {
                log.error("Email campaign dsn message processing failed!", e);
            }
        }
    }

    private DsnDetails extractDsnDetails(final Set<BodyPart> bodyParts) throws MessagingException {

        DsnDetails dsnDetails = new DsnDetails();
        for (BodyPart bodyPart : bodyParts) {
            String contentType = bodyPart.getContentType();

            // mime type - "message/delivery-status"
            if (contentType.toLowerCase().startsWith(DsnBodyPartHelper.MESSAGE_DELIVERY_STATUS)) {
                String statusCode = dsnBodyPartHelper.getStatusCode(bodyPart);
                dsnDetails.setStatusCode(statusCode);

            // mime type - "text/plain"
            } else if (contentType.toLowerCase().startsWith(DsnBodyPartHelper.PLAIN_TEXT)) {
                String reason = dsnBodyPartHelper.getReason(bodyPart);
                dsnDetails.setReason(reason);

            // mime type - "message/rfc822" OR "text/rfc822-headers"
            } else if (contentType.toLowerCase().startsWith(DsnBodyPartHelper.MESSAGE_RFC822)
                    || contentType.toLowerCase().startsWith(DsnBodyPartHelper.TEXT_RFC822_HEADERS)) {
                String messageId = dsnBodyPartHelper.getMessageId(bodyPart);
                dsnDetails.setMessageId(messageId);
            }
        }

        return dsnDetails;
    }

    private boolean isDeliveryStatusNotificationEmail(final Set<BodyPart> bodyParts) throws MessagingException {
        for (BodyPart bodyPart : bodyParts) {
            String contentType = bodyPart.getContentType();
            if (contentType.toLowerCase().startsWith("message/delivery-status")) {
                return true;
            }
        }

        return false;
    }

    private void processWhatsappMessage(
            TransactionalEmailIndexRow indexRow,
            TransactionalEmailRecord record) {

        TransactionalEmailActivity targetActivity = indexRow.getTransactionalEmailActivity();

        if (targetActivity.getSendWhatsAppMessage()) {
            TransactionalEmailType targetType = targetActivity.getTransactionalEmailType();
            Account account = targetType.getAccount();

            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            String whatsappTemplateName = targetType.getWhatsAppTemplateName();
            List<RefreshTemplateResponse> templates = session.refreshTemplate();
            Optional<RefreshTemplateResponse> templateOptional = templates.stream().filter(item -> item.getName().equalsIgnoreCase(whatsappTemplateName)).findAny();


            Map<String, TransactionalEmailFile> fileMap = targetActivity.getTransactionalEmailFiles().stream()
                    .collect(Collectors.toMap(TransactionalEmailFile::getName, Function.identity()));


            if (templateOptional.isEmpty()) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Whatsapp template not found");
                txerService.save(record);
                return;
            }

            RefreshTemplateResponse template = templateOptional.get();

            String mobileNo = indexRow.getText4();
            if (!SmsUtils.isValidWhatsAppPhoneNumber(mobileNo)) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Invalid phone number");
                txerService.save(record);
                return;
            }

            // Construct body content
            String bodyText = template.getBodyText();

            if (bodyText == null) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Template body not found");
                txerService.save(record);
                return;
            }

            String finalBodyContent = bodyText;
            List<String> parameters = new ArrayList<>();
            for (TransactionalEmailWhatsappTemplateParam param : targetType.getTransactionalEmailWhatsappTemplateParams()) {
                String paramValue = indexRowHelper.buildTransactionalEmailParameterMap(indexRow, targetType.getTransactionalEmailIndexFields()).get(param.getField());
                finalBodyContent = finalBodyContent.replace(param.getIndex(), paramValue);
                parameters.add(paramValue);
            }

            if (finalBodyContent.length() > 1024) {
                targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
                record.setWhatsappStatusSkip(true);
                record.setWhatsappStatusSkipReason("Template body more than maximum allowed size 1024");
                txerService.save(record);
                return;
            }

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
                ackParameters.add("tx-" + record.getId().toString());
            }

            session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            SendMessageResponse sendMessageResponse = session.sendTemplateMessage(template.getName(), template.getLanguage(), mobileNo, headerDocumentUrl, parameters, ackParameters);
            record.setWhatsappMessageId(sendMessageResponse.getMessages().get(0).getId());
            record.setWhatsappBodyContent(finalBodyContent);

            txerService.save(record);
        }
    }
}
