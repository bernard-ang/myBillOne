package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.ReportType;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.TransactionalEmailActivityCreateRequest;
import com.grabbill.server.controller.request.TransactionalEmailActivityRequest;
import com.grabbill.server.controller.request.TransactionalEmailTypeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.TransactionalEmailReportService;
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
@RequestMapping("/txe-types")
public class TransactionalEmailController extends BaseController {

    @Autowired
    @Qualifier("transactionalEmailTypeService")
    private BaseTypeService<TransactionalEmailType, TransactionalEmailActivity> txetService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("transactionalEmailFileService")
    private BaseFileService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailFile> txefService;

    @Autowired
    @Qualifier("transactionalEmailIndexRowService")
    private BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> txeirService;

    @Autowired
    private TransactionalEmailReportService transactionalEmailReportService;

    @Autowired
    private UnsubscribedEmailService unsubscribedEmailService;

    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getTypes(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User user = userDetails.getUser();
        Page<TransactionalEmailType> page = StringUtils.hasLength(name) ?
                txetService.getAllByName(user, name, pageable) :
                txetService.getAll(user, pageable);

        SearchResultPayload<TransactionalEmailTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<TransactionalEmailTypeBasicPayload>builder()
                        .items(page.get()
                                .map(transactionalEmailType -> TransactionalEmailTypeBasicPayload.from(
                                        transactionalEmailType,
                                        txeaService.getByTypeAndStatusIn(
                                                transactionalEmailType,
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
        TransactionalEmailType target = getTransactionalEmailType(userDetails.getUser(), typeId);
        List<TransactionalEmailActivity> targetActivities = txeaService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        TransactionalEmailTypePayload.from(
                                target,
                                targetActivities,
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.TRANSACTIONAL_EMAIL,
                                        typeId
                                )
                        )
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody TransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        TransactionalEmailType newInstance = new TransactionalEmailType();
        request.to(newInstance);
        newInstance.setAccount(account);
        TransactionalEmailType savedInstance = txetService.save(newInstance);

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
                        TransactionalEmailTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody TransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = txetService.findNextDuplicateName(request.getName());

        TransactionalEmailType newInstance = new TransactionalEmailType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        TransactionalEmailType savedInstance = txetService.save(newInstance);

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
                        TransactionalEmailTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        Optional<TransactionalEmailType> typeOptional = txetService.getByName(userDetails.getUser(), name);

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
            @Valid @RequestBody TransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        request.to(targetType);

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (!txeaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (TransactionalEmailIndexField indexField : targetType.getTransactionalEmailIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        TransactionalEmailType updatedTargetType = txetService.save(targetType);

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
                        TransactionalEmailTypePayload.from(
                                updatedTargetType,
                                txeaService.getByTypeAndStatusIn(
                                        updatedTargetType,
                                        Collections.singletonList(ProcessStatus.COMPLETED)
                                ),
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.TRANSACTIONAL_EMAIL,
                                        typeId
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
        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        if (!txeaService.getByType(targetType).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3002, "Type is referenced by activity(s).");
        }

        txetService.delete(targetType);

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
                        new ApiMessage("Transactional email type with id [" + typeId + "] removed successfully.")
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
        TransactionalEmailType target = getTransactionalEmailType(userDetails.getUser(), typeId);
        Page<TransactionalEmailActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = txeaService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = txeaService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = txeaService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = txeaService.getByType(target, pageable);
        }


        SearchResultPayload<TransactionalEmailActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<TransactionalEmailActivityBasicPayload>builder()
                        .items(page.get().map(TransactionalEmailActivityBasicPayload::from).collect(Collectors.toList()))
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
        TransactionalEmailType target = getTransactionalEmailType(userDetails.getUser(), typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, target);

        List<EmbeddedLink> embeddedLinks = embeddedLinkService.getByDomainTypeAndActivityIdIs(getDomainType(), activityId);

        List<EmbeddedLinkPayload> embeddedLinkPayloads = new ArrayList<>();
        for (EmbeddedLink embeddedLink : embeddedLinks) {
            embeddedLinkPayloads.add(
                    EmbeddedLinkPayload.from(
                            embeddedLink,
                            embeddedLinkClickService.countTotalClicksByEmbeddedLinkId(embeddedLink.getId()),
                            embeddedLinkClickService.countUniqueClicksByEmbeddedLinkId(embeddedLink.getId())
                    )
            );
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        TransactionalEmailActivityPayload.from(
                                targetActivity,
                                embeddedLinkPayloads,
                                unsubscribedEmailService.totalUnsubscribedEmailByActivity(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.TRANSACTIONAL_EMAIL,
                                        typeId,
                                        activityId)
                        )
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> newActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody TransactionalEmailActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity newActivityInstance = new TransactionalEmailActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setEmailFrom(targetType.getEmailFrom());
        newActivityInstance.setEmailFromName(targetType.getEmailFromName());
        newActivityInstance.setEmailContent(targetType.getEmailContent());
        newActivityInstance.setEmailMjmlContent(targetType.getEmailMjmlContent());
        newActivityInstance.setEmailSubject(targetType.getEmailSubject());
        newActivityInstance.setTransactionalEmailType(targetType);
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        TransactionalEmailActivity savedActivityInstance = txeaService.save(newActivityInstance);

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
                        TransactionalEmailActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        TransactionalEmailType targetType = getTransactionalEmailType(userDetails.getUser(), typeId);
        Optional<TransactionalEmailActivity> activityOptional = txeaService.getByNameAndType(name, targetType);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        NameCheckPayload.from(activityOptional.isPresent())
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> updateActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @Valid @RequestBody TransactionalEmailActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            TransactionalEmailActivity updatedInstance = txeaService.save(targetActivity);

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
                            TransactionalEmailActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {
            long additionalEmailCount = targetActivity.getTransactionalEmailIndexRows().size();
            if (planUsageService.isTransactionalEmailUsageExceeded(account, additionalEmailCount)) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1902,
                        "Plan's email size limit exceeded"
                );
            }

            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

            request.to(targetActivity);
            targetActivity.setStatus(ProcessStatus.SUBMITTED);
            targetActivity.setSubmittedTimestamp(timestamp);
            targetActivity = txeaService.save(targetActivity);

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
                            TransactionalEmailActivityPayload.from(targetActivity)
                    )
            );
        }

        throw new GrabbillServerException(
                GrabbillServerErrorCode.GRB3006,
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3007, "Deletion only allowed for activity in draft status.");
        }

        long totalFileSize = 0;
        for (TransactionalEmailFile targetFile : targetActivity.getTransactionalEmailFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        txeaService.delete(targetActivity);

        // if there is no DRAFT activity for the type, set all new index fields added
        // which is not hard referenced yet to soft reference FALSE
        if (txeaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (TransactionalEmailIndexField indexField : targetType.getTransactionalEmailIndexFields()) {
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
                        new ApiMessage("Transactional email activity with id [" + activityId + "] removed successfully.")
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3004,
                    "Incorrect state for Transactional email activity of id [" + activityId + "] - file purging failed!"
            );
        }

        purgeAllFiles(userDetails, targetActivity);

        TransactionalEmailActivity transactionalEmailActivity = getTransactionalEmailActivity(activityId, targetType);
        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                transactionalEmailActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_PURGE,
                transactionalEmailActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        TransactionalEmailActivityPayload.from(transactionalEmailActivity)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}/embedded-links/{linkId}/click-summary")
    public ResponseEntity<GrabbillApiResponse> getTotalEmbeddedLinkClicksCount(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @PathVariable Long linkId
    ) {
        TransactionalEmailType targetType = getTransactionalEmailType(userDetails.getUser(), typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        EmbeddedLink targetEmbeddedLink = embeddedLinkService.getByDomainTypeAndTypeIdAndActivityIdAndId(
                getDomainType(), targetType.getId(), targetActivity.getId(), linkId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1031,
                        "Embedded link of id [" + linkId + "] not found!"
                )
        );

        EmbeddedLinkClickSummaryPayload summaryPayload = new EmbeddedLinkClickSummaryPayload();
        summaryPayload.setTotalClicks(embeddedLinkClickService.countTotalClicksByEmbeddedLinkId(targetEmbeddedLink.getId()));
        summaryPayload.setTotalUniqueClicks(embeddedLinkClickService.countUniqueClicksByEmbeddedLinkId(targetEmbeddedLink.getId()));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        summaryPayload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}/embedded-links/{linkId}/clicks")
    public ResponseEntity<GrabbillApiResponse> getEmbeddedLinkClicks(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @PathVariable Long linkId,
            @PageableDefault(sort = {"clickedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        TransactionalEmailType targetType = getTransactionalEmailType(userDetails.getUser(), typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        EmbeddedLink targetEmbeddedLink = embeddedLinkService.getByDomainTypeAndTypeIdAndActivityIdAndId(
                getDomainType(), targetType.getId(), targetActivity.getId(), linkId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1031,
                        "Embedded link of id [" + linkId + "] not found!"
                )
        );

        Page<EmbeddedLinkClick> page = embeddedLinkClickService.getByEmbeddedLinkId(targetEmbeddedLink.getId(), pageable);
        SearchResultPayload<EmbeddedLinkClickPayload> searchResultPayload =
                SearchResultPayload.<EmbeddedLinkClickPayload>builder()
                        .items(page.get().map(EmbeddedLinkClickPayload::from).collect(Collectors.toList()))
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3005,
                    "File upload failed for transactional email activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        if (file.getContentType() != null && file.getContentType().endsWith(ZIP)) {

            try {
                verifyZipContent(account, currentSubscription, file, GrabbillServerErrorCode.GRB3005);

                ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(file.getBytes()));
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = getEntryName(entry);

                    // skipping directory entry and hidden files and none pdf file types
                    if (!entry.isDirectory() && !entryName.startsWith(".") && entryName.endsWith(".pdf")) {
                        Item item = uploadFileByZipInputStreamInternal(
                                account,
                                activityId,
                                FileObjectType.TRANSACTIONAL_EMAIL,
                                entryName,
                                zipInputStream
                        );

                        Optional<TransactionalEmailFile> fileOptional = txefService.getByNameAndActivity(
                                entryName, targetActivity);
                        if (fileOptional.isPresent()) {
                            TransactionalEmailFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entryName);
                            fileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            fileInstance.setFileSize(item.size());
                            fileInstance.setTransactionalEmailType(targetType);
                            fileInstance.setTransactionalEmailActivity(targetActivity);
                            txefService.save(fileInstance);

                        } else {
                            TransactionalEmailFile newFileInstance = new TransactionalEmailFile();
                            newFileInstance.setName(entryName);
                            newFileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setTransactionalEmailType(targetType);
                            newFileInstance.setTransactionalEmailActivity(targetActivity);
                            txefService.save(newFileInstance);
                        }
                        totalFileSize += item.size();
                    }
                }
                zipInputStream.close();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for Transactional email activity of id [" + activityId + "]!"
                );
            }

        } else if (file.getContentType() != null && file.getContentType().endsWith("pdf")) {
            if (file.getSize() > FILE_MAX_BYTES) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3005,
                        "PDF file size exceeded 20mb."
                );
            } else if (file.getSize() > currentSubscription.getMaxAttachmentSize()) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3005,
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
                        FileObjectType.TRANSACTIONAL_EMAIL,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.TRANSACTIONAL_EMAIL,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename()
                );

                Optional<TransactionalEmailFile> fileOptional = txefService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    TransactionalEmailFile fileInstance = fileOptional.get();
                    totalFileSize -= fileInstance.getFileSize();
                    fileInstance.setName(file.getOriginalFilename());
                    fileInstance.setFileType(file.getContentType());
                    fileInstance.setFileSize(item.size());
                    fileInstance.setTransactionalEmailType(targetType);
                    fileInstance.setTransactionalEmailActivity(targetActivity);
                    txefService.save(fileInstance);

                } else {
                    TransactionalEmailFile newFileInstance = new TransactionalEmailFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setTransactionalEmailType(targetType);
                    newFileInstance.setTransactionalEmailActivity(targetActivity);
                    txefService.save(newFileInstance);
                }
                totalFileSize += item.size();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for Transactional email activity of id [" + activityId + "]!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3005,
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

        List<TransactionalEmailFile> files = txefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromTransactionalEmailFiles(files)
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        TransactionalEmailFile targetFile = getFileInActivityById(targetActivity, fileId);

        byte[] bytes = downloadFileInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetFile.getName(),
                FileObjectType.TRANSACTIONAL_EMAIL
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (targetActivity.getTransactionalEmailFiles().isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3009,
                    "No file for given activity with id [" + activityId + "]!"
            );
        }

        ByteArrayOutputStream baos = exportInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetActivity.getTransactionalEmailFiles(),
                FileObjectType.TRANSACTIONAL_EMAIL
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3011,
                    "Incorrect state for Transactional email activity of id [" + activityId + "] - files deletion failed!"
            );
        }

        long totalFileSize = 0;
        for (TransactionalEmailFile transactionalEmailFile : targetActivity.getTransactionalEmailFiles()) {
            totalFileSize -= transactionalEmailFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    transactionalEmailFile.getName()
            );
        }
        targetActivity.getTransactionalEmailFiles().clear();
        txeaService.save(targetActivity);

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

        List<TransactionalEmailFile> files = txefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromTransactionalEmailFiles(files)
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

        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);
        TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3007,
                    "File deletion failed for transactional email activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        TransactionalEmailFile targetFile = null;
        for (TransactionalEmailFile file : targetActivity.getTransactionalEmailFiles()) {
            if (file.getId().equals(fileId)) {
                targetFile = file;
                totalFileSize -= file.getFileSize();
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3007,
                    "File with id [" + fileId + "] is not found."
            );
        }

        targetActivity.getTransactionalEmailFiles().remove(targetFile);
        for (TransactionalEmailIndexRow indexRow : targetActivity.getTransactionalEmailIndexRows()) {
            if (indexRow.getTransactionalEmailFile() != null
                    && indexRow.getTransactionalEmailFile().getId().equals(targetFile.getId())) {
                indexRow.setTransactionalEmailFile(null);
            }
        }
        txeaService.save(targetActivity);

        fileStorageService.delete(
                account,
                FileObjectType.TRANSACTIONAL_EMAIL,
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

        List<TransactionalEmailFile> files = txefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromTransactionalEmailFiles(files)
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
        TransactionalEmailType targetType = getTransactionalEmailType(userDetails.getUser(), typeId);
        Page<TransactionalEmailIndexRow> page = txeirService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        SearchResultPayload<TransactionalEmailIndexRowPayload> searchResultPayload =
                SearchResultPayload.<TransactionalEmailIndexRowPayload>builder()
                        .items(page.get().map(TransactionalEmailIndexRowPayload::from).collect(Collectors.toList()))
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
        TransactionalEmailType targetType = getTransactionalEmailType(userDetails.getUser(), typeId);
        Page<TransactionalEmailIndexRow> page = txeirService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<TransactionalEmailIndexRowPayload> searchResultPayload =
                SearchResultPayload.<TransactionalEmailIndexRowPayload>builder()
                        .items(page.get().map(TransactionalEmailIndexRowPayload::from).collect(Collectors.toList()))
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
        String wabaGuid = user.getAccount().getWabaGuid();
        TransactionalEmailType targetType = getTransactionalEmailType(user, typeId);

        List<TransactionalEmailActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            TransactionalEmailActivity targetActivity = getTransactionalEmailActivity(Long.parseLong(activityId), targetType);

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
        Workbook workbook = transactionalEmailReportService.generateTransactionalEmailReport(targetActivities, targetReportTypes, zoneId, encryptAttachmentPassword, wabaGuid != null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3013, "Failed to generate report for transactional email type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during transactional email report generation");
            }
        }
    }

    private TransactionalEmailType getTransactionalEmailType(
            final User user,
            final Long typeId
    ) {
        TransactionalEmailType targetType = txetService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3001,
                        "Transactional email type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private TransactionalEmailActivity getTransactionalEmailActivity(
            final Long activityId,
            final TransactionalEmailType transactionalEmailType
    ) {
        return txeaService.getByIdAndType(activityId, transactionalEmailType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3003,
                        "Transactional email activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void purgeAllFiles(
            final GrabbillUserDetails userDetails,
            final TransactionalEmailActivity targetActivity
    ) {
        User user = userDetails.getUser();
        for (TransactionalEmailIndexRow indexRow : targetActivity.getTransactionalEmailIndexRows()) {
            indexRow.setTransactionalEmailFile(null);
        }
        long totalFileSize = 0;
        for (TransactionalEmailFile targetFile : targetActivity.getTransactionalEmailFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    user.getAccount(),
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        targetActivity.getTransactionalEmailFiles().clear();

        targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity.setPurgedBy(userDetails.getUsername());
        txeaService.saveAndFlush(targetActivity);

        updateStorageUsageStatistic(user.getAccount(), totalFileSize);
    }

    private void updateAllIndexFieldsAsSoftReferenced(final TransactionalEmailType targetType) {
        boolean typeChanged = false;
        for (TransactionalEmailIndexField indexField : targetType.getTransactionalEmailIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            txetService.save(targetType);
        }
    }

    private TransactionalEmailFile getFileInActivityById(
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
                    "No file with given id [" + fileId + "] found!"
            );
        }
        return targetFile;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.TRANSACTIONAL_EMAIL;
    }

}
