package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.ReportType;
import com.grabbill.core.model.sftp.SftpFolderValidationSummary;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.repository.MTWhatsAppFileRepository;
import com.grabbill.core.repository.MTWhatsAppIndexRowRepository;
import com.grabbill.core.service.*;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.MTWhatsAppActivityCreateRequest;
import com.grabbill.server.controller.request.MTWhatsAppActivityRequest;
import com.grabbill.server.controller.request.MTWhatsAppTypeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.MTWhatsAppReportService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/mt-whatsapp-types")
public class MTWhatsAppController extends BaseController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    @Qualifier("mtWhatsAppTypeService")
    private BaseTypeService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppTypeService;

    @Autowired
    @Qualifier("mtWhatsAppActivityService")
    private BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;

    @Autowired
    @Qualifier("mtWhatsAppFileService")
    private BaseFileService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppFile> mtWhatsAppFileService;

    @Autowired
    @Qualifier("mtWhatsAppIndexRowService")
    private BaseIndexRowService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexRow> mtWhatsAppIndexRowService;

    @Autowired
    @Qualifier("mtWhatsAppActivitySftpService")
    private BaseActivitySftpService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexField> baseActivitySftpService;

    @Autowired
    private MTWhatsAppReportService mtWhatsAppReportService;

    @Autowired
    private MTWhatsAppFileRepository mtWhatsAppFileRepository;

    @Autowired
    private MTWhatsAppIndexRowRepository mtWhatsAppIndexRowRepository;

    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getTypes(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User user = userDetails.getUser();
        Page<MTWhatsAppType> page = StringUtils.hasLength(name) ?
                mtWhatsAppTypeService.getAllByName(user, name, pageable) :
                mtWhatsAppTypeService.getAll(user, pageable);

        SearchResultPayload<MTWhatsAppTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<MTWhatsAppTypeBasicPayload>builder()
                        .items(page.get()
                                .map(mtWhatsAppType -> MTWhatsAppTypeBasicPayload.from(
                                        mtWhatsAppType,
                                        mtWhatsAppActivityService.getByTypeAndStatusIn(
                                                mtWhatsAppType,
                                                Collections.singletonList(ProcessStatus.COMPLETED)
                                        ),
                                        mtWhatsAppFileRepository)
                                )
                                .collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}")
    public ResponseEntity<GrabbillApiResponse> getType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId
    ) {
        MTWhatsAppType target = getMtWhatsAppType(userDetails.getUser(), typeId);
        List<MTWhatsAppActivity> targetActivities = mtWhatsAppActivityService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppTypePayload.from(
                                target,
                                targetActivities,
                                mtWhatsAppFileRepository,
                                mtWhatsAppIndexRowRepository
                        )
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody MTWhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTWhatsAppType newInstance = new MTWhatsAppType();
        request.to(newInstance);
        newInstance.setAccount(account);
        MTWhatsAppType savedInstance = mtWhatsAppTypeService.save(newInstance);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId(),
                getDomainType(),
                ActionType.TYPE_CREATE,
                savedInstance.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppTypePayload.from(savedInstance, Collections.emptyList(),
                                mtWhatsAppFileRepository,
                                mtWhatsAppIndexRowRepository
                        )
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody MTWhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = mtWhatsAppTypeService.findNextDuplicateName(request.getName());

        MTWhatsAppType newInstance = new MTWhatsAppType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        MTWhatsAppType savedInstance = mtWhatsAppTypeService.save(newInstance);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId(),
                getDomainType(),
                ActionType.TYPE_DUPLICATE,
                savedInstance.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppTypePayload.from(
                                savedInstance, Collections.emptyList(),
                                mtWhatsAppFileRepository, mtWhatsAppIndexRowRepository
                        )
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        Optional<MTWhatsAppType> typeOptional = mtWhatsAppTypeService.getByName(userDetails.getUser(), name);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        NameCheckPayload.from(typeOptional.isPresent())
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{typeId}")
    public ResponseEntity<GrabbillApiResponse> updateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody MTWhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        request.to(targetType);

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (!mtWhatsAppActivityService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (MTWhatsAppIndexField indexField : targetType.getMtWhatsAppIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        MTWhatsAppType updatedTargetType = mtWhatsAppTypeService.save(targetType);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                updatedTargetType.getId(),
                getDomainType(),
                ActionType.TYPE_UPDATE,
                updatedTargetType.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppTypePayload.from(
                                updatedTargetType,
                                mtWhatsAppActivityService.getByTypeAndStatusIn(
                                        updatedTargetType,
                                        Collections.singletonList(ProcessStatus.COMPLETED)
                                ),
                                mtWhatsAppFileRepository,
                                mtWhatsAppIndexRowRepository
                        )
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{typeId}")
    public ResponseEntity<GrabbillApiResponse> deleteType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId
    ) {
        User user = userDetails.getUser();
        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        if (!mtWhatsAppActivityService.getByType(targetType).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10002, "Multi-template WhatsApp Type is referenced by activity(s).");
        }

        if(targetType.getLastSentDate() != null) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10002, "Multi-template WhatsApp Type is in use.");
        }

        mtWhatsAppTypeService.delete(targetType);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                targetType.getId(),
                getDomainType(),
                ActionType.TYPE_DELETE,
                targetType.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Multi-template WhatsApp type with id [" + typeId + "] removed successfully.")
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> getActivities(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProcessStatus status,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MTWhatsAppType target = getMtWhatsAppType(userDetails.getUser(), typeId);
        Page<MTWhatsAppActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = mtWhatsAppActivityService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = mtWhatsAppActivityService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = mtWhatsAppActivityService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = mtWhatsAppActivityService.getByType(target, pageable);
        }

        SearchResultPayload<MTWhatsAppActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<MTWhatsAppActivityBasicPayload>builder()
                        .items(page.get().map(MTWhatsAppActivityBasicPayload::from).collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> getActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        MTWhatsAppType target = getMtWhatsAppType(userDetails.getUser(), typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, target);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppActivityPayload.from(targetActivity)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> newActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody MTWhatsAppActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity newActivityInstance = new MTWhatsAppActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setMtWhatsAppType(targetType);
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        MTWhatsAppActivity savedActivityInstance = mtWhatsAppActivityService.save(newActivityInstance);

        // mark all index fields as soft referenced
        updateAllIndexFieldsAsSoftReferenced(targetType);

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                savedActivityInstance.getId(),
                getDomainType(),
                ActionType.ACTIVITY_CREATE,
                savedActivityInstance.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        MTWhatsAppType targetType = getMtWhatsAppType(userDetails.getUser(), typeId);
        Optional<MTWhatsAppActivity> activityOptional = mtWhatsAppActivityService.getByNameAndType(name, targetType);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        NameCheckPayload.from(activityOptional.isPresent())
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities/{activityId}/validate-sftp")
    public ResponseEntity<GrabbillApiResponse> validateSftp(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @Valid @RequestBody MTWhatsAppActivityRequest request
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        targetActivity.setSftp(request.isSftp());
        targetActivity.setSftpPath(request.getSftpPath());

        if (request.isSftp()) {
            SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                    account, targetType, targetType.getMtWhatsAppIndexFields(), targetActivity);
            if (summary.hasError()) {
                return ResponseEntity.ok().body(
                        new GrabbillApiResponse(
                                GrabbillServerApiVersion.V1.getVersion(),
                                BaseActivitySftpFolderValidationSummaryPayload.from(summary)
                        )
                );
            }
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        BaseActivitySftpFolderValidationSummaryPayload.ok()
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> updateActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @Valid @RequestBody MTWhatsAppActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);

        if(!targetActivity.getStatus().equals(ProcessStatus.DRAFT)) {
            // only allow updating draft activity
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10006,
                    "Incorrect state for activity update"
            );
        }

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            MTWhatsAppActivity updatedInstance = mtWhatsAppActivityService.save(targetActivity);

            // mark all index fields as soft referenced
            updateAllIndexFieldsAsSoftReferenced(targetType);

            auditLogService.log(
                    account.getId(),
                    Optional.of(targetType.getId()),
                    updatedInstance.getId(),
                    getDomainType(),
                    ActionType.ACTIVITY_UPDATE,
                    updatedInstance.getName(),
                    userDetails.getUsername()
            );

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            MTWhatsAppActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {
            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
            request.to(targetActivity);

            if (targetActivity.isSftp()) {
                SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                        account, targetType, targetType.getMtWhatsAppIndexFields(), targetActivity);
                if (summary.hasError()) {
                    return ResponseEntity.badRequest().body(
                            new GrabbillApiResponse(
                                    GrabbillServerApiVersion.V1.getVersion(),
                                    BaseActivitySftpFolderValidationSummaryPayload.from(summary)
                            )
                    );
                }
                submitPrepareJobInternal(
                        account,
                        userDetails.getUsername(),
                        targetType.getId(),
                        targetType.getName(),
                        targetActivity.getId(),
                        targetActivity.getName(),
                        timestamp
                );

                return ResponseEntity.ok().body(
                        new GrabbillApiResponse(
                                GrabbillServerApiVersion.V1.getVersion(),
                                MTWhatsAppActivityPayload.from(targetActivity)
                        )
                );

            } else {

                targetActivity.setStatus(ProcessStatus.SUBMITTED);
                targetActivity.setSubmittedTimestamp(timestamp);
                targetActivity = mtWhatsAppActivityService.save(targetActivity);

                if (targetActivity.getScheduledTimestamp() != null) {
                    scheduleJobInternal(
                            account,
                            userDetails.getUsername(),
                            targetType.getId(),
                            targetType.getName(),
                            targetActivity.getId(),
                            targetActivity.getName(),
                            timestamp,
                            targetActivity.getScheduledTimestamp()
                    );

                } else {
                    submitJobInternal(
                            account,
                            userDetails.getUsername(),
                            targetType.getId(),
                            targetType.getName(),
                            targetActivity.getId(),
                            targetActivity.getName(),
                            timestamp
                    );
                }

                return ResponseEntity.ok().body(
                        new GrabbillApiResponse(
                                GrabbillServerApiVersion.V1.getVersion(),
                                MTWhatsAppActivityPayload.from(targetActivity)
                        )
                );
            }
        }

        throw new GrabbillServerException(
                GrabbillServerErrorCode.GRB10006,
                "Incorrect state for activity update - " + request.getStatus()
        );
    }

    @Transactional
    @DeleteMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> deleteActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10007, "Deletion only allowed for activity in draft status.");
        }

        long totalFileSize = 0;
        for (MTWhatsAppFile targetFile : targetActivity.getMtWhatsAppFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        mtWhatsAppActivityService.delete(targetActivity);

        // if there is no DRAFT activity for the type, set all new index fields added
        // which is not hard referenced yet to soft reference FALSE
        if (mtWhatsAppActivityService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (MTWhatsAppIndexField indexField : targetType.getMtWhatsAppIndexFields()) {
                // new index field added which is not hard referenced yet
                if (!indexField.isHardRef() && indexField.isSoftRef()) {
                    indexField.setSoftRef(false);
                }
            }
        }

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                targetActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_DELETE,
                targetActivity.getName(),
                userDetails.getUsername()
        );

        updateStorageUsageStatistic(account, totalFileSize);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Multi-template WhatsApp activity with id [" + activityId + "] removed successfully.")
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{typeId}/activities/{activityId}/purge/files")
    public ResponseEntity<GrabbillApiResponse> purgeFiles(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10004,
                    "Incorrect state for Multi-template WhatsApp activity of id [" + activityId + "] - file purging failed!"
            );
        }

        purgeAllFiles(userDetails, targetActivity);

        MTWhatsAppActivity mtWhatsAppActivity = getMtWhatsAppActivity(activityId, targetType);
        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                mtWhatsAppActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_PURGE,
                mtWhatsAppActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTWhatsAppActivityPayload.from(mtWhatsAppActivity)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities/{activityId}/files")
    public ResponseEntity<GrabbillApiResponse> uploadFile(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @RequestPart(name = "file") MultipartFile file
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        AccountSubscription currentSubscription = getActiveSubscription(account);

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10005,
                    "File upload failed for multi-template whatsapp activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        if (file.getContentType() != null && file.getContentType().endsWith(ZIP)) {

            try {
                verifyZipContent(account, currentSubscription, file, GrabbillServerErrorCode.GRB10005);

                ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(file.getBytes()));
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = getEntryName(entry);

                    // skipping directory entry and hidden files and none pdf file types
                    if (!entry.isDirectory() && !entryName.startsWith(".") && entryName.endsWith(".pdf")) {
                        Item item = uploadFileByZipInputStreamInternal(
                                account,
                                activityId,
                                FileObjectType.WHATSAPP,
                                entryName,
                                zipInputStream
                        );

                        Optional<MTWhatsAppFile> fileOptional = mtWhatsAppFileService.getByNameAndActivity(
                                entryName, targetActivity);
                        if (fileOptional.isPresent()) {
                            MTWhatsAppFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entryName);
                            fileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            fileInstance.setFileSize(item.size());
                            fileInstance.setMtWhatsAppType(targetType);
                            fileInstance.setMtWhatsAppActivity(targetActivity);
                            mtWhatsAppFileService.save(fileInstance);

                        } else {
                            MTWhatsAppFile newFileInstance = new MTWhatsAppFile();
                            newFileInstance.setName(entryName);
                            newFileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setMtWhatsAppType(targetType);
                            newFileInstance.setMtWhatsAppActivity(targetActivity);
                            mtWhatsAppFileService.save(newFileInstance);
                        }
                        totalFileSize += item.size();
                    }
                }
                zipInputStream.close();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for multi-template whatsapp activity of id [" + activityId + "]!"
                );
            }

        } else if (file.getContentType() != null && file.getContentType().endsWith("pdf")) {
            if (file.getSize() > FILE_MAX_BYTES) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10005,
                        "PDF file size exceeded 50mb (system limit)."
                );
            } else if (file.getSize() > currentSubscription.getMaxAttachmentSize()) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10005,
                        "PDF file size exceeded " + currentSubscription.getMaxAttachmentSize() + " bytes."
                );
            }

            if (planUsageService.isStorageUsageExceeded(account, file.getSize())) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1901,
                        "Plan's storage limit exceeded"
                );
            }

            try {
                fileStorageService.upload(
                        account,
                        FileObjectType.WHATSAPP,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.WHATSAPP,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename()
                );

                Optional<MTWhatsAppFile> fileOptional = mtWhatsAppFileService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    MTWhatsAppFile fileInstance = fileOptional.get();
                    totalFileSize -= fileInstance.getFileSize();
                    fileInstance.setName(file.getOriginalFilename());
                    fileInstance.setFileType(file.getContentType());
                    fileInstance.setFileSize(item.size());
                    fileInstance.setMtWhatsAppType(targetType);
                    fileInstance.setMtWhatsAppActivity(targetActivity);
                    mtWhatsAppFileService.save(fileInstance);

                } else {
                    MTWhatsAppFile newFileInstance = new MTWhatsAppFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setMtWhatsAppType(targetType);
                    newFileInstance.setMtWhatsAppActivity(targetActivity);
                    mtWhatsAppFileService.save(newFileInstance);
                }
                totalFileSize += item.size();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for Multi-template WhatsApp activity of id [" + activityId + "]!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10005,
                    "File type of [" + file.getContentType() + "] is not supported"
            );
        }

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                targetActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_FILE_UPLOAD,
                targetActivity.getName(),
                userDetails.getUsername()
        );

        updateStorageUsageStatistic(account, totalFileSize);

        List<MTWhatsAppFile> files = mtWhatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtWhatsAppFiles(files)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}/files/{fileId}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @PathVariable Long fileId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        MTWhatsAppFile targetFile = getFileInActivityById(targetActivity, fileId);

        byte[] bytes = downloadFileInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetFile.getName(),
                FileObjectType.WHATSAPP
        );

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}/export")
    public ResponseEntity<ByteArrayResource> export(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (targetActivity.getMtWhatsAppFiles().isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10009,
                    "No file for given activity with id [" + activityId + "]!"
            );
        }

        ByteArrayOutputStream baos = exportInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetActivity.getMtWhatsAppFiles(),
                FileObjectType.WHATSAPP
        );

        return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createFileExportHttpHeaders(), HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping(value = "/{typeId}/activities/{activityId}/files")
    public ResponseEntity<GrabbillApiResponse> deleteAllFiles(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10011,
                    "Incorrect state for Multi-template whatsapp activity of id [" + activityId + "] - files deletion failed!"
            );
        }

        long totalFileSize = 0;
        for (MTWhatsAppFile mtWhatsAppFile : targetActivity.getMtWhatsAppFiles()) {
            totalFileSize -= mtWhatsAppFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    mtWhatsAppFile.getName()
            );
        }
        targetActivity.getMtWhatsAppFiles().clear();
        mtWhatsAppActivityService.save(targetActivity);

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                targetActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_FILE_DELETE,
                targetActivity.getName(),
                userDetails.getUsername()
        );

        updateStorageUsageStatistic(account, totalFileSize);

        List<MTWhatsAppFile> files = mtWhatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtWhatsAppFiles(files)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{typeId}/activities/{activityId}/files/{fileId}")
    public ResponseEntity<GrabbillApiResponse> deleteFileById(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @PathVariable Long fileId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);
        MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10007,
                    "File deletion failed for Multi-template whatsapp activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        MTWhatsAppFile targetFile = null;
        for (MTWhatsAppFile file : targetActivity.getMtWhatsAppFiles()) {
            if (file.getId().equals(fileId)) {
                targetFile = file;
                totalFileSize -= file.getFileSize();
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10007,
                    "File with id [" + fileId + "] is not found."
            );
        }

        targetActivity.getMtWhatsAppFiles().remove(targetFile);
        for (MTWhatsAppIndexRow indexRow : targetActivity.getMtWhatsAppIndexRows()) {
            if (indexRow.getMtWhatsAppFile() != null
                    && indexRow.getMtWhatsAppFile().getId().equals(targetFile.getId())) {
                indexRow.setMtWhatsAppFile(null);
            }
        }
        mtWhatsAppActivityService.save(targetActivity);

        fileStorageService.delete(
                account,
                FileObjectType.WHATSAPP,
                targetActivity.getId().toString(),
                targetFile.getName()
        );

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                targetActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_FILE_DELETE,
                targetActivity.getName(),
                userDetails.getUsername()
        );

        updateStorageUsageStatistic(account, totalFileSize);

        List<MTWhatsAppFile> files = mtWhatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtWhatsAppFiles(files)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/files")
    public ResponseEntity<GrabbillApiResponse> getFilesByType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @RequestParam(required = false) Map<String, String> filters,
            @PageableDefault Pageable pageable
    ) {
        MTWhatsAppType targetType = getMtWhatsAppType(userDetails.getUser(), typeId);
        Page<MTWhatsAppIndexRow> page = mtWhatsAppIndexRowService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        SearchResultPayload<MTWhatsAppIndexRowPayload> searchResultPayload =
                SearchResultPayload.<MTWhatsAppIndexRowPayload>builder()
                        .items(page.get().map(MTWhatsAppIndexRowPayload::from).collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/records")
    public ResponseEntity<GrabbillApiResponse> getRecords(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @RequestParam(required = false) Map<String, String> filters,
            @PageableDefault Pageable pageable
    ) {
        MTWhatsAppType targetType = getMtWhatsAppType(userDetails.getUser(), typeId);
        Page<MTWhatsAppIndexRow> page = mtWhatsAppIndexRowService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<MTWhatsAppIndexRowPayload> searchResultPayload =
                SearchResultPayload.<MTWhatsAppIndexRowPayload>builder()
                        .items(page.get().map(MTWhatsAppIndexRowPayload::from).collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/reports")
    public ResponseEntity<ByteArrayResource> downloadReport(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @RequestParam List<String> activityIds,
            @RequestParam List<String> reportTypes,
            @RequestParam Boolean encryptAttachmentPassword,
            @RequestParam String tz
    ) {
        User user = userDetails.getUser();
        MTWhatsAppType targetType = getMtWhatsAppType(user, typeId);

        List<MTWhatsAppActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            MTWhatsAppActivity targetActivity = getMtWhatsAppActivity(Long.parseLong(activityId), targetType);

            if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB10013, "Activity [" + activityId + "] not in right state for report generation.");
            }

            targetActivities.add(targetActivity);
        }
        List<ReportType> targetReportTypes = new ArrayList<>();
        for (String reportType : reportTypes) {
            ReportType targetReportType = ReportType.from(reportType);
            if (targetReportType != null) {
                targetReportTypes.add(targetReportType);
            }
        }

        ZoneId zoneId = ZoneId.of(tz);
        Workbook workbook = mtWhatsAppReportService.generateWhatsAppReport(targetActivities, targetReportTypes, zoneId, encryptAttachmentPassword);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10013, "Failed to generate report for multi template whatsapp type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during multi template whatsapp report generation");
            }
        }
    }

    private MTWhatsAppType getMtWhatsAppType(final User user, final Long typeId) {
        MTWhatsAppType targetType = mtWhatsAppTypeService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10001,
                        "Multi-template WhatsApp type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private MTWhatsAppActivity getMtWhatsAppActivity(final Long activityId, final MTWhatsAppType mtWhatsAppType) {
        return mtWhatsAppActivityService.getByIdAndType(activityId, mtWhatsAppType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10003,
                        "Multi-template WhatsApp activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void purgeAllFiles(
            final GrabbillUserDetails userDetails,
            final MTWhatsAppActivity targetActivity
    ) {
        User user = userDetails.getUser();
        for (MTWhatsAppIndexRow indexRow : targetActivity.getMtWhatsAppIndexRows()) {
            indexRow.setMtWhatsAppFile(null);
        }
        long totalFileSize = 0;
        for (MTWhatsAppFile targetFile : targetActivity.getMtWhatsAppFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    user.getAccount(),
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        targetActivity.getMtWhatsAppFiles().clear();

        targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity.setPurgedBy(userDetails.getUsername());
        mtWhatsAppActivityService.saveAndFlush(targetActivity);

        updateStorageUsageStatistic(user.getAccount(), totalFileSize);
    }

    private void updateAllIndexFieldsAsSoftReferenced(final MTWhatsAppType targetType) {
        boolean typeChanged = false;
        for (MTWhatsAppIndexField indexField : targetType.getMtWhatsAppIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            mtWhatsAppTypeService.save(targetType);
        }
    }

    private MTWhatsAppFile getFileInActivityById(
            final MTWhatsAppActivity targetActivity,
            final Long fileId
    ) {
        MTWhatsAppFile targetFile = null;
        for (MTWhatsAppFile mtWhatsAppFile : targetActivity.getMtWhatsAppFiles()) {
            if (mtWhatsAppFile.getId().equals(fileId)) {
                targetFile = mtWhatsAppFile;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10008,
                    "No file with given id [" + fileId + "] found!"
            );
        }
        return targetFile;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.MT_WHATSAPP;
    }

}
