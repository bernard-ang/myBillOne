package com.grabbill.engine.job.core;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DataType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponseComponent;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.service.*;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.core.utils.SmsUtils;
import com.grabbill.engine.job.JobProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class MTWhatsAppJobProcessor extends AbstractJobProcessor {

    @Value("${file.external.url}")
    private String FILE_EXTERNAL_URL;

    @Autowired
    @Qualifier("mtWhatsAppTypeService")
    private BaseTypeService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppTypeService;

    @Autowired
    @Qualifier("mtWhatsAppActivityService")
    private BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;

    @Autowired
    @Qualifier("mtWhatsAppIndexRowService")
    private BaseIndexRowService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexRow> mtWhatsAppIndexRowService;

    @Autowired
    @Qualifier("mtWhatsAppRecordService")
    private MTWhatsAppRecordService mtWhatsAppRecordService;

    @Autowired
    @Qualifier("mtWhatsAppActivitySftpService")
    private BaseActivitySftpService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexField> baseActivitySftpService;

    @Autowired
    private WhatsAppService whatsAppService;


    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    private ActivityTrackingService activityTrackingService;

    @Override
    void processInternal(Job targetJob) throws JobProcessingException {
        // mark activity as processing
        MTWhatsAppActivity targetActivity = mtWhatsAppActivityService.markAsProcessing(targetJob.getActivityId());
        MTWhatsAppType targetType = targetActivity.getMtWhatsAppType();
        logProcessing(targetJob, targetType.getId());


        targetActivity = mtWhatsAppActivityService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getMtWhatsAppType();

        Map<Long, Exception> indexRowIdToExceptionMap;
        indexRowIdToExceptionMap = processIndexRowsInternal(targetType, targetActivity);


        // mark activity as processed completely
        targetActivity.setStatus(ProcessStatus.COMPLETED);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity = mtWhatsAppActivityService.updateCount(targetActivity);
        MTWhatsAppActivity updatedActivity = mtWhatsAppActivityService.save(targetActivity);


        // update activity statistics
        int sent = 0;
        for (MTWhatsAppIndexRow indexRow : updatedActivity.getMtWhatsAppIndexRows()) {

            MTWhatsAppRecord record = indexRow.getMtWhatsAppRecord();

            // update record(s) with sent error
            if (!indexRowIdToExceptionMap.isEmpty() && indexRowIdToExceptionMap.containsKey(indexRow.getId())) {
                Exception exception = indexRowIdToExceptionMap.get(indexRow.getId());
                record.setMessage((exception.getMessage().length() < 255) ?
                        exception.getMessage() : exception.getMessage().substring(0, 255));
                record.setStatus(ProcessStatus.ERROR);
                record.setWhatsAppStatusFailedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                record.setWhatsAppStatusFailed(true);
                mtWhatsAppRecordService.save(record);

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                String message = stringWriter.toString();
                log.warn(message);
            }

            // accumulate statistical data
            if(record.getWhatsAppStatusSent() != null && record.getWhatsAppStatusSent()) {
                sent++;
            }
        }

        // update statistic data
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processed.");

        activityTrackingService.trackCompleted(ActivityTrackingService.ActivityType.MT_WHATSAPP, targetJob.getActivityId());

        // if not archive, purge files
        log.info("Total storage used is [" + updatedActivity.getStorageSize() + "] bytes.");


        Integer accountId = targetJob.getAccount().getId();
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(accountId);
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setTotalStorageUsed(targetUsageStatistic.getTotalStorageUsed() + updatedActivity.getStorageSize());
            targetUsageStatistic.setTotalWhatsappMessageSent((targetUsageStatistic.getTotalWhatsappMessageSent() != null ? targetUsageStatistic.getTotalWhatsappMessageSent() : 0) + sent);
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + accountId + "].");
        }


        // update type with last sent by and sent date; update all index fields as hard referenced
        MTWhatsAppType updatedType = mtWhatsAppTypeService.getByActivity(updatedActivity);
        updatedType.setLastSentBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastSentDate(updatedActivity.getLastModifiedDate());
        for (MTWhatsAppIndexField targetIndexField : updatedType.getMtWhatsAppIndexFields()) {
            targetIndexField.setHardRef(true);
        }
        mtWhatsAppTypeService.save(updatedType);
    }

    private Map<Long, Exception> processIndexRowsInternal(
            final MTWhatsAppType targetType,
            final MTWhatsAppActivity targetActivity
    ) throws JobProcessingException {

        Account account = targetType.getAccount();
        Map<String, MTWhatsAppFile> fileMap = targetActivity.getMtWhatsAppFiles().stream()
                .collect(Collectors.toMap(MTWhatsAppFile::getName, Function.identity()));
        Map<Long, Exception> indexRowIdToExceptionMap = new HashMap<>();

        Map<String, MTWhatsAppTemplate> typeTemplatesMap = new HashMap<>();
        for (MTWhatsAppTemplate mtWhatsappTemplate : targetType.getMtWhatsappTemplates()) {
            typeTemplatesMap.put(mtWhatsappTemplate.getWhatsAppTemplateName(), mtWhatsappTemplate);
        }

        Map<String, RefreshTemplateResponse> templatesMap = new HashMap<>();
        WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
        for (String whatsappTemplateName : typeTemplatesMap.keySet()) {
            List<RefreshTemplateResponse> templates = session.refreshTemplate();
            Optional<RefreshTemplateResponse> template = templates.stream().filter(item -> item.getName().equalsIgnoreCase(whatsappTemplateName)).findAny();

            if (template.isPresent()) {
                RefreshTemplateResponse currentTemplate = template.get();
                templatesMap.put(currentTemplate.getName(), currentTemplate);

                MTWhatsAppActivityTemplate mtWhatsAppActivityTemplate = new MTWhatsAppActivityTemplate();
                mtWhatsAppActivityTemplate.setWhatsAppTemplateName(currentTemplate.getName());
                mtWhatsAppActivityTemplate.setWhatsAppBodyContent(currentTemplate.getBodyText());
                mtWhatsAppActivityTemplate.setWhatsAppDocument(currentTemplate.getHeaderComponent().isPresent());
                mtWhatsAppActivityTemplate.setWhatsAppFooterContent(currentTemplate.getFooterText());
                mtWhatsAppActivityTemplate.setWhatsAppButton(currentTemplate.getButtonText());
                mtWhatsAppActivityTemplate.setMtWhatsAppActivity(targetActivity);

                targetActivity.getMtWhatsAppActivityTemplates().add(mtWhatsAppActivityTemplate);
            }
        }

        AtomicLong storageSize = new AtomicLong(0);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        for (MTWhatsAppIndexRow indexRow : targetActivity.getMtWhatsAppIndexRows()) {
            txTemplate.executeWithoutResult(status -> {
                String mobileNo = indexRow.getText1();
                String filename = indexRow.getText2();
                String password = indexRow.getText3();
                String templateName = indexRow.getText4();

                MTWhatsAppRecord record = indexRow.getMtWhatsAppRecord();

                // construct whatsapp parameter map, perform value interpolation on email content
                Map<String, String> parameterMap = buildParameterMap(indexRow, targetType.getMtWhatsAppIndexFields());

                // IMPORTANT: skipping those index row with record (already processed)
                if (record == null) {
                    record = new MTWhatsAppRecord();
                    record.setName(mobileNo);
                    record.setPriority(targetActivity.getPriority());
                    record.setMtWhatsAppActivity(targetActivity);
                    record.setMtWhatsAppIndexRow(indexRow);

                    try {
                        if (targetType.isHasAttachment()) {
                            if (fileMap.containsKey(filename)) {

                                MTWhatsAppFile file = fileMap.get(filename);
                                byte[] fileBytes = fileStorageService.download(
                                        targetType.getAccount(),
                                        FileObjectType.WHATSAPP,
                                        targetActivity.getId().toString(),
                                        filename
                                );
                                storageSize.addAndGet(file.getFileSize());

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

                                indexRow.setMtWhatsAppFile(file);

                                // file not found
                            } else {
                                record.setMessage("File [" + filename + "] does not exist!");
                                throw new GrabbillException(record.getMessage());
                            }
                        }

                        record = processWhatsAppMessage(
                                indexRow,
                                record,
                                targetActivity,
                                targetType,
                                parameterMap,
                                fileMap,
                                typeTemplatesMap.get(templateName),
                                templatesMap.get(templateName)
                        );

                        // done processing record
                        record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                        record.setStatus(ProcessStatus.COMPLETED);

                    } catch (Exception e) {
                        log.error("Unknown error while processing index row", e);
                        record.setWhatsAppStatusFailedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                        record.setStatus(ProcessStatus.ERROR);
                        record.setMessage((e.getMessage().length() < 255) ?
                                e.getMessage() : e.getMessage().substring(0, 255));
                        record.setWhatsAppStatusFailed(true);
                        targetActivity.setWhatsAppStatusFailed(targetActivity.getWhatsAppStatusFailed() + 1);
                    }


                    // link record to index row
                    MTWhatsAppRecord savedRecord = mtWhatsAppRecordService.save(record);
                    indexRow.setMtWhatsAppType(targetType);
                    indexRow.setMtWhatsAppRecord(savedRecord);
                    mtWhatsAppIndexRowService.save(indexRow);
                }
            });
        }

        targetActivity.setStorageSize(storageSize.get());

        return indexRowIdToExceptionMap;
    }

    private MTWhatsAppRecord processWhatsAppMessage(
            MTWhatsAppIndexRow indexRow,
            MTWhatsAppRecord record,
            MTWhatsAppActivity targetActivity,
            MTWhatsAppType targetType,
            Map<String, String> parameterMap,
            Map<String, MTWhatsAppFile> fileMap,
            MTWhatsAppTemplate typeTemplate,
            RefreshTemplateResponse template
    ) {

        if (typeTemplate == null) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setStatus(ProcessStatus.ERROR);
            record.setWhatsAppStatusSkipReason("Multi-template WhatsApp type template not found");
            return record;
        }

        if (template == null) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setStatus(ProcessStatus.ERROR);
            record.setWhatsAppStatusSkipReason("Multi-template WhatsApp activity template not found");
            return record;
        }

        String mobileNo = indexRow.getText1();
        if (!SmsUtils.isValidWhatsAppPhoneNumber(mobileNo)) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setStatus(ProcessStatus.ERROR);
            record.setWhatsAppStatusSkipReason("Invalid phone number");
            return record;
        }

        // Construct body content
        String bodyText = template.getBodyText();

        if (bodyText == null) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setWhatsAppStatusSkipReason("Template body not found");
            record.setStatus(ProcessStatus.ERROR);
            record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            return record;
        }

        String finalBodyContent = bodyText;
        List<String> parameters = new ArrayList<>();
        for (MTWhatsappTemplateParam param : typeTemplate.getMtWhatsappTemplateParams()) {
            String paramValue = parameterMap.get(param.getField());
            finalBodyContent = finalBodyContent.replace(param.getIndex(), paramValue);
            parameters.add(paramValue);
        }

        if (finalBodyContent.length() > 1024) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setWhatsAppStatusSkipReason("Template body more than maximum allowed size 1024");
            record.setStatus(ProcessStatus.ERROR);
            return record;
        }

        MTWhatsAppRecord updatedRecord = mtWhatsAppRecordService.save(record);

        // Construct header content
        Optional<RefreshTemplateResponseComponent> headerComponentOptional = template.getHeaderComponent();

        String headerDocumentUrl = null;
        String filename = indexRow.getText2();

        if (headerComponentOptional.isPresent() && fileMap.containsKey(filename)) {
            MTWhatsAppFile mtWhatsAppFile = fileMap.get(filename);
            headerDocumentUrl = FILE_EXTERNAL_URL
                    .replace("{domain}", "mtwa")
                    .replace("{activityId}", targetActivity.getId().toString())
                    .replace("{fileId}", mtWhatsAppFile.getId().toString())
                    .replace("{filename}", mtWhatsAppFile.getName());
        }

        Optional<RefreshTemplateResponseComponent> buttonComponentOptional = template.getButtonsComponent();
        List<String> ackParameters = null;
        if (buttonComponentOptional.isPresent()) {
            ackParameters = new ArrayList<>();
            ackParameters.add("mtwa-" + updatedRecord.getId().toString());
        }

        // TODO: handle socket timeout
        Account account = targetType.getAccount();
        WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
        SendMessageResponse sendMessageResponse = session.sendTemplateMessage(template.getName(), template.getLanguage(), mobileNo, headerDocumentUrl, parameters, ackParameters);
        updatedRecord.setWhatsAppMessageId(sendMessageResponse.getMessages().get(0).getId());
        updatedRecord.setWhatsAppBodyContent(finalBodyContent);
        record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        return updatedRecord;
    }

    Map<String, String> buildParameterMap(
            final MTWhatsAppIndexRow indexRow,
            final List<MTWhatsAppIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            MTWhatsAppIndexField indexField = indexFields.get(i);
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

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        MTWhatsAppActivity targetActivity = mtWhatsAppActivityService.getById(targetJob.getActivityId()).get();

        // NOT manually purged before
        if (targetActivity.getPurgedTimestamp() == null) {

            for (MTWhatsAppIndexRow indexRow : targetActivity.getMtWhatsAppIndexRows()) {
                indexRow.setMtWhatsAppFile(null);
            }

            int totalFileSize = 0;
            MTWhatsAppType targetType = targetActivity.getMtWhatsAppType();
            for (MTWhatsAppFile targetFile : targetActivity.getMtWhatsAppFiles()) {
                totalFileSize += targetFile.getFileSize();
                fileStorageService.delete(
                        targetType.getAccount(),
                        FileObjectType.WHATSAPP,
                        targetActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            targetActivity.getMtWhatsAppFiles().clear();

            targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            targetActivity.setPurgedBy(CREATED_BY);
            mtWhatsAppActivityService.saveAndFlush(targetActivity);

            // update storage usage statistic
            updateStorageUsageStatisticForPurgedFileSize(targetJob.getAccount().getId(), totalFileSize);
        }
    }

    @Override
    void startPrepareInternal(final Job prepareJob) throws JobProcessingException {
        MTWhatsAppActivity targetActivity = (MTWhatsAppActivity) getBaseActivity(prepareJob.getActivityId());
        MTWhatsAppType targetType = targetActivity.getMtWhatsAppType();
        Account account = targetType.getAccount();

        try {
            baseActivitySftpService.process(account, targetType, targetType.getMtWhatsAppIndexFields(), targetActivity);
            mtWhatsAppActivityService.saveAndFlush(targetActivity);
        } catch (Exception e) {
            throw new JobProcessingException("Failed to prepare activity", e);
        }
    }

    @Override
    void endPrepareInternal(final Job prepareJob) throws JobProcessingException {
        MTWhatsAppActivity targetActivity = (MTWhatsAppActivity) getBaseActivity(prepareJob.getActivityId());

        Job job = new Job();
        job.setAccount(prepareJob.getAccount());
        job.setDomainType(prepareJob.getDomainType());
        job.setTypeId(prepareJob.getTypeId());
        job.setTypeName(prepareJob.getTypeName());
        job.setActivityId(prepareJob.getActivityId());
        job.setActivityName(prepareJob.getActivityName());
        job.setEventType(JobEventType.PROCESS);
        job.setStatus(JobStatus.NEW);
        job.setCreatedBy(prepareJob.getCreatedBy());
        job.setCreatedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        if (targetActivity.getScheduledTimestamp() != null) {
            job.setExecutionMode(JobExecutionMode.SCHEDULED);
            job.setScheduledExecutionTimestamp(targetActivity.getScheduledTimestamp());
        } else {
            job.setExecutionMode(JobExecutionMode.IMMEDIATE);
        }
        jobService.save(job);

        auditLogService.log(
                prepareJob.getAccount().getId(),
                Optional.of(prepareJob.getTypeId()),
                prepareJob.getActivityId(),
                prepareJob.getDomainType(),
                ActionType.JOB_CREATE,
                prepareJob.getActivityName(),
                CREATED_BY
        );
    }

    @Override
    BaseActivity getBaseActivity(final long id) {
        return mtWhatsAppActivityService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.MT_WHATSAPP;
    }

}
