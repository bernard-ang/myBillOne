package com.grabbill.engine.job.core;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.engine.job.JobProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class DigitalFilingJobProcessor extends AbstractJobProcessor {

    @Autowired
    @Qualifier("digitalFilingTypeService")
    private BaseTypeService<DigitalFilingType, DigitalFilingActivity> dftService;

    @Autowired
    @Qualifier("digitalFilingActivityService")
    private BaseActivityService<DigitalFilingType, DigitalFilingActivity> dfaService;

    @Autowired
    @Qualifier("digitalFilingIndexRowService")
    private BaseIndexRowService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexRow> dfirService;

    @Autowired
    @Qualifier("digitalFilingRecordService")
    private BaseRecordService<DigitalFilingRecord> dfrService;

    @Autowired
    @Qualifier("digitalFilingActivitySftpService")
    private BaseActivitySftpService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexField> baseActivitySftpService;


    @Override
    void processInternal(final Job targetJob) throws JobProcessingException {
        // mark activity as processing
        Integer accountId = targetJob.getAccount().getId();
        DigitalFilingActivity targetActivity = dfaService.markAsProcessing(targetJob.getActivityId());
        DigitalFilingType targetType = targetActivity.getDigitalFilingType();

        auditLogService.log(
                accountId,
                Optional.of(targetType.getId()),
                targetJob.getActivityId(),
                getSupportedActivityType(),
                ActionType.ACTIVITY_PROCESS,
                targetJob.getActivityName(),
                CREATED_BY
        );
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processing.");

        targetActivity = dfaService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getDigitalFilingType();
        Map<String, DigitalFilingFile> fileMap = targetActivity.getDigitalFilingFiles().stream()
                .collect(Collectors.toMap(DigitalFilingFile::getName, Function.identity()));

        long storageSize = 0;
        for (DigitalFilingIndexRow indexRow : targetActivity.getDigitalFilingIndexRows()) {
            DigitalFilingRecord record = indexRow.getDigitalFilingRecord();

            // IMPORTANT: skipping those index row with record (already processed)
            if (record == null) {
                record = new DigitalFilingRecord();
                record.setName(indexRow.getText2());
                record.setPriority(targetActivity.getPriority());
                record.setDigitalFilingActivity(targetActivity);
                if (fileMap.containsKey(indexRow.getText1())) {
                    DigitalFilingFile targetFile = fileMap.get(indexRow.getText1());
                    indexRow.setDigitalFilingFile(targetFile);
                    record.setStatus(ProcessStatus.COMPLETED);
                    storageSize += targetFile.getFileSize();

                } else {
                    record.setStatus(ProcessStatus.ERROR);
                    record.setMessage("File [" + indexRow.getText1() + "] does not exist!");
                }
                record.setDigitalFilingIndexRow(indexRow);
                DigitalFilingRecord savedRecord = dfrService.save(record);

                // link record to index row
                indexRow.setDigitalFilingType(targetType);
                indexRow.setDigitalFilingRecord(savedRecord);
                dfirService.save(indexRow);

            }
        }
        targetActivity.setStorageSize(storageSize);


        // mark activity as completed
        targetActivity.setStatus(ProcessStatus.COMPLETED);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        OffsetDateTime expectedPurgeTimestamp = targetType.isAutoPurge() ?
                targetJob.getCreatedTimestamp().plusDays(targetType.getAutoPurgeByDays()) : null;
        targetActivity.setExpectedPurgedTimestamp(expectedPurgeTimestamp);
        DigitalFilingActivity updatedActivity = dfaService.save(targetActivity);
        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processed. Total storage used is [" + updatedActivity.getStorageSize()
                + "] bytes.");

        // update type with last upload by and upload date
        DigitalFilingType updatedType = dftService.getByActivity(updatedActivity);
        updatedType.setLastUploadBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastUploadDate(updatedActivity.getLastModifiedDate());

        // update all index fields as hard referenced
        for (DigitalFilingIndexField targetIndexField : updatedType.getDigitalFilingIndexFields()) {
            targetIndexField.setHardRef(true);
        }

        dftService.save(updatedType);

        scheduleAutoPurgeJobIfSet(
                updatedType,
                updatedType.getId(),
                updatedActivity.getId(),
                updatedActivity.getName(),
                expectedPurgeTimestamp
        );
    }

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        DigitalFilingActivity targetActivity = dfaService.getById(targetJob.getActivityId()).get();

        // NOT manually purged before
        if (targetActivity.getPurgedTimestamp() == null) {

            for (DigitalFilingIndexRow indexRow : targetActivity.getDigitalFilingIndexRows()) {
                indexRow.setDigitalFilingFile(null);
            }

            int totalFileSize = 0;
            DigitalFilingType targetType = targetActivity.getDigitalFilingType();
            for (DigitalFilingFile targetFile : targetActivity.getDigitalFilingFiles()) {
                totalFileSize += targetFile.getFileSize();
                fileStorageService.delete(
                        targetType.getAccount(),
                        FileObjectType.DIGITAL_FILING,
                        targetActivity.getId().toString(),
                        targetFile.getName()
                );
            }
            targetActivity.getDigitalFilingFiles().clear();

            targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            targetActivity.setPurgedBy(CREATED_BY);
            dfaService.saveAndFlush(targetActivity);
            log.info("Activity [" + targetJob.getActivityId()
                    + "] of type [" + targetJob.getDomainType()
                    + "] is purged. Total storage sized removed is [" + totalFileSize
                    + "] bytes.");

            // update storage usage statistic
            updateStorageUsageStatisticForPurgedFileSize(targetJob.getAccount().getId(), totalFileSize);
        }
    }

    @Override
    void startPrepareInternal(final Job prepareJob) throws JobProcessingException {
        DigitalFilingActivity targetActivity = (DigitalFilingActivity) getBaseActivity(prepareJob.getActivityId());
        DigitalFilingType targetType = targetActivity.getDigitalFilingType();
        Account account = targetType.getAccount();

        try {
            baseActivitySftpService.process(account, targetType, targetType.getDigitalFilingIndexFields(), targetActivity);
            dfaService.saveAndFlush(targetActivity);
        } catch (Exception e) {
            throw new JobProcessingException("Failed to prepare activity", e);
        }
    }

    @Override
    void endPrepareInternal(final Job prepareJob) throws JobProcessingException {
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
        job.setExecutionMode(JobExecutionMode.IMMEDIATE);
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
        return dfaService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.DIGITAL_FILING;
    }

}
