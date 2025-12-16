package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.ReportType;
import com.grabbill.core.model.sftp.SftpFolderValidationSummary;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.DigitalFilingActivityCreateRequest;
import com.grabbill.server.controller.request.DigitalFilingActivityRequest;
import com.grabbill.server.controller.request.DigitalFilingTypeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.DigitalFilingReportService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
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
import java.util.zip.ZipOutputStream;


/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/df-types")
public class DigitalFilingController extends BaseController {

    @Value("${digital-filing.download.zip.max-entry:1000}")
    private int maxEntriesPerZip;

    @Autowired
    @Qualifier("digitalFilingTypeService")
    private BaseTypeService<DigitalFilingType, DigitalFilingActivity> dftService;

    @Autowired
    @Qualifier("digitalFilingActivityService")
    private BaseActivityService<DigitalFilingType, DigitalFilingActivity> dfaService;

    @Autowired
    @Qualifier("digitalFilingFileService")
    private BaseFileService<DigitalFilingType, DigitalFilingActivity, DigitalFilingFile> dffService;

    @Autowired
    @Qualifier("digitalFilingIndexRowService")
    private BaseIndexRowService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexRow> dfirService;

    @Autowired
    @Qualifier("digitalFilingActivitySftpService")
    private BaseActivitySftpService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexField> baseActivitySftpService;

    @Autowired
    private DigitalFilingReportService digitalFilingReportService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getTypes(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User user = userDetails.getUser();
        Page<DigitalFilingType> page = StringUtils.hasLength(name) ?
                dftService.getAllByName(user, name, pageable) :
                dftService.getAll(userDetails.getUser(), pageable);

        SearchResultPayload<DigitalFilingTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<DigitalFilingTypeBasicPayload>builder()
                        .items(page.get()
                                .map(digitalFilingType -> DigitalFilingTypeBasicPayload.from(
                                        digitalFilingType,
                                        dfaService.getByTypeAndStatusIn(
                                                digitalFilingType,
                                                Collections.singletonList(ProcessStatus.COMPLETED)
                                        ))
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
        DigitalFilingType target = getDigitalFilingType(userDetails.getUser(), typeId);
        List<DigitalFilingActivity> targetActivities = dfaService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        DigitalFilingTypePayload.from(target, targetActivities)
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody DigitalFilingTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        DigitalFilingType newInstance = new DigitalFilingType();
        request.to(newInstance);
        newInstance.setAccount(account);
        DigitalFilingType savedInstance = dftService.save(newInstance);

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
                        DigitalFilingTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody DigitalFilingTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        DigitalFilingType newInstance = new DigitalFilingType();
        request.to(newInstance);
        newInstance.setName(dftService.findNextDuplicateName(request.getName()));
        newInstance.setAccount(account);
        DigitalFilingType savedInstance = dftService.save(newInstance);

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
                        DigitalFilingTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        NameCheckPayload.from(dftService.getByName(userDetails.getUser(), name).isPresent())
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{typeId}")
    public ResponseEntity<GrabbillApiResponse> updateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody DigitalFilingTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        request.to(targetType);

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (!dfaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (DigitalFilingIndexField indexField : targetType.getDigitalFilingIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        DigitalFilingType updatedTargetType = dftService.save(targetType);

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
                        DigitalFilingTypePayload.from(
                                updatedTargetType,
                                dfaService.getByTypeAndStatusIn(
                                        updatedTargetType,
                                        Collections.singletonList(ProcessStatus.COMPLETED)
                                )
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
        DigitalFilingType target = getDigitalFilingType(user, typeId);
        if (!dfaService.getByType(target).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB2002, "Type is referenced by activity(s).");
        }

        dftService.delete(target);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                target.getId(),
                getDomainType(),
                ActionType.TYPE_DELETE,
                target.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Digital filing type with id [" + typeId + "] removed successfully.")
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
        DigitalFilingType target = getDigitalFilingType(userDetails.getUser(), typeId);
        Page<DigitalFilingActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = dfaService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = dfaService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = dfaService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = dfaService.getByType(target, pageable);
        }

        SearchResultPayload<DigitalFilingActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<DigitalFilingActivityBasicPayload>builder()
                        .items(page.get().map(DigitalFilingActivityBasicPayload::from).collect(Collectors.toList()))
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
        DigitalFilingType target = getDigitalFilingType(userDetails.getUser(), typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, target);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        DigitalFilingActivityPayload.from(targetActivity)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> newActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody DigitalFilingActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity newActivityInstance = new DigitalFilingActivity();
        newActivityInstance.setDigitalFilingType(targetType);
        newActivityInstance.setName(request.getName());
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        DigitalFilingActivity savedActivityInstance = dfaService.save(newActivityInstance);

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
                        DigitalFilingActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        DigitalFilingType targetType = getDigitalFilingType(userDetails.getUser(), typeId);
        Optional<DigitalFilingActivity> activityOptional = dfaService.getByNameAndType(name, targetType);

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
            @Valid @RequestBody DigitalFilingActivityRequest request
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        targetActivity.setSftp(request.isSftp());
        targetActivity.setSftpPath(request.getSftpPath());

        if (request.isSftp()) {
            SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                    account, targetType, targetType.getDigitalFilingIndexFields(), targetActivity);
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
            @Valid @RequestBody DigitalFilingActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            DigitalFilingActivity updatedInstance = dfaService.save(targetActivity);

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
                            DigitalFilingActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {
            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
            request.to(targetActivity);

            if (targetActivity.isSftp()) {
                SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                        account, targetType, targetType.getDigitalFilingIndexFields(), targetActivity);
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
                                DigitalFilingActivityPayload.from(targetActivity)
                        )
                );

            } else {

                targetActivity.setStatus(ProcessStatus.SUBMITTED);
                targetActivity.setSubmittedTimestamp(timestamp);
                targetActivity = dfaService.save(targetActivity);

                submitJobInternal(
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
                                DigitalFilingActivityPayload.from(targetActivity)
                        )
                );
            }
        }

        throw new GrabbillServerException(
                GrabbillServerErrorCode.GRB2006,
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
        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB2007, "Deletion only allowed for activity in draft status.");
        }

        long totalFileSize = 0;
        for (DigitalFilingFile targetFile : targetActivity.getDigitalFilingFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.DIGITAL_FILING,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        dfaService.delete(targetActivity);

        // if there is no DRAFT activity for the type, set all new index fields added
        // which is not hard referenced yet to soft reference FALSE
        if (dfaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (DigitalFilingIndexField indexField : targetType.getDigitalFilingIndexFields()) {
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
                        new ApiMessage("Digital filing activity with id [" + activityId + "] removed successfully.")
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
        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2004,
                    "Incorrect state for Digital filing activity of id [" + activityId + "] - file purging failed!"
            );
        }

        purgeAllFiles(userDetails, targetActivity);

        DigitalFilingActivity digitalFilingActivity  = getDigitalFilingActivity(activityId, targetType);
        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                digitalFilingActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_PURGE,
                digitalFilingActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        DigitalFilingActivityPayload.from(digitalFilingActivity)
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

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2005,
                    "File upload failed for digital filing activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        if (file.getContentType() != null && file.getContentType().endsWith(ZIP)) {

            try {
                // email attachment file limit not appplicable for digital filing file(s)
                verifyZipContent(account, null, file, GrabbillServerErrorCode.GRB2005);

                ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(file.getBytes()));
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = getEntryName(entry);

                    // skipping directory entry and hidden files and none pdf file types
                    if (!entry.isDirectory() && !entryName.startsWith(".")) {
                        Item item = uploadFileByZipInputStreamInternal(
                                account,
                                activityId,
                                FileObjectType.DIGITAL_FILING,
                                entryName,
                                zipInputStream
                        );

                        Optional<DigitalFilingFile> fileOptional = dffService.getByNameAndActivity(
                                entryName, targetActivity);
                        if (fileOptional.isPresent()) {
                            DigitalFilingFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entryName);
                            fileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            fileInstance.setFileSize(item.size());
                            fileInstance.setDigitalFilingType(targetType);
                            fileInstance.setDigitalFilingActivity(targetActivity);
                            dffService.save(fileInstance);

                        } else {
                            DigitalFilingFile newFileInstance = new DigitalFilingFile();
                            newFileInstance.setName(entryName);
                            newFileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setDigitalFilingType(targetType);
                            newFileInstance.setDigitalFilingActivity(targetActivity);
                            dffService.save(newFileInstance);
                        }
                        totalFileSize += item.size();
                    }
                }
                zipInputStream.close();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for Digital filing activity of id [" + activityId + "]!"
                );
            }

        } else if (file.getContentType() != null) {
            if (file.getSize() > FILE_MAX_BYTES) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB2005,
                        "PDF file size exceeded 20mb."
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
                        FileObjectType.DIGITAL_FILING,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.DIGITAL_FILING,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename()
                );

                Optional<DigitalFilingFile> fileOptional = dffService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    DigitalFilingFile fileInstance = fileOptional.get();
                    totalFileSize -= fileInstance.getFileSize();
                    fileInstance.setName(file.getOriginalFilename());
                    fileInstance.setFileType(file.getContentType());
                    fileInstance.setFileSize(item.size());
                    fileInstance.setDigitalFilingType(targetType);
                    fileInstance.setDigitalFilingActivity(targetActivity);
                    dffService.save(fileInstance);

                } else {
                    DigitalFilingFile newFileInstance = new DigitalFilingFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setDigitalFilingType(targetType);
                    newFileInstance.setDigitalFilingActivity(targetActivity);
                    dffService.save(newFileInstance);
                }
                totalFileSize += item.size();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for Digital filing activity of id [" + activityId + "]!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2005,
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

        List<DigitalFilingFile> files = dffService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromDigitalFilingFiles(files)
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

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        DigitalFilingFile targetFile = getFileInActivityById(targetActivity, fileId);

        byte[] bytes = downloadFileInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetFile.getName(),
                FileObjectType.DIGITAL_FILING
        );

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
    }

    // StreamingResponseBody
    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}/export")
    public ResponseEntity<ByteArrayResource> export(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (targetActivity.getDigitalFilingFiles().isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2009,
                    "No file for given activity with id [" + activityId + "]!"
            );
        }

        ByteArrayOutputStream baos = exportInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetActivity.getDigitalFilingFiles(),
                FileObjectType.DIGITAL_FILING
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

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2011,
                    "Incorrect state for Digital filing activity of id [" + activityId + "] - files deletion failed!"
            );
        }

        long totalFileSize = 0;
        for (DigitalFilingFile digitalFilingFile : targetActivity.getDigitalFilingFiles()) {
            totalFileSize -= digitalFilingFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.DIGITAL_FILING,
                    targetActivity.getId().toString(),
                    digitalFilingFile.getName()
            );
        }
        targetActivity.getDigitalFilingFiles().clear();
        dfaService.save(targetActivity);

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

        List<DigitalFilingFile> files = dffService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromDigitalFilingFiles(files)
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

        DigitalFilingType targetType = getDigitalFilingType(user, typeId);
        DigitalFilingActivity targetActivity = getDigitalFilingActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2007,
                    "File deletion failed for digital filing activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        DigitalFilingFile targetFile = null;
        for (DigitalFilingFile file : targetActivity.getDigitalFilingFiles()) {
            if (file.getId().equals(fileId)) {
                targetFile = file;
                totalFileSize -= file.getFileSize();
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2007,
                    "File with id [" + fileId + "] is not found."
            );
        }

        targetActivity.getDigitalFilingFiles().remove(targetFile);
        for (DigitalFilingIndexRow indexRow : targetActivity.getDigitalFilingIndexRows()) {
            if (indexRow.getDigitalFilingFile() != null
                    && indexRow.getDigitalFilingFile().getId().equals(targetFile.getId())) {
                indexRow.setDigitalFilingFile(null);
            }
        }
        dfaService.save(targetActivity);

        fileStorageService.delete(
                account,
                FileObjectType.DIGITAL_FILING,
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

        List<DigitalFilingFile> files = dffService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromDigitalFilingFiles(files)
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
        DigitalFilingType targetType = getDigitalFilingType(userDetails.getUser(), typeId);
        Page<DigitalFilingIndexRow> page = dfirService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        SearchResultPayload<DigitalFilingIndexRowPayload> searchResultPayload =
                SearchResultPayload.<DigitalFilingIndexRowPayload>builder()
                        .items(page.get().map(DigitalFilingIndexRowPayload::from).collect(Collectors.toList()))
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
    @GetMapping(value = "/{typeId}/files", params = { "download=1" } )
    public ResponseEntity<ByteArrayResource> downloadFilesByType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @RequestParam(required = false) Map<String, String> filters
    ) throws IOException {
        Pageable pageable = PageRequest.of(0, maxEntriesPerZip);
        User user = userDetails.getUser();
        Account account = user.getAccount();

        DigitalFilingType targetType = getDigitalFilingType(userDetails.getUser(), typeId);
        Page<DigitalFilingIndexRow> page = dfirService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        List<DigitalFilingIndexRow> indexRows = page.get().collect(Collectors.toList());
        for (DigitalFilingIndexRow indexRow : indexRows) {

            DigitalFilingActivity activity = indexRow.getDigitalFilingActivity();
            DigitalFilingFile file = indexRow.getDigitalFilingFile();
            byte[] bytes = downloadFileInternal(
                    account,
                    userDetails.getUsername(),
                    targetType.getId(),
                    activity.getId(),
                    activity.getName(),
                    file.getName(),
                    FileObjectType.DIGITAL_FILING
            );

            ZipEntry entry = new ZipEntry(file.getName());
            entry.setSize(bytes.length);
            zos.putNextEntry(entry);
            zos.write(bytes);
            zos.closeEntry();
        }
        zos.close();

        HttpHeaders headers = new HttpHeaders();
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("download.zip").build());

        return new ResponseEntity<>(
                new ByteArrayResource(baos.toByteArray()),
                headers,
                HttpStatus.OK
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
        DigitalFilingType targetType = getDigitalFilingType(userDetails.getUser(), typeId);
        Page<DigitalFilingIndexRow> page = dfirService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<DigitalFilingIndexRowPayload> searchResultPayload =
                SearchResultPayload.<DigitalFilingIndexRowPayload>builder()
                        .items(page.get().map(DigitalFilingIndexRowPayload::from).collect(Collectors.toList()))
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
            @RequestParam String tz
    ) {
        User user = userDetails.getUser();
        DigitalFilingType targetType = getDigitalFilingType(user, typeId);

        List<DigitalFilingActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            DigitalFilingActivity targetActivity = getDigitalFilingActivity(Long.parseLong(activityId), targetType);

            if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB3013, "Activity [" + activityId + "] not in right state for report generation.");
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
        Workbook workbook = digitalFilingReportService.generateDigitalFilingReport(targetActivities, targetReportTypes, zoneId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB2012, "Failed to generate report for Digital Filing type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during digital filing email report generation");
            }
        }
    }

    private DigitalFilingType getDigitalFilingType(
            final User user,
            final Long typeId
    ) {
        DigitalFilingType targetType = dftService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB2001,
                        "Digital filing type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private DigitalFilingActivity getDigitalFilingActivity(
            final Long activityId,
            final DigitalFilingType digitalFilingType
    ) {
        return dfaService.getByIdAndType(activityId, digitalFilingType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB2003,
                        "Digital filing activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void purgeAllFiles(
            final GrabbillUserDetails userDetails,
            final DigitalFilingActivity targetActivity
    ) {
        for (DigitalFilingIndexRow indexRow : targetActivity.getDigitalFilingIndexRows()) {
            indexRow.setDigitalFilingFile(null);
        }
        long totalFileSize = 0;
        for (DigitalFilingFile targetFile : targetActivity.getDigitalFilingFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    userDetails.getUser().getAccount(),
                    FileObjectType.DIGITAL_FILING,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        targetActivity.getDigitalFilingFiles().clear();

        targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity.setPurgedBy(userDetails.getUsername());
        dfaService.saveAndFlush(targetActivity);

        updateStorageUsageStatistic(userDetails.getUser().getAccount(), totalFileSize);
    }

    private void updateAllIndexFieldsAsSoftReferenced(final DigitalFilingType targetType) {
        boolean typeChanged = false;
        for (DigitalFilingIndexField indexField : targetType.getDigitalFilingIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            dftService.save(targetType);
        }
    }

    private DigitalFilingFile getFileInActivityById(
            final DigitalFilingActivity targetActivity,
            final Long fileId
    ) {
        DigitalFilingFile targetFile = null;
        for (DigitalFilingFile digitalFilingFile : targetActivity.getDigitalFilingFiles()) {
            if (digitalFilingFile.getId().equals(fileId)) {
                targetFile = digitalFilingFile;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB2008,
                    "No file with given id [" + fileId + "] found!"
            );
        }
        return targetFile;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.DIGITAL_FILING;
    }

}
