package com.grabbill.engine.job.core;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponseComponent;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.BaseIndexRowService;
import com.grabbill.core.service.BaseTypeService;
import com.grabbill.core.service.whatsapp.WhatsAppRecordService;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author seez
 */
@Slf4j
@Transactional
public class WhatsAppJobProcessor extends AbstractJobProcessor {

    @Value("${file.external.url}")
    private String FILE_EXTERNAL_URL;

    @Autowired
    @Qualifier("whatsAppTypeService")
    private BaseTypeService<WhatsAppType, WhatsAppActivity> whatsAppTypeService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("whatsAppIndexRowService")
    private BaseIndexRowService<WhatsAppType, WhatsAppActivity, WhatsAppIndexRow> whatsAppIndexRowService;

    @Autowired
    @Qualifier("whatsAppRecordService")
    WhatsAppRecordService whatsAppRecordService;

    @Autowired
    private WhatsAppService whatsAppService;


    @Override
    void processInternal(final Job targetJob) throws JobProcessingException {
        // mark activity as processing
        WhatsAppActivity targetActivity = whatsAppActivityService.markAsProcessing(targetJob.getActivityId());
        WhatsAppType targetType = targetActivity.getWhatsAppType();
        logProcessing(targetJob, targetType.getId());


        targetActivity = whatsAppActivityService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getWhatsAppType();

        Map<Long, Exception> indexRowIdToExceptionMap;
        indexRowIdToExceptionMap = processIndexRowsInternal(targetType, targetActivity);


        // mark activity as processed completely
        targetActivity.setStatus(ProcessStatus.COMPLETED);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        WhatsAppActivity updatedActivity = whatsAppActivityService.save(targetActivity);


        // update activity statistics
        int sent = 0;
        for (WhatsAppIndexRow indexRow : updatedActivity.getWhatsAppIndexRows()) {

            WhatsAppRecord record = indexRow.getWhatsAppRecord();

            // update record(s) with sent error
            if (!indexRowIdToExceptionMap.isEmpty() && indexRowIdToExceptionMap.containsKey(indexRow.getId())) {
                Exception exception = indexRowIdToExceptionMap.get(indexRow.getId());
                record.setMessage((exception.getMessage().length() < 255) ?
                        exception.getMessage() : exception.getMessage().substring(0, 255));
                record.setStatus(ProcessStatus.ERROR);
                record.setWhatsAppStatusFailedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                record.setWhatsAppStatusFailed(true);
                targetActivity.setWhatsAppStatusFailed(targetActivity.getWhatsAppStatusFailed() + 1);

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                String message = stringWriter.toString();
                log.warn(message);
            }

            // accumulate statistical data
            if(record.getWhatsAppStatusSent()) {
                sent++;
            }
        }

        // update statistic data
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processed.");

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
        WhatsAppType updatedType = whatsAppTypeService.getByActivity(updatedActivity);
        updatedType.setLastSentBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastSentDate(updatedActivity.getLastModifiedDate());
        for (WhatsAppIndexField targetIndexField : updatedType.getWhatsAppIndexFields()) {
            targetIndexField.setHardRef(true);
        }
        whatsAppTypeService.save(updatedType);
    }

    private Map<Long, Exception> processIndexRowsInternal(
            final WhatsAppType targetType,
            final WhatsAppActivity targetActivity
    ) throws JobProcessingException {

        Account account = targetType.getAccount();
        Map<String, WhatsAppFile> fileMap = targetActivity.getWhatsAppFiles().stream()
                .collect(Collectors.toMap(WhatsAppFile::getName, Function.identity()));
        Map<Long, Exception> indexRowIdToExceptionMap = new HashMap<>();

        WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
        String whatsappTemplateName = targetType.getWhatsAppTemplateName();
        List<RefreshTemplateResponse> templates = session.refreshTemplate();
        Optional<RefreshTemplateResponse> template = templates.stream().filter(item -> item.getName().equalsIgnoreCase(whatsappTemplateName)).findAny();

        if (template.isPresent()) {
            RefreshTemplateResponse currentTemplate = template.get();
            targetActivity.setWhatsAppTemplateName(currentTemplate.getName());
            targetActivity.setWhatsAppBodyContent(currentTemplate.getBodyText());
            targetActivity.setWhatsAppDocument(currentTemplate.getHeaderComponent().isPresent());
            targetActivity.setWhatsAppFooterContent(currentTemplate.getFooterText());
            targetActivity.setWhatsAppButton(currentTemplate.getButtonText());
        }

        long storageSize = 0;
        for (WhatsAppIndexRow indexRow : targetActivity.getWhatsAppIndexRows()) {
            String mobileNo = indexRow.getText1();
            String filename = indexRow.getText2();
            String password = indexRow.getText3();

            WhatsAppRecord record = indexRow.getWhatsAppRecord();

            // construct email parameter map, perform value interpolation on email content
            Map<String, String> parameterMap = buildParameterMap(indexRow, targetType.getWhatsAppIndexFields());

            // IMPORTANT: skipping those index row with record (already processed)
            if (record == null) {
                record = new WhatsAppRecord();
                record.setName(mobileNo);
                record.setPriority(targetActivity.getPriority());
                record.setWhatsAppActivity(targetActivity);
                record.setWhatsAppIndexRow(indexRow);

                try {
                    if (targetType.isHasAttachment()) {
                        if (fileMap.containsKey(filename)) {

                            WhatsAppFile file = fileMap.get(filename);
                            byte[] fileBytes = fileStorageService.download(
                                    targetType.getAccount(),
                                    FileObjectType.WHATSAPP,
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
                            
                            indexRow.setWhatsAppFile(file);

                            // file not found
                        } else {
                            record.setMessage("File [" + filename + "] does not exist!");
                            throw new GrabbillException(record.getMessage());
                        }
                    }
                        
                    record = processWhatsAppMessage(indexRow, record, targetActivity, targetType, parameterMap, fileMap, template.orElse(null));

                    // done processing record
                    record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    record.setStatus(ProcessStatus.COMPLETED);

                } catch (Exception e) {
                    log.error("Unknown error while processing index row", e);
                    record.setWhatsAppStatusFailedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    record.setWhatsAppStatusFailed(true);
                    record.setStatus(ProcessStatus.ERROR);
                    record.setMessage((e.getMessage().length() < 255) ?
                            e.getMessage() : e.getMessage().substring(0, 255));
                    targetActivity.setWhatsAppStatusFailed(targetActivity.getWhatsAppStatusFailed() + 1);
                }


                // link record to index row
                WhatsAppRecord savedRecord = whatsAppRecordService.save(record);
                indexRow.setWhatsAppType(targetType);
                indexRow.setWhatsAppRecord(savedRecord);
                whatsAppIndexRowService.save(indexRow);
            }
        }

        targetActivity.setStorageSize(storageSize);

        return indexRowIdToExceptionMap;
    }

    private WhatsAppRecord processWhatsAppMessage(
            WhatsAppIndexRow indexRow,
            WhatsAppRecord record,
            WhatsAppActivity targetActivity,
            WhatsAppType targetType,
            Map<String, String> parameterMap,
            Map<String, WhatsAppFile> fileMap,
            RefreshTemplateResponse template) {

        // TODO: error handling

        if (template == null) {
            targetActivity.setWhatsAppStatusSkip(targetActivity.getWhatsAppStatusSkip() + 1);
            record.setWhatsAppStatusSkip(true);
            record.setStatus(ProcessStatus.ERROR);
            record.setWhatsAppStatusSkipReason("WhatsApp template not found");
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
        for (WhatsappTemplateParam param : targetType.getWhatsappTemplateParams()) {
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

        WhatsAppRecord updatedRecord = whatsAppRecordService.save(record);

        // Construct header content
        Optional<RefreshTemplateResponseComponent> headerComponentOptional = template.getHeaderComponent();

        String headerDocumentUrl = null;
        String filename = indexRow.getText2();

        if (headerComponentOptional.isPresent() && fileMap.containsKey(filename)) {
            WhatsAppFile whatsAppFile = fileMap.get(filename);
            headerDocumentUrl = FILE_EXTERNAL_URL
                    .replace("{domain}", "wa")
                    .replace("{activityId}", targetActivity.getId().toString())
                    .replace("{fileId}", whatsAppFile.getId().toString())
                    .replace("{filename}", whatsAppFile.getName());
        }

        Optional<RefreshTemplateResponseComponent> buttonComponentOptional = template.getButtonsComponent();
        List<String> ackParameters = null;
        if (buttonComponentOptional.isPresent()) {
            ackParameters = new ArrayList<>();
            ackParameters.add("wa-" + updatedRecord.getId().toString());
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

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        WhatsAppActivity targetActivity = whatsAppActivityService.getById(targetJob.getActivityId()).get();

        // NOT manually purged before
        if (targetActivity.getPurgedTimestamp() == null) {

            for (WhatsAppIndexRow indexRow : targetActivity.getWhatsAppIndexRows()) {
                indexRow.setWhatsAppFile(null);
            }

            int totalFileSize = 0;
            WhatsAppType targetType = targetActivity.getWhatsAppType();
            for (WhatsAppFile targetFile : targetActivity.getWhatsAppFiles()) {
                totalFileSize += targetFile.getFileSize();
                fileStorageService.delete(
                        targetType.getAccount(),
                        FileObjectType.TRANSACTIONAL_EMAIL,
                        targetActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            targetActivity.getWhatsAppFiles().clear();

            targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            targetActivity.setPurgedBy(CREATED_BY);
            whatsAppActivityService.saveAndFlush(targetActivity);

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
        return whatsAppActivityService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.WHATSAPP;
    }

}
