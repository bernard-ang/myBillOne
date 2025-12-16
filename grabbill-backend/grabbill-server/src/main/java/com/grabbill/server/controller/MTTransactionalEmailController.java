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
import com.grabbill.server.controller.request.MTTransactionalEmailActivityCreateRequest;
import com.grabbill.server.controller.request.MTTransactionalEmailActivityRequest;
import com.grabbill.server.controller.request.MTTransactionalEmailTypeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.MTTransactionalEmailReportService;
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
@RequestMapping("/mt-txe-types")
public class MTTransactionalEmailController extends BaseController {

    @Autowired
    @Qualifier("mtTransactionalEmailTypeService")
    private BaseTypeService<MTTransactionalEmailType, MTTransactionalEmailActivity> mtTxetService;

    @Autowired
    @Qualifier("mtTransactionalEmailActivityService")
    private BaseActivityService<MTTransactionalEmailType, MTTransactionalEmailActivity> mtTxeaService;

    @Autowired
    @Qualifier("mtTransactionalEmailFileService")
    private BaseFileService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailFile> mtTxefService;

    @Autowired
    @Qualifier("mtTransactionalEmailIndexRowService")
    private BaseIndexRowService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexRow> mtTxeirService;

    @Autowired
    @Qualifier("mtTransactionalEmailActivitySftpService")
    private BaseActivitySftpService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexField> baseActivitySftpService;

    @Autowired
    private MTTransactionalEmailReportService mtTransactionalEmailReportService;

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
        Page<MTTransactionalEmailType> page = StringUtils.hasLength(name) ?
                mtTxetService.getAllByName(user, name, pageable) :
                mtTxetService.getAll(user, pageable);

        SearchResultPayload<MTTransactionalEmailTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<MTTransactionalEmailTypeBasicPayload>builder()
                        .items(page.get()
                                .map(mtTransactionalEmailType -> MTTransactionalEmailTypeBasicPayload.from(
                                        mtTransactionalEmailType,
                                        mtTxeaService.getByTypeAndStatusIn(
                                                mtTransactionalEmailType,
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
        MTTransactionalEmailType target = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        List<MTTransactionalEmailActivity> targetActivities = mtTxeaService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTTransactionalEmailTypePayload.from(
                                target,
                                targetActivities,
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.MT_TRANSACTIONAL_EMAIL,
                                        typeId
                                )
                        )
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody MTTransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTTransactionalEmailType newInstance = new MTTransactionalEmailType();
        request.to(newInstance);
        newInstance.setAccount(account);
        MTTransactionalEmailType savedInstance = mtTxetService.save(newInstance);

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
                        MTTransactionalEmailTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody MTTransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = mtTxetService.findNextDuplicateName(request.getName());

        MTTransactionalEmailType newInstance = new MTTransactionalEmailType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        MTTransactionalEmailType savedInstance = mtTxetService.save(newInstance);

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
                        MTTransactionalEmailTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        Optional<MTTransactionalEmailType> typeOptional = mtTxetService.getByName(userDetails.getUser(), name);

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
            @Valid @RequestBody MTTransactionalEmailTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        request.to(targetType);

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (!mtTxeaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (MTTransactionalEmailIndexField indexField : targetType.getMtTransactionalEmailIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        MTTransactionalEmailType updatedTargetType = mtTxetService.save(targetType);

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
                        MTTransactionalEmailTypePayload.from(
                                updatedTargetType,
                                mtTxeaService.getByTypeAndStatusIn(
                                        updatedTargetType,
                                        Collections.singletonList(ProcessStatus.COMPLETED)
                                ),
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.MT_TRANSACTIONAL_EMAIL,
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        if (!mtTxeaService.getByType(targetType).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3002, "Type is referenced by activity(s).");
        }

        mtTxetService.delete(targetType);

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
                        new ApiMessage("Multi-template Transactional email type with id [" + typeId + "] removed successfully.")
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
        MTTransactionalEmailType target = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        Page<MTTransactionalEmailActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = mtTxeaService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = mtTxeaService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = mtTxeaService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = mtTxeaService.getByType(target, pageable);
        }


        SearchResultPayload<MTTransactionalEmailActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<MTTransactionalEmailActivityBasicPayload>builder()
                        .items(page.get().map(MTTransactionalEmailActivityBasicPayload::from).collect(Collectors.toList()))
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
        MTTransactionalEmailType target = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, target);

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
                        MTTransactionalEmailActivityPayload.from(
                                targetActivity,
                                embeddedLinkPayloads,
                                unsubscribedEmailService.totalUnsubscribedEmailByActivity(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.MT_TRANSACTIONAL_EMAIL,
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
            @Valid @RequestBody MTTransactionalEmailActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity newActivityInstance = new MTTransactionalEmailActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setEmailFrom(targetType.getEmailFrom());
        newActivityInstance.setEmailFromName(targetType.getEmailFromName());
        for (MTTransactionalEmailTemplate targetEmailTemplate : targetType.getMtTransactionalEmailTemplates()) {
            MTTransactionalEmailActivityTemplate targetEmailActivityTemplate = new MTTransactionalEmailActivityTemplate();
            targetEmailActivityTemplate.setEmailTemplateName(targetEmailTemplate.getEmailTemplateName());
            targetEmailActivityTemplate.setEmailSubject(targetEmailTemplate.getEmailSubject());
            targetEmailActivityTemplate.setEmailContent(targetEmailTemplate.getEmailContent());
            targetEmailActivityTemplate.setEmailMjmlContent(targetEmailTemplate.getEmailMjmlContent());
            targetEmailActivityTemplate.setMtTransactionalEmailActivity(newActivityInstance);
            newActivityInstance.getMtTransactionalEmailActivityTemplates().add(targetEmailActivityTemplate);
        }
        newActivityInstance.setMtTransactionalEmailType(targetType);
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        MTTransactionalEmailActivity savedActivityInstance = mtTxeaService.save(newActivityInstance);

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
                        MTTransactionalEmailActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        Optional<MTTransactionalEmailActivity> activityOptional = mtTxeaService.getByNameAndType(name, targetType);

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
            @Valid @RequestBody MTTransactionalEmailActivityRequest request
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        targetActivity.setSftp(request.isSftp());
        targetActivity.setSftpPath(request.getSftpPath());

        if (request.isSftp()) {
            SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                    account, targetType, targetType.getMtTransactionalEmailIndexFields(), targetActivity);
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
            @Valid @RequestBody MTTransactionalEmailActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            MTTransactionalEmailActivity updatedInstance = mtTxeaService.save(targetActivity);

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
                            MTTransactionalEmailActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {

            long additionalEmailCount = targetActivity.getMtTransactionalEmailIndexRows().size();
            if (planUsageService.isTransactionalEmailUsageExceeded(account, additionalEmailCount)) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1902,
                        "Plan's email size limit exceeded"
                );
            }

            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
            request.to(targetActivity);

            if (targetActivity.isSftp()) {
                SftpFolderValidationSummary summary = baseActivitySftpService.validate(
                        account, targetType, targetType.getMtTransactionalEmailIndexFields(), targetActivity);
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
                                MTTransactionalEmailActivityPayload.from(targetActivity)
                        )
                );

            } else {

                targetActivity.setStatus(ProcessStatus.SUBMITTED);
                targetActivity.setSubmittedTimestamp(timestamp);
                targetActivity = mtTxeaService.save(targetActivity);

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
                                MTTransactionalEmailActivityPayload.from(targetActivity)
                        )
                );
            }
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3007, "Deletion only allowed for activity in draft status.");
        }

        long totalFileSize = 0;
        for (MTTransactionalEmailFile targetFile : targetActivity.getMtTransactionalEmailFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        mtTxeaService.delete(targetActivity);

        // if there is no DRAFT activity for the type, set all new index fields added
        // which is not hard referenced yet to soft reference FALSE
        if (mtTxeaService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (MTTransactionalEmailIndexField indexField : targetType.getMtTransactionalEmailIndexFields()) {
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
                        new ApiMessage("Multi-template Transactional email activity with id [" + activityId + "] removed successfully.")
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3004,
                    "Incorrect state for multi-template Transactional email activity of id [" + activityId + "] - file purging failed!"
            );
        }

        purgeAllFiles(userDetails, targetActivity);

        MTTransactionalEmailActivity mtTransactionalEmailActivity = getMtTransactionalEmailActivity(activityId, targetType);
        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                mtTransactionalEmailActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_PURGE,
                mtTransactionalEmailActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        MTTransactionalEmailActivityPayload.from(mtTransactionalEmailActivity)
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3005,
                    "File upload failed for multi-template transactional email activity of id [" + activityId + "]."
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

                        Optional<MTTransactionalEmailFile> fileOptional = mtTxefService.getByNameAndActivity(
                                entryName, targetActivity);
                        if (fileOptional.isPresent()) {
                            MTTransactionalEmailFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entryName);
                            fileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            fileInstance.setFileSize(item.size());
                            fileInstance.setMtTransactionalEmailType(targetType);
                            fileInstance.setMtTransactionalEmailActivity(targetActivity);
                            mtTxefService.save(fileInstance);

                        } else {
                            MTTransactionalEmailFile newFileInstance = new MTTransactionalEmailFile();
                            newFileInstance.setName(entryName);
                            newFileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setMtTransactionalEmailType(targetType);
                            newFileInstance.setMtTransactionalEmailActivity(targetActivity);
                            mtTxefService.save(newFileInstance);
                        }
                        totalFileSize += item.size();
                    }
                }
                zipInputStream.close();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for multi-template Transactional email activity of id [" + activityId + "]!"
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

                Optional<MTTransactionalEmailFile> fileOptional = mtTxefService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    MTTransactionalEmailFile fileInstance = fileOptional.get();
                    totalFileSize -= fileInstance.getFileSize();
                    fileInstance.setName(file.getOriginalFilename());
                    fileInstance.setFileType(file.getContentType());
                    fileInstance.setFileSize(item.size());
                    fileInstance.setMtTransactionalEmailType(targetType);
                    fileInstance.setMtTransactionalEmailActivity(targetActivity);
                    mtTxefService.save(fileInstance);

                } else {
                    MTTransactionalEmailFile newFileInstance = new MTTransactionalEmailFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setMtTransactionalEmailType(targetType);
                    newFileInstance.setMtTransactionalEmailActivity(targetActivity);
                    mtTxefService.save(newFileInstance);
                }
                totalFileSize += item.size();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for multi-template Transactional email activity of id [" + activityId + "]!"
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

        List<MTTransactionalEmailFile> files = mtTxefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtTransactionalEmailFiles(files)
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        MTTransactionalEmailFile targetFile = getFileInActivityById(targetActivity, fileId);

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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (targetActivity.getMtTransactionalEmailFiles().isEmpty()) {
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
                targetActivity.getMtTransactionalEmailFiles(),
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3011,
                    "Incorrect state for multi-template Transactional email activity of id [" + activityId + "] - files deletion failed!"
            );
        }

        long totalFileSize = 0;
        for (MTTransactionalEmailFile mtTransactionalEmailFile : targetActivity.getMtTransactionalEmailFiles()) {
            totalFileSize -= mtTransactionalEmailFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    mtTransactionalEmailFile.getName()
            );
        }
        targetActivity.getMtTransactionalEmailFiles().clear();
        mtTxeaService.save(targetActivity);

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

        List<MTTransactionalEmailFile> files = mtTxefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtTransactionalEmailFiles(files)
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

        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);
        MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3007,
                    "File deletion failed for multi-template transactional email activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        MTTransactionalEmailFile targetFile = null;
        for (MTTransactionalEmailFile file : targetActivity.getMtTransactionalEmailFiles()) {
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

        targetActivity.getMtTransactionalEmailFiles().remove(targetFile);
        for (MTTransactionalEmailIndexRow indexRow : targetActivity.getMtTransactionalEmailIndexRows()) {
            if (indexRow.getMtTransactionalEmailFile() != null
                    && indexRow.getMtTransactionalEmailFile().getId().equals(targetFile.getId())) {
                indexRow.setMtTransactionalEmailFile(null);
            }
        }
        mtTxeaService.save(targetActivity);

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

        List<MTTransactionalEmailFile> files = mtTxefService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromMtTransactionalEmailFiles(files)
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        Page<MTTransactionalEmailIndexRow> page = mtTxeirService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        SearchResultPayload<MTTransactionalEmailIndexRowPayload> searchResultPayload =
                SearchResultPayload.<MTTransactionalEmailIndexRowPayload>builder()
                        .items(page.get().map(MTTransactionalEmailIndexRowPayload::from).collect(Collectors.toList()))
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(userDetails.getUser(), typeId);
        Page<MTTransactionalEmailIndexRow> page = mtTxeirService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<MTTransactionalEmailIndexRowPayload> searchResultPayload =
                SearchResultPayload.<MTTransactionalEmailIndexRowPayload>builder()
                        .items(page.get().map(MTTransactionalEmailIndexRowPayload::from).collect(Collectors.toList()))
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
        MTTransactionalEmailType targetType = getMtTransactionalEmailType(user, typeId);

        List<MTTransactionalEmailActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            MTTransactionalEmailActivity targetActivity = getMtTransactionalEmailActivity(Long.parseLong(activityId), targetType);

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
        Workbook workbook = mtTransactionalEmailReportService.generateMtTransactionalEmailReport(targetActivities, targetReportTypes, zoneId, encryptAttachmentPassword, wabaGuid != null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3013, "Failed to generate report for multi-template transactional email type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during multi-template transactional email report generation");
            }
        }
    }

    private MTTransactionalEmailType getMtTransactionalEmailType(
            final User user,
            final Long typeId
    ) {
        MTTransactionalEmailType targetType = mtTxetService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3001,
                        "Multi-template Transactional email type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private MTTransactionalEmailActivity getMtTransactionalEmailActivity(
            final Long activityId,
            final MTTransactionalEmailType mtTransactionalEmailType
    ) {
        return mtTxeaService.getByIdAndType(activityId, mtTransactionalEmailType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3003,
                        "Multi-template Transactional email activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void purgeAllFiles(
            final GrabbillUserDetails userDetails,
            final MTTransactionalEmailActivity targetActivity
    ) {
        User user = userDetails.getUser();
        for (MTTransactionalEmailIndexRow indexRow : targetActivity.getMtTransactionalEmailIndexRows()) {
            indexRow.setMtTransactionalEmailFile(null);
        }
        long totalFileSize = 0;
        for (MTTransactionalEmailFile targetFile : targetActivity.getMtTransactionalEmailFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    user.getAccount(),
                    FileObjectType.TRANSACTIONAL_EMAIL,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        targetActivity.getMtTransactionalEmailFiles().clear();

        targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity.setPurgedBy(userDetails.getUsername());
        mtTxeaService.saveAndFlush(targetActivity);

        updateStorageUsageStatistic(user.getAccount(), totalFileSize);
    }

    private void updateAllIndexFieldsAsSoftReferenced(final MTTransactionalEmailType targetType) {
        boolean typeChanged = false;
        for (MTTransactionalEmailIndexField indexField : targetType.getMtTransactionalEmailIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            mtTxetService.save(targetType);
        }
    }

    private MTTransactionalEmailFile getFileInActivityById(
            final MTTransactionalEmailActivity targetActivity,
            final Long fileId
    ) {
        MTTransactionalEmailFile targetFile = null;
        for (MTTransactionalEmailFile transactionalEmailFile : targetActivity.getMtTransactionalEmailFiles()) {
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
        return DomainType.MT_TRANSACTIONAL_EMAIL;
    }

}
