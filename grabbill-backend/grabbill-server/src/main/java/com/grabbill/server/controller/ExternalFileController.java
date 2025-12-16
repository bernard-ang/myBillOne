package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.FileStorageService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
@RestController
@RequestMapping("/ext/files")
public class ExternalFileController {

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("mtWhatsAppActivityService")
    private BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    AuditLogService auditLogService;

    @Transactional
    @GetMapping(value = "tx/activities/{activityId}/files/{fileId}/{filename}")
    public ResponseEntity<ByteArrayResource> downloadTransactionalEmailFile(
            @PathVariable Long activityId,
            @PathVariable Long fileId
    ) {
        TransactionalEmailActivity transactionalEmailActivity = txeaService.getById(activityId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3003,
                        "Transactional email activity of id [" + activityId + "] not found"
                )
        );

        OffsetDateTime processedTimestamp = transactionalEmailActivity.getProcessedTimestamp();
        OffsetDateTime maximumAllowedTime = processedTimestamp == null ? OffsetDateTime.now().plusDays(1) : processedTimestamp.plusDays(1);

        if (OffsetDateTime.now().isAfter(maximumAllowedTime)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3014,
                    "Transactional email activity file of id [" + activityId + "] had been expired"
            );
        }

        TransactionalEmailFile targetFile = getTransactionalEmailFileInActivityById(transactionalEmailActivity, fileId);

        TransactionalEmailType transactionalEmailType = transactionalEmailActivity.getTransactionalEmailType();

        byte[] bytes = downloadFileInternal(
                transactionalEmailType.getAccount(),
                activityId,
                targetFile.getName(),
                FileObjectType.TRANSACTIONAL_EMAIL
        );


        if (transactionalEmailType.isPasswordProtected()) {
            Optional<TransactionalEmailIndexRow> indexRowOptional =
                    transactionalEmailActivity.getTransactionalEmailIndexRows().stream().filter(row -> row.getText2().equals(targetFile.getName())).findFirst();
            if (indexRowOptional.isPresent()) {
                String password = indexRowOptional.get().getText3();

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
                    PDDocument document = PDDocument.load(bytes);
                    document.protect(protectionPolicy);
                    document.save(baos);
                    bytes = baos.toByteArray();
                    document.close();

                } catch (IOException e) {
                    throw new GrabbillException("Failed to password protect [" + targetFile.getName() + "]: " + e.getMessage(), e);
                }
            }
        }

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
    }

    @Transactional
    @GetMapping(value = "wa/activities/{activityId}/files/{fileId}/{filename}")
    public ResponseEntity<ByteArrayResource> downloadWhatsAppFile(
            @PathVariable Long activityId,
            @PathVariable Long fileId
    ) {
        WhatsAppActivity activity = whatsAppActivityService.getById(activityId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3003,
                        "WhatsApp activity of id [" + activityId + "] not found"
                )
        );

        OffsetDateTime processedTimestamp = activity.getProcessedTimestamp();
        OffsetDateTime maximumAllowedTime = processedTimestamp == null ? OffsetDateTime.now().plusDays(1) : processedTimestamp.plusDays(1);

        if (OffsetDateTime.now().isAfter(maximumAllowedTime)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3014,
                    "WhatsApp activity file of id [" + activityId + "] had been expired"
            );
        }

        WhatsAppFile targetFile = getWhatsAppFileInActivityById(activity, fileId);

        WhatsAppType type = activity.getWhatsAppType();

        byte[] bytes = downloadFileInternal(
                type.getAccount(),
                activityId,
                targetFile.getName(),
                FileObjectType.WHATSAPP
        );

        if (type.isPasswordProtected()) {
            Optional<WhatsAppIndexRow> indexRowOptional =
                    activity.getWhatsAppIndexRows().stream().filter(row -> row.getText2().equals(targetFile.getName())).findFirst();
            if (indexRowOptional.isPresent()) {
                String password = indexRowOptional.get().getText3();

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
                    PDDocument document = PDDocument.load(bytes);
                    document.protect(protectionPolicy);
                    document.save(baos);
                    bytes = baos.toByteArray();
                    document.close();

                } catch (IOException e) {
                    throw new GrabbillException("Failed to password protect [" + targetFile.getName() + "]: " + e.getMessage(), e);
                }
            }
        }

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
    }

    @Transactional
    @GetMapping(value = "mtwa/activities/{activityId}/files/{fileId}/{filename}")
    public ResponseEntity<ByteArrayResource> downloadMtWhatsAppFile(
            @PathVariable Long activityId,
            @PathVariable Long fileId
    ) {
        MTWhatsAppActivity activity = mtWhatsAppActivityService.getById(activityId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3003,
                        "Multi-template WhatsApp activity of id [" + activityId + "] not found"
                )
        );

        OffsetDateTime processedTimestamp = activity.getProcessedTimestamp();
        OffsetDateTime maximumAllowedTime = processedTimestamp == null ? OffsetDateTime.now().plusDays(1) : processedTimestamp.plusDays(1);

        if (OffsetDateTime.now().isAfter(maximumAllowedTime)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3014,
                    "Multi-template WhatsApp activity file of id [" + activityId + "] had been expired"
            );
        }

        MTWhatsAppFile targetFile = getMtWhatsAppFileInActivityById(activity, fileId);

        MTWhatsAppType type = activity.getMtWhatsAppType();

        byte[] bytes = downloadFileInternal(
                type.getAccount(),
                activityId,
                targetFile.getName(),
                FileObjectType.WHATSAPP
        );

        if (type.isPasswordProtected()) {
            Optional<MTWhatsAppIndexRow> indexRowOptional =
                    activity.getMtWhatsAppIndexRows().stream().filter(row -> row.getText2().equals(targetFile.getName())).findFirst();
            if (indexRowOptional.isPresent()) {
                String password = indexRowOptional.get().getText3();

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
                    PDDocument document = PDDocument.load(bytes);
                    document.protect(protectionPolicy);
                    document.save(baos);
                    bytes = baos.toByteArray();
                    document.close();

                } catch (IOException e) {
                    throw new GrabbillException("Failed to password protect [" + targetFile.getName() + "]: " + e.getMessage(), e);
                }
            }
        }

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
    }

    private TransactionalEmailFile getTransactionalEmailFileInActivityById(
            final TransactionalEmailActivity targetActivity,
            final Long fileId
    ) {
        TransactionalEmailFile targetFile = null;
        for (TransactionalEmailFile transactionalEmailFile : targetActivity.getTransactionalEmailFiles()) {
            if (transactionalEmailFile.getId().equals(fileId)) {
                targetFile = transactionalEmailFile;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3008,
                    "No file with given id [" + fileId + "] found"
            );
        }
        return targetFile;
    }

    private WhatsAppFile getWhatsAppFileInActivityById(
            final WhatsAppActivity targetActivity,
            final Long fileId
    ) {
        WhatsAppFile targetFile = null;
        for (WhatsAppFile file : targetActivity.getWhatsAppFiles()) {
            if (file.getId().equals(fileId)) {
                targetFile = file;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3008,
                    "No file with given id [" + fileId + "] found"
            );
        }
        return targetFile;
    }

    private MTWhatsAppFile getMtWhatsAppFileInActivityById(
            final MTWhatsAppActivity targetActivity,
            final Long fileId
    ) {
        MTWhatsAppFile targetFile = null;
        for (MTWhatsAppFile file : targetActivity.getMtWhatsAppFiles()) {
            if (file.getId().equals(fileId)) {
                targetFile = file;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3008,
                    "No file with given id [" + fileId + "] found"
            );
        }
        return targetFile;
    }

    byte[] downloadFileInternal(
            final Account account,
            final Long activityId,
            final String fileName,
            final FileObjectType fileObjectType
    ) {

        return fileStorageService.download(
                account,
                fileObjectType,
                activityId.toString(),
                fileName
        );
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

}
