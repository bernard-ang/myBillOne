package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author michaellow
 */
@Slf4j
public abstract class BaseController extends PaymentAwareController {

    long FILE_MAX_BYTES = 50000000L;        //  50 MB
    long ZIP_MAX_BYTES = 500000000L;       // 500 MB
    String ZIP = "zip";
    String BACK_SLASH = "/";
    String DASH = "-";
    String TEMP_DIR = System.getProperty("java.io.tmpdir");

    String CREATED_BY = "system";

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    JobService jobService;

    @Autowired
    AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    AccountSubscriptionService accountSubscriptionService;

    @Autowired
    PlanUsageService planUsageService;

    @Autowired
    EmbeddedLinkService embeddedLinkService;

    @Autowired
    EmbeddedLinkClickService embeddedLinkClickService;


    abstract DomainType getDomainType();


    Set<String> getUserCodes(final User user) {
        return user.getUserCodes().stream().map(UserCode::getCode).collect(Collectors.toSet());
    }

    @Transactional
    void verifyIfTypeCodeIsAllowed(final User user, final String targetCode) {
        Set<String> userCodes = getUserCodes(user);
        if (!userCodes.isEmpty() && !userCodes.contains(targetCode)) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB1029, "Type code is not allowed");
        }
    }

    void submitPrepareJobInternal(
            final Account account,
            final String username,
            final Long typeId,
            final String typeName,
            final Long activityId,
            final String activityName,
            final OffsetDateTime timestamp
    ) {
        Job job = new Job();
        job.setAccount(account);
        job.setDomainType(getDomainType());
        job.setTypeId(typeId);
        job.setTypeName(typeName);
        job.setActivityId(activityId);
        job.setActivityName(activityName);
        job.setExecutionMode(JobExecutionMode.IMMEDIATE);
        job.setEventType(JobEventType.PREPARE);
        job.setStatus(JobStatus.NEW);
        job.setCreatedBy(username);
        job.setCreatedTimestamp(timestamp);
        jobService.save(job);

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.JOB_CREATE,
                activityName,
                CREATED_BY
        );

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.ACTIVITY_SUBMIT,
                activityName,
                username
        );
    }

    void submitJobInternal(
            final Account account,
            final String username,
            final Long typeId,
            final String typeName,
            final Long activityId,
            final String activityName,
            final OffsetDateTime timestamp
    ) {
        Job job = new Job();
        job.setAccount(account);
        job.setDomainType(getDomainType());
        job.setTypeId(typeId);
        job.setTypeName(typeName);
        job.setActivityId(activityId);
        job.setActivityName(activityName);
        job.setExecutionMode(JobExecutionMode.IMMEDIATE);
        job.setEventType(JobEventType.PROCESS);
        job.setStatus(JobStatus.NEW);
        job.setCreatedBy(username);
        job.setCreatedTimestamp(timestamp);
        jobService.save(job);

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.JOB_CREATE,
                activityName,
                CREATED_BY
        );

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.ACTIVITY_SUBMIT,
                activityName,
                username
        );
    }

    void scheduleJobInternal(
            final Account account,
            final String username,
            final Long typeId,
            final String typeName,
            final Long activityId,
            final String activityName,
            final OffsetDateTime submittedTimestamp,
            final OffsetDateTime scheduledTimestamp
    ) {
        Job job = new Job();
        job.setAccount(account);
        job.setDomainType(getDomainType());
        job.setTypeId(typeId);
        job.setTypeName(typeName);
        job.setActivityId(activityId);
        job.setActivityName(activityName);
        job.setExecutionMode(JobExecutionMode.SCHEDULED);
        job.setEventType(JobEventType.PROCESS);
        job.setStatus(JobStatus.NEW);
        job.setScheduledExecutionTimestamp(scheduledTimestamp);
        job.setCreatedBy(username);
        job.setCreatedTimestamp(submittedTimestamp);
        jobService.save(job);

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.JOB_CREATE,
                activityName,
                CREATED_BY
        );

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.ACTIVITY_SCHEDULE,
                activityName,
                username
        );
    }

    String getEntryName(final ZipEntry entry) {
        String entryName = entry.getName();
        if (entryName.contains(BACK_SLASH)) {
            String[] entryNameSplit = entryName.split(BACK_SLASH);
            entryName = entryNameSplit[entryNameSplit.length - 1];
        }

        return entryName;
    }

    void verifyZipContent(
            final Account account,
            final AccountSubscription currentSubscription,
            final MultipartFile file,
            final GrabbillServerErrorCode errorCode
    ) throws IOException {
        // IMPORTANT: only able to read file size using ZipFile
        StringBuilder tempFilePath = new StringBuilder(TEMP_DIR)
                .append(BACK_SLASH).append(account.getId()).append(DASH).append(file.getName());
        try (OutputStream os = new FileOutputStream(tempFilePath.toString())) {
            os.write(file.getBytes());
        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0001, "", e);
        }

        boolean attachmentMaxSizeExceeded = false;
        String targetFileName = null;

        long totalFileSize = 0;
        ZipFile zipFile = new ZipFile(tempFilePath.toString());
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {

            ZipEntry entry = zipEntries.nextElement();
            if (!entry.isDirectory()) {
                String entryName = getEntryName(entry);
                if (entryName.startsWith(".") || !entryName.endsWith(".pdf")) {
                    log.info("File type of [" + entry.getName() + "] is not supported and skipped");

                } else {
                    if (DomainType.TRANSACTIONAL_EMAIL.equals(getDomainType())
                            && entry.getSize() > currentSubscription.getMaxAttachmentSize()) {
                        targetFileName = entryName;
                        attachmentMaxSizeExceeded = true;
                        break;
                    }

                    totalFileSize += entry.getSize();
                }
            }
        }
        new File(tempFilePath.toString()).delete();

        if (attachmentMaxSizeExceeded) {
            throw new GrabbillServerException(
                    errorCode,
                    "File [" + targetFileName + "] size exceeded " + currentSubscription.getMaxAttachmentSize() + " bytes."
            );
        }

        if (totalFileSize > ZIP_MAX_BYTES) {
            throw new GrabbillServerException(
                    errorCode,
                    "ZIP file size exceeded 500mb."
            );
        }

        if (planUsageService.isStorageUsageExceeded(account, totalFileSize)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1901,
                    "Plan's storage limit exceeded"
            );
        }
    }

    Item uploadFileByZipInputStreamInternal(
            final Account account,
            final Long activityId,
            final FileObjectType fileObjectType,
            final String entryName,
            final ZipInputStream zipInputStream
    ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] byteBuff = new byte[4096];
        int bytesRead;
        while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
            out.write(byteBuff, 0, bytesRead);
        }

        byte[] bytes = out.toByteArray();
        out.close();
        zipInputStream.closeEntry();

        fileStorageService.upload(
                account,
                fileObjectType,
                activityId.toString(),
                entryName,
                bytes
        );
        return fileStorageService.find(
                account,
                fileObjectType,
                activityId.toString(),
                entryName
        );
    }

    byte[] downloadFileInternal(
            final Account account,
            final String username,
            final Long typeId,
            final Long activityId,
            final String activityName,
            final String fileName,
            final FileObjectType fileObjectType
    ) {
        byte[] bytes = fileStorageService.download(
                account,
                fileObjectType,
                activityId.toString(),
                fileName
        );

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.ACTIVITY_FILE_DOWNLOAD,
                activityName + " > " + fileName,
                username
        );

        return bytes;
    }

    HttpHeaders createFileDownloadHttpHeaders(final BaseFile targetFile) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.parseMediaType(targetFile.getFileType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(targetFile.getName()).build());

        return headers;
    }

    HttpHeaders createReportDownloadHttpHeaders(
            final Long typeId,
            final int dataLength
    ) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report-" + typeId + ".xlsx\"");
        headers.setContentLength(dataLength);

        return headers;
    }

    ByteArrayOutputStream exportInternal(
            final Account account,
            final String username,
            final Long typeId,
            final Long activityId,
            final String activityName,
            final List<? extends BaseFile> files,
            final FileObjectType fileObjectType
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (BaseFile file : files) {
                byte[] bytes = fileStorageService.download(
                        account,
                        fileObjectType,
                        activityId.toString(),
                        file.getName()
                );
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                zos.write(bytes);
            }
            zos.closeEntry();
            zos.close();

        } catch (IOException e) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0001,
                    "File(s) export failed for activity [" + activityId + "]!"
            );
        }

        auditLogService.log(
                account.getId(),
                Optional.of(typeId),
                activityId,
                getDomainType(),
                ActionType.ACTIVITY_FILE_EXPORT,
                activityName,
                username
        );

        return baos;
    }

    HttpHeaders createFileExportHttpHeaders() {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentDisposition(ContentDisposition.attachment().filename("export.zip").build());

        return headers;
    }

    void updateStorageUsageStatistic(
            final Account account,
            final long totalFileSize
    ) {
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(account.getId());
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setTotalStorageUsed(targetUsageStatistic.getTotalStorageUsed() + totalFileSize);
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + account.getId() + "].");
        }
    }

    AccountSubscription getActiveSubscription(final Account account) {
        return accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId())
                .orElseThrow(
                        () -> new GrabbillServerException(
                                GrabbillServerErrorCode.GRB1020,
                                "No active subscription plan found!"
                        )
                );
    }

}
