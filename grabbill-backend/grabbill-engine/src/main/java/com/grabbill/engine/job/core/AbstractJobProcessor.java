package com.grabbill.engine.job.core;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DataType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEvent;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.service.*;
import com.grabbill.engine.configuration.JobProperties;
import com.grabbill.engine.job.JobProcessingException;
import com.grabbill.engine.job.dsn.DsnScanJobEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
@Slf4j
public abstract class AbstractJobProcessor implements JobProcessor {

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    JobService jobService;

    @Autowired
    AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    JobProperties jobProperties;

    @Autowired
    IndexRowHelper indexRowHelper;

    @Autowired
    SmtpService smtpService;

    @Autowired
    MailServerService mailServerService;

    @Autowired
    MessageIdGenerator messageIdGenerator;

    @Autowired
    BouncedEmailService bouncedEmailService;

    @Autowired
    DsnScanJobEventPublisher jobEventPublisher;

    @Autowired
    UnsubscribedEmailService unsubscribedEmailService;

    @Autowired
    UnsubscribedEmailLinkService unsubscribedEmailLinkService;

    @Autowired
    EmbeddedLinkService embeddedLinkService;


    @Transactional
    @Override
    public void process(final JobEvent jobEvent) throws JobProcessingException {

        // step 1: retrieve job instance
        Job targetJob = getJobAndMarkExecutionStartTimestamp(jobEvent.getJobId());


        // step 2: verify job instance state
        if (!JobStatus.QUEUED.equals(targetJob.getStatus())) {
            markJobAsFailed(targetJob, "Invalid job state for execution - " + targetJob.getStatus());
            return;
        }
        targetJob = markJobAsProcessing(targetJob);
        log.info("Job event processing - " + jobEvent);


        // step 3: retrieve activity instance
        BaseActivity baseActivity = getBaseActivity(jobEvent.getActivityId());
        if (baseActivity == null) {
            markJobAsFailed(targetJob, "Target activity with id not found - " + jobEvent.getActivityId());
            return;
        }


        try {
            if (JobEventType.PREPARE.equals(targetJob.getEventType())) {
                if (!ProcessStatus.SUBMITTED.equals(baseActivity.getStatus())) {
                    markJobAsFailed(targetJob, "Invalid activity state for prepare execution - " + baseActivity.getStatus());
                    return;
                }

                startPrepareInternal(targetJob);

            } else if (JobEventType.PROCESS.equals(targetJob.getEventType())) {
                // step 4.1:
                if (!targetJob.isRetry()) {
                    if (!ProcessStatus.SUBMITTED.equals(baseActivity.getStatus())) {
                        markJobAsFailed(targetJob, "Invalid activity state for execution - " + baseActivity.getStatus());
                        return;
                    }

                    // step 4.1.1: process new activity
                    processInternal(targetJob);

                } else {
                    if (!ProcessStatus.SUBMITTED.equals(baseActivity.getStatus())
                            // TODO: should allow to retry for activity of ERROR status??
                            && !ProcessStatus.ERROR.equals(baseActivity.getStatus())) {
                        markJobAsFailed(targetJob, "Invalid activity state for execution - " + baseActivity.getStatus());
                        return;
                    }

                    // step 4.1.2: retry process activity
                    processInternal(targetJob);
                    targetJob.setRetryTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    jobService.save(targetJob);
                }

            } else if (JobEventType.PURGE.equals(targetJob.getEventType())) {
                if (!ProcessStatus.COMPLETED.equals(baseActivity.getStatus())) {
                    markJobAsFailed(targetJob, "Invalid activity state for purging - " + baseActivity.getStatus());
                    return;
                }

                // step 4.2: purge activity
                purgeInternal(targetJob);
            }

        } catch (JobProcessingException e) {
            markJobAsFailed(targetJob, e.getMessage());
            return;
        }


        // step 5: mark job as completed
        getAndMarkJobAsCompleted(jobEvent.getJobId());

        if (JobEventType.PREPARE.equals(targetJob.getEventType())) {
            endPrepareInternal(targetJob);
        }
    }

    abstract void processInternal(Job job) throws JobProcessingException;

    abstract void purgeInternal(Job job) throws JobProcessingException;

    abstract void startPrepareInternal(Job job) throws JobProcessingException;

    abstract void endPrepareInternal(Job job) throws JobProcessingException;

    abstract BaseActivity getBaseActivity(long id);

    void scheduleAutoPurgeJobIfSet(
            final PurgeableType targetType,
            final long typeId,
            final long activityId,
            final String activityName,
            final OffsetDateTime expectedPurgeTimestamp
    ) {

        if (targetType.isAutoPurge()) {
            Job job = new Job();
            job.setTypeId(typeId);
            job.setTypeName(targetType.getName());
            job.setAccount(targetType.getAccount());
            job.setDomainType(getSupportedActivityType());
            job.setActivityId(activityId);
            job.setActivityName(activityName);
            job.setExecutionMode(JobExecutionMode.SCHEDULED);
            job.setEventType(JobEventType.PURGE);
            job.setStatus(JobStatus.NEW);
            job.setScheduledExecutionTimestamp(expectedPurgeTimestamp);
            job.setCreatedBy(CREATED_BY);
            job.setCreatedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            jobService.save(job);

            auditLogService.log(
                    targetType.getAccount().getId(),
                    Optional.empty(),
                    activityId,
                    getSupportedActivityType(),
                    ActionType.JOB_CREATE,
                    activityName,
                    CREATED_BY
            );
        }
    }

    private Job getJobAndMarkExecutionStartTimestamp(final long jobId) {
        Optional<Job> targetJobOptional = jobService.getById(jobId);
        if (!targetJobOptional.isPresent()) {
            log.error("Fatal error: Target job not found - " + jobId);
            throw new JobProcessingException("Target job [" + jobId + "] is not found!");
        }

        Job targetJob = targetJobOptional.get();
        AuditLog auditLog = auditLogService.log(
                targetJob.getAccount().getId(),
                Optional.empty(),
                targetJob.getActivityId(),
                getSupportedActivityType(),
                ActionType.JOB_PROCESS_START,
                targetJob.getActivityName(),
                CREATED_BY
        );
        targetJob.setExecutionStartTimestamp(auditLog.getCreatedDate());

        return targetJob;
    }

    private Job markJobAsProcessing(final Job targetJob) {
        targetJob.setStatus(JobStatus.PROCESSING);
        return jobService.saveAndFlush(targetJob);
    }

    private void markJobAsFailed(final Job targetJob, final String errorMessage) {
        targetJob.setStatus(JobStatus.FAILED);
        targetJob.setErrorMessage(errorMessage);
        targetJob.setExecutionEndTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        auditLogService.log(
                targetJob.getAccount().getId(),
                Optional.empty(),
                targetJob.getActivityId(),
                getSupportedActivityType(),
                ActionType.JOB_PROCESS_END,
                targetJob.getActivityName(),
                CREATED_BY
        );

        jobService.save(targetJob);
    }

    private void getAndMarkJobAsCompleted(final long jobId) {
        Job targetJob = jobService.getById(jobId).get();
        targetJob.setStatus(JobStatus.SUCCESS);
        targetJob.setExecutionEndTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        auditLogService.log(
                targetJob.getAccount().getId(),
                Optional.empty(),
                targetJob.getActivityId(),
                getSupportedActivityType(),
                ActionType.JOB_PROCESS_END,
                targetJob.getActivityName(),
                CREATED_BY
        );

        jobService.save(targetJob);
    }

    void batchSend(
            final SmtpService targetSmtpService,
            final List<MimeMessage> batchMessages,
            final Map<Long, Exception> indexRowIdToExceptionMap,
            final long batchSendInterval
    ) {
        try {
            targetSmtpService.send(batchMessages);

        } catch (MailSendException e) {
            for (Map.Entry<Object, Exception> failedMessage : e.getFailedMessages().entrySet()) {
                MimeMessage mimeMessage = (MimeMessage) failedMessage.getKey();
                Exception exception = failedMessage.getValue();
                try {
                    String messageId = mimeMessage.getMessageID();
                    indexRowIdToExceptionMap.put(Long.valueOf(messageIdGenerator.parse(messageId)), exception);

                } catch (Exception ex) {
                    // error reading the messageID!
                    // skip this message, can't map back to index row!!!
                    log.warn("Unable to map messageID back to respective index row!", ex);
                }

            }
        }
        batchMessages.clear();

        // force current thread to sleep for the defined batch send interval,
        try {
            Thread.sleep(batchSendInterval);
        } catch (InterruptedException e) {
            log.warn("Batch email send interval interrupted..");
        }
    }

    void updateStorageUsageStatisticForPurgedFileSize(
            final Integer accountId,
            final long purgedFileSize
    ) {
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(accountId);
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setTotalStorageUsed(targetUsageStatistic.getTotalStorageUsed() - purgedFileSize);
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + accountId + "].");
        }
    }

    Map<String, String> buildParameterMap(
            final WhatsAppIndexRow indexRow,
            final List<WhatsAppIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            WhatsAppIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = indexRowHelper.getText(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = indexRowHelper.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = indexRowHelper.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }

    Map<String, String> buildParameterMap(
            final EmailCampaignIndexRow indexRow,
            final List<EmailCampaignIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            EmailCampaignIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = indexRowHelper.getText(i + 1, indexRow);
                parameterMap.put(indexField.getName(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = indexRowHelper.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getName(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = indexRowHelper.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getName(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }

    void logProcessing(final Job targetJob, final Long typeId) {
        auditLogService.log(
                targetJob.getAccount().getId(),
                Optional.of(typeId),
                targetJob.getActivityId(),
                getSupportedActivityType(),
                ActionType.ACTIVITY_PROCESS,
                targetJob.getActivityName(),
                CREATED_BY
        );
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processing.");
    }

    Map<String, EmbeddedLink> generateUrlToEmbeddedLinkMap(
            final DomainType domainType,
            final Long targetTypeId,
            final Long targetActivityId,
            final String rawEmailContent
    ) {
        Map<String, EmbeddedLink> urlToEmbeddedLinkMap = new HashMap<>();
        Jsoup.parse(rawEmailContent)
                .select("a")
                .forEach(element -> {
                    if (element.hasAttr("href")) {
                        String hrefAttr = element.attributes().get("href");

                        if (!hrefAttr.equals("{{unsubscribe_link}}") && !(hrefAttr.startsWith("{{") && hrefAttr.endsWith("}}"))) {
                            EmbeddedLink targetEmbeddedLink = new EmbeddedLink();
                            targetEmbeddedLink.setDomainType(domainType);
                            targetEmbeddedLink.setTypeId(targetTypeId);
                            targetEmbeddedLink.setActivityId(targetActivityId);
                            targetEmbeddedLink.setUrl(hrefAttr);

                            urlToEmbeddedLinkMap.put(hrefAttr, embeddedLinkService.save(targetEmbeddedLink));
                        }
                    }
                });

        return urlToEmbeddedLinkMap;
    }

    boolean isValidURL(final String url) {
        String trimmedUrl = url.trim();
        if (!StringUtils.hasLength(trimmedUrl)) {
            return false;
        }

        try {
            new URL(trimmedUrl).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

}
