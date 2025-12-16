package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class  JobPayload implements ApiPayload {

    private Long id;

    private JobExecutionMode executionMode;

    private JobEventType eventType;

    private DomainType domainType;

    private Integer accountId;

    private String accountName;

    private Long typeId;

    private String typeName;

    private Long activityId;

    private String activityName;

    private JobStatus status;

    private String reason;

    private String createdBy;

    private OffsetDateTime createdTimestamp;

    private OffsetDateTime scheduledExecutionTimestamp;

    private OffsetDateTime executionStartTimestamp;

    private OffsetDateTime executionEndTimestamp;

    private boolean retry;

    private OffsetDateTime retryTimestamp;

    private DigitalFiling digitalFiling;

    private TransactionalEmail transactionalEmail;

    private EmailCampaign emailCampaign;

    private Sms sms;

    private WhatsApp whatsApp;


    @Data
    public static class DigitalFiling {

        private int totalRecords;


        public static DigitalFiling from(final DigitalFilingActivity digitalFilingActivity) {
            DigitalFiling target = new DigitalFiling();
            target.setTotalRecords(digitalFilingActivity.getDigitalFilingRecords().size());

            return target;
        }

    }

    @Data
    public static class TransactionalEmail {

        private int totalRecords;
        private int totalRecordsSent;
        private int totalRecordsBounced;
        private int totalRecordsSkipped;

        private String fromEmail;
        private String fromName;
        private String subject;
        private String content;

        private String whatsAppTemplateName;
        private String whatsAppBodyContent;
        private Boolean whatsAppDocument;
        private String whatsAppFooterContent;
        private String whatsAppButton;

        private int whatsAppStatusSent;
        private int whatsAppStatusSkip;
        private int whatsAppStatusRead;
        private int whatsAppStatusDelivered;
        private int whatsAppStatusAcknowledge;
        private int whatsAppStatusFailed;


        public static TransactionalEmail from(final TransactionalEmailActivity activity) {
            TransactionalEmail target = new TransactionalEmail();
            target.setTotalRecords(activity.getTransactionalEmailRecords().size());
            target.setTotalRecordsSent(activity.getEmailStatusSent());
            target.setTotalRecordsBounced(activity.getEmailStatusBounced());
            int totalSkip = activity.getEmailStatusUnsubscribedSkip() +
                    (activity.getEmailStatusBouncedSkip() == null ? 0: activity.getEmailStatusBouncedSkip());
            target.setTotalRecordsSkipped(totalSkip);

            target.setFromEmail(activity.getEmailFrom());
            target.setFromName(activity.getEmailFromName());
            target.setSubject(activity.getEmailSubject());
            target.setContent(activity.getEmailContent());

            target.setWhatsAppTemplateName(activity.getWhatsAppTemplateName());
            target.setWhatsAppBodyContent(activity.getWhatsAppBodyContent());
            target.setWhatsAppDocument(activity.getWhatsAppDocument());
            target.setWhatsAppFooterContent(activity.getWhatsAppFooterContent());
            target.setWhatsAppButton(activity.getWhatsAppButton());

            target.setWhatsAppStatusSent(activity.getWhatsAppStatusSent() == null ? 0: activity.getWhatsAppStatusSent());
            target.setWhatsAppStatusSkip(activity.getWhatsAppStatusSkip() == null ? 0: activity.getWhatsAppStatusSkip());
            target.setWhatsAppStatusDelivered(activity.getWhatsAppStatusDelivered() == null ? 0: activity.getWhatsAppStatusDelivered());
            target.setWhatsAppStatusRead(activity.getWhatsAppStatusRead() == null ? 0: activity.getWhatsAppStatusRead());
            target.setWhatsAppStatusAcknowledge(activity.getWhatsAppStatusAcknowledge() == null ? 0: activity.getWhatsAppStatusAcknowledge());
            target.setWhatsAppStatusFailed(activity.getWhatsAppStatusFailed() == null ? 0: activity.getWhatsAppStatusFailed());

            return target;
        }
    }

    @Data
    public static class EmailCampaign {

        private int totalRecords;
        private int totalRecordsSent;
        private int totalRecordsBounced;
        private int totalRecordsSkipped;

        private String fromEmail;
        private String fromName;
        private String subject;
        private String content;
        private BaseFilePayload filePayload;

        public static EmailCampaign from(final EmailCampaignActivity emailCampaignActivity) {
            EmailCampaign target = new EmailCampaign();
            target.setTotalRecords(emailCampaignActivity.getEmailCampaignRecords().size());
            target.setTotalRecordsSent(emailCampaignActivity.getEmailStatusSent());
            target.setTotalRecordsBounced(emailCampaignActivity.getEmailStatusBounced());
            int totalSkip = emailCampaignActivity.getEmailStatusUnsubscribedSkip() +
                    (emailCampaignActivity.getEmailStatusBouncedSkip() == null ? 0: emailCampaignActivity.getEmailStatusBouncedSkip());
            target.setTotalRecordsSkipped(totalSkip);
            target.setFromEmail(emailCampaignActivity.getEmailFrom());
            target.setFromName(emailCampaignActivity.getEmailFromName());
            target.setSubject(emailCampaignActivity.getEmailSubject());
            target.setContent(emailCampaignActivity.getEmailContent());

            return target;
        }

    }

    @Data
    public static class Sms {
        private String smsFrom;
        private String smsContent;

        private int totalRecords;
        private int totalRecordsSent;
        private int totalRecordsError;
        private int totalCreditUsed;

        public static Sms from(final SmsActivity smsActivity) {
            Sms target = new Sms();
            target.setSmsFrom(smsActivity.getSmsFrom());
            target.setSmsContent(smsActivity.getSmsContent());
            target.setTotalRecords(smsActivity.getTotalSms());
            target.setTotalRecordsSent(smsActivity.getSmsStatusSent());
            target.setTotalRecordsError(smsActivity.getSmsStatusError());
            target.setTotalCreditUsed(smsActivity.getSmsCreditUsed());

            return target;
        }
    }

    @Data
    public static class WhatsApp {
        private String whatsAppTemplateName;
        private String whatsAppBodyContent;
        private Boolean whatsAppDocument;
        private String whatsAppFooterContent;
        private String whatsAppButton;

        private int totalRecords;
        private int totalRecordsSent;
        private int totalRecordsDelivered;
        private int totalRecordsSkipped;
        private int totalRecordsRead;
        private int totalRecordsAcknowledged;
        private int totalRecordsFailed;
        private BaseFilePayload filePayload;

        public static WhatsApp from(final WhatsAppActivity activity) {
            WhatsApp target = new WhatsApp();
            target.setTotalRecords(activity.getWhatsAppRecords().size());
            target.setTotalRecordsSent(activity.getWhatsAppStatusSent());
            target.setTotalRecordsSkipped(activity.getWhatsAppStatusSkip());
            target.setTotalRecordsDelivered(activity.getWhatsAppStatusDelivered());
            target.setTotalRecordsRead(activity.getWhatsAppStatusRead());
            target.setTotalRecordsAcknowledged(activity.getWhatsAppStatusAcknowledge());
            target.setTotalRecordsFailed(activity.getWhatsAppStatusFailed());

            target.setWhatsAppTemplateName(activity.getWhatsAppTemplateName());
            target.setWhatsAppBodyContent(activity.getWhatsAppBodyContent());
            target.setWhatsAppDocument(activity.getWhatsAppDocument());
            target.setWhatsAppFooterContent(activity.getWhatsAppFooterContent());
            target.setWhatsAppButton(activity.getWhatsAppButton());

            return target;
        }

    }

    public static JobPayload from(final Job job) {
        JobPayload payload = new JobPayload();
        payload.setId(job.getId());
        payload.setExecutionMode(job.getExecutionMode());
        payload.setEventType(job.getEventType());
        payload.setDomainType(job.getDomainType());
        payload.setAccountId(job.getAccount().getId());
        payload.setAccountName(job.getAccount().getCompanyName());
        payload.setTypeId(job.getTypeId());
        payload.setTypeName(job.getTypeName());
        payload.setActivityId(job.getActivityId());
        payload.setActivityName(job.getActivityName());
        payload.setStatus(job.getStatus());
        payload.setReason(job.getErrorMessage());
        payload.setCreatedBy(job.getCreatedBy());
        payload.setCreatedTimestamp(job.getCreatedTimestamp());
        payload.setScheduledExecutionTimestamp(job.getScheduledExecutionTimestamp());
        payload.setExecutionStartTimestamp(job.getExecutionStartTimestamp());
        payload.setExecutionEndTimestamp(job.getExecutionEndTimestamp());
        payload.setRetry(job.isRetry());
        payload.setRetryTimestamp(job.getRetryTimestamp());

        return payload;
    }

}
