package com.grabbill.server.controller;

import com.google.gson.Gson;
import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.ReportType;
import com.grabbill.core.service.*;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.whatsapp.WhatsAppServiceException;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.whatsapp.*;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.FilesPayload;
import com.grabbill.server.controller.response.payload.NameCheckPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.controller.response.payload.WhatsAppTemplatesPayload;
import com.grabbill.server.controller.response.payload.whatsapp.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.core.model.whatsapp.request.Sample;
import com.grabbill.core.model.whatsapp.request.TemplateRequest;
import com.grabbill.core.model.whatsapp.components.*;
import com.grabbill.core.model.whatsapp.request.enums.ButtonType;
import com.grabbill.core.model.whatsapp.request.enums.ComponentFormat;
import com.grabbill.core.model.whatsapp.request.enums.Language;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.server.service.WhatsAppReportService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * @author seez
 */
@Slf4j
@RestController
@RequestMapping("/whatsapp")
public class WhatsAppController extends BaseController {

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private AccountService accountService;

    @Value("${whatsapp.ack.url}")
    private String whatsAppAckUrl;

    @Autowired
    @Qualifier("whatsAppTypeService")
    private BaseTypeService<WhatsAppType, WhatsAppActivity> whatsAppTypeService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("whatsAppFileService")
    private BaseFileService<WhatsAppType, WhatsAppActivity, WhatsAppFile> whatsAppFileService;

    @Autowired
    @Qualifier("whatsAppIndexRowService")
    private BaseIndexRowService<WhatsAppType, WhatsAppActivity, WhatsAppIndexRow> whatsAppIndexRowService;

    @Autowired
    private WhatsAppReportService whatsAppReportService;

    @Transactional
    @GetMapping("/templates")
    public ResponseEntity<GrabbillApiResponse> getWhatsappTemplates(
            @AuthenticationPrincipal GrabbillUserDetails userDetails) {
        Account account = userDetails.getUser().getAccount();
        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            WhatsAppTemplatesPayload.from(session.refreshTemplate())
                    )
            );
        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @PostMapping(value = "/templates", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.MULTIPART_MIXED_VALUE})
    public ResponseEntity<GrabbillApiResponse> createWhatsappTemplates(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestPart(name = "sample", required = false) MultipartFile sample,
            @RequestPart(name = "request") String request
    ) {
        Account account = userDetails.getUser().getAccount();
        Gson gson = new Gson();
        WhatsAppTemplateRequest templateRequest = gson.fromJson(request, WhatsAppTemplateRequest.class);

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            List<AbstractComponent> components = new ArrayList<>();

            if (templateRequest.isHasAttachment()) {
                components.add(HeaderComponent.builder()
                        .format(ComponentFormat.DOCUMENT)
                        .build());
            }

            components.add(BodyComponent.builder().text(templateRequest.getBody()).build());


            String footer = templateRequest.getFooter();
            boolean hasFooterContent = footer != null && !footer.trim().equalsIgnoreCase("");
            if (hasFooterContent) {
                components.add(FooterComponent.builder().text(footer).build());
            }

            if (templateRequest.isHasAcknowledgementButton()) {
                String[] example = {"tx-123"};
                Button acknowldgeReceiptButton =
                        Button.builder()
                                .type(ButtonType.URL)
                                .text("Acknowledge Receipt")
                                .url(whatsAppAckUrl)
                                .example(example)
                                .build();
                Button[] buttons = {acknowldgeReceiptButton};
                components.add(ButtonsComponent.builder().buttons(buttons).build());
            }

            TemplateRequest.TemplateRequestBuilder builder =
                    TemplateRequest.builder()
                            .name(templateRequest.getName())
                            .category(templateRequest.getCategory())
                            .language(Language.ENGLISH)
                            .components(components);

            if (templateRequest.isHasAttachment()) {
                try {
                    builder.sample(Sample.builder()
                            .fileData(Base64.getEncoder().encodeToString(sample.getBytes()))
                            .fileSize(sample.getSize())
                            .fileMimeType(sample.getContentType())
                            .build());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            TemplateRequest whatsappTemplateRequest = builder.build();
            RefreshTemplatePayload template = RefreshTemplatePayload.from(session.createTemplate(whatsappTemplateRequest));

            auditLogService.log(
                    account.getId(),
                    Optional.empty(),
                    0L,
                    DomainType.WHATSAPP_TEMPLATE,
                    ActionType.CREATE,
                    template.getName(),
                    userDetails.getUsername()
            );

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            template
                    )
            );

        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @DeleteMapping("/templates/{waTemplateId}")
    public ResponseEntity<GrabbillApiResponse> deleteWhatsappTemplates(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String waTemplateId) {
        Account account = userDetails.getUser().getAccount();

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
            List<RefreshTemplateResponse> refreshTemplateResponses = session.refreshTemplate();
            List<RefreshTemplateResponse> templates = refreshTemplateResponses.stream().filter(item -> item.getWaTemplateId().equalsIgnoreCase(waTemplateId)).collect(Collectors.toList());

            if (templates.isEmpty()) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1507,
                        "Template with id " + waTemplateId + " not found"
                );
            }

            RefreshTemplateResponse template = templates.get(0);

            session.deleteTemplate(template.getId());

            auditLogService.log(
                    account.getId(),
                    Optional.empty(),
                    0L,
                    DomainType.WHATSAPP_TEMPLATE,
                    ActionType.DELETE,
                    template.getName(),
                    userDetails.getUsername()
            );

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            WhatsAppTemplatesPayload.from(session.refreshTemplate())
                    )
            );

        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @PutMapping("/auto-reply-message")
    public ResponseEntity<GrabbillApiResponse> updateAutoReplyMessage(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody WhatsAppAutoReplyRequest request) {
        Account account = userDetails.getUser().getAccount();
        account.setWabaAutoReplyMessage(request.getMessage());
        accountService.save(account);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                DomainType.ACCOUNT,
                ActionType.WABA_AUTO_REPLY_MESSAGE_UPDATE,
                "WhatsApp auto reply message was updated.",
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Successfully update auto reply message")
                )
        );
    }


    private static GrabbillServerException handleWhatsappError(WhatsAppServiceException exception) {
        GrabbillServerErrorCode errorCode;
        switch (exception.getErrorCode()) {
            case GRB1501:
                errorCode = GrabbillServerErrorCode.GRB1501;
                break;
            case GRB1502:
                errorCode = GrabbillServerErrorCode.GRB1502;
                break;
            case GRB1503:
                errorCode = GrabbillServerErrorCode.GRB1503;
                break;
            case GRB1504:
                errorCode = GrabbillServerErrorCode.GRB1504;
                break;
            case GRB1505:
                errorCode = GrabbillServerErrorCode.GRB1505;
                break;
            case GRB1506:
                errorCode = GrabbillServerErrorCode.GRB1506;
                break;
            case GRB1507:
                errorCode = GrabbillServerErrorCode.GRB1507;
                break;
            default:
                errorCode = GrabbillServerErrorCode.GRB1500;
        }

        return new GrabbillServerException(errorCode, exception.getMessage());
    }

    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getTypes(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User user = userDetails.getUser();
        Page<WhatsAppType> page = StringUtils.hasLength(name) ?
                whatsAppTypeService.getAllByName(user, name, pageable) :
                whatsAppTypeService.getAll(user, pageable);

        SearchResultPayload<WhatsAppTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<WhatsAppTypeBasicPayload>builder()
                        .items(page.get()
                                .map(whatsAppType -> WhatsAppTypeBasicPayload.from(
                                        whatsAppType,
                                        whatsAppActivityService.getByTypeAndStatusIn(
                                                whatsAppType,
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
        WhatsAppType target = getWhatsAppType(userDetails.getUser(), typeId);
        List<WhatsAppActivity> targetActivities = whatsAppActivityService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        WhatsAppTypePayload.from(
                                target,
                                targetActivities
                        )
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody WhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        WhatsAppType newInstance = new WhatsAppType();
        request.to(newInstance);
        newInstance.setAccount(account);
        WhatsAppType savedInstance = whatsAppTypeService.save(newInstance);

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
                        WhatsAppTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody WhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = whatsAppTypeService.findNextDuplicateName(request.getName());

        WhatsAppType newInstance = new WhatsAppType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        WhatsAppType savedInstance = whatsAppTypeService.save(newInstance);

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
                        WhatsAppTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        Optional<WhatsAppType> typeOptional = whatsAppTypeService.getByName(userDetails.getUser(), name);

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
            @Valid @RequestBody WhatsAppTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        request.to(targetType);

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (!whatsAppActivityService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (WhatsAppIndexField indexField : targetType.getWhatsAppIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        WhatsAppType updatedTargetType = whatsAppTypeService.save(targetType);

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
                        WhatsAppTypePayload.from(
                                updatedTargetType,
                                whatsAppActivityService.getByTypeAndStatusIn(
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
        WhatsAppType targetType = getWhatsAppType(user, typeId);
        if (!whatsAppActivityService.getByType(targetType).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10002, "Type is referenced by activity(s).");
        }

        whatsAppTypeService.delete(targetType);

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
                        new ApiMessage("WhatsApp type with id [" + typeId + "] removed successfully.")
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
        WhatsAppType target = getWhatsAppType(userDetails.getUser(), typeId);
        Page<WhatsAppActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = whatsAppActivityService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = whatsAppActivityService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = whatsAppActivityService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = whatsAppActivityService.getByType(target, pageable);
        }


        SearchResultPayload<WhatsAppActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<WhatsAppActivityBasicPayload>builder()
                        .items(page.get().map(WhatsAppActivityBasicPayload::from).collect(Collectors.toList()))
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
        WhatsAppType target = getWhatsAppType(userDetails.getUser(), typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, target);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        WhatsAppActivityPayload.from(targetActivity)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> newActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody WhatsAppActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity newActivityInstance = new WhatsAppActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setWhatsAppType(targetType);
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        WhatsAppActivity savedActivityInstance = whatsAppActivityService.save(newActivityInstance);

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
                        WhatsAppActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        WhatsAppType targetType = getWhatsAppType(userDetails.getUser(), typeId);
        Optional<WhatsAppActivity> activityOptional = whatsAppActivityService.getByNameAndType(name, targetType);

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
            @Valid @RequestBody WhatsAppActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            WhatsAppActivity updatedInstance = whatsAppActivityService.save(targetActivity);

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
                            WhatsAppActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {
            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

            request.to(targetActivity);
            targetActivity.setStatus(ProcessStatus.SUBMITTED);
            targetActivity.setSubmittedTimestamp(timestamp);
            targetActivity = whatsAppActivityService.save(targetActivity);

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
                            WhatsAppActivityPayload.from(targetActivity)
                    )
            );
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10007, "Deletion only allowed for activity in draft status.");
        }

        long totalFileSize = 0;
        for (WhatsAppFile targetFile : targetActivity.getWhatsAppFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        whatsAppActivityService.delete(targetActivity);

        // if there is no DRAFT activity for the type, set all new index fields added
        // which is not hard referenced yet to soft reference FALSE
        if (whatsAppActivityService.getByTypeAndStatusIn(
                targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (WhatsAppIndexField indexField : targetType.getWhatsAppIndexFields()) {
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
                        new ApiMessage("WhatsApp activity with id [" + activityId + "] removed successfully.")
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10004,
                    "Incorrect state for WhatsApp activity of id [" + activityId + "] - file purging failed!"
            );
        }

        purgeAllFiles(userDetails, targetActivity);

        WhatsAppActivity whatsAppActivity = getWhatsAppActivity(activityId, targetType);
        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                whatsAppActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_PURGE,
                whatsAppActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        WhatsAppActivityPayload.from(whatsAppActivity)
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10005,
                    "File upload failed for whatsapp activity of id [" + activityId + "]."
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

                        Optional<WhatsAppFile> fileOptional = whatsAppFileService.getByNameAndActivity(
                                entryName, targetActivity);
                        if (fileOptional.isPresent()) {
                            WhatsAppFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entryName);
                            fileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            fileInstance.setFileSize(item.size());
                            fileInstance.setWhatsAppType(targetType);
                            fileInstance.setWhatsAppActivity(targetActivity);
                            whatsAppFileService.save(fileInstance);

                        } else {
                            WhatsAppFile newFileInstance = new WhatsAppFile();
                            newFileInstance.setName(entryName);
                            newFileInstance.setFileType(URLConnection.guessContentTypeFromName(entryName));
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setWhatsAppType(targetType);
                            newFileInstance.setWhatsAppActivity(targetActivity);
                            whatsAppFileService.save(newFileInstance);
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
                        GrabbillServerErrorCode.GRB10005,
                        "PDF file size exceeded 20mb."
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

                Optional<WhatsAppFile> fileOptional = whatsAppFileService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    WhatsAppFile fileInstance = fileOptional.get();
                    totalFileSize -= fileInstance.getFileSize();
                    fileInstance.setName(file.getOriginalFilename());
                    fileInstance.setFileType(file.getContentType());
                    fileInstance.setFileSize(item.size());
                    fileInstance.setWhatsAppType(targetType);
                    fileInstance.setWhatsAppActivity(targetActivity);
                    whatsAppFileService.save(fileInstance);

                } else {
                    WhatsAppFile newFileInstance = new WhatsAppFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setWhatsAppType(targetType);
                    newFileInstance.setWhatsAppActivity(targetActivity);
                    whatsAppFileService.save(newFileInstance);
                }
                totalFileSize += item.size();

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for WhatsApp activity of id [" + activityId + "]!"
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

        List<WhatsAppFile> files = whatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromWhatsAppFiles(files)
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        WhatsAppFile targetFile = getFileInActivityById(targetActivity, fileId);

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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (targetActivity.getWhatsAppFiles().isEmpty()) {
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
                targetActivity.getWhatsAppFiles(),
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10011,
                    "Incorrect state for Transactional email activity of id [" + activityId + "] - files deletion failed!"
            );
        }

        long totalFileSize = 0;
        for (WhatsAppFile whatsAppFile : targetActivity.getWhatsAppFiles()) {
            totalFileSize -= whatsAppFile.getFileSize();
            fileStorageService.delete(
                    account,
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    whatsAppFile.getName()
            );
        }
        targetActivity.getWhatsAppFiles().clear();
        whatsAppActivityService.save(targetActivity);

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

        List<WhatsAppFile> files = whatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromWhatsAppFiles(files)
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

        WhatsAppType targetType = getWhatsAppType(user, typeId);
        WhatsAppActivity targetActivity = getWhatsAppActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB10007,
                    "File deletion failed for whatsapp activity of id [" + activityId + "]."
            );
        }

        long totalFileSize = 0;
        WhatsAppFile targetFile = null;
        for (WhatsAppFile file : targetActivity.getWhatsAppFiles()) {
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

        targetActivity.getWhatsAppFiles().remove(targetFile);
        for (WhatsAppIndexRow indexRow : targetActivity.getWhatsAppIndexRows()) {
            if (indexRow.getWhatsAppFile() != null
                    && indexRow.getWhatsAppFile().getId().equals(targetFile.getId())) {
                indexRow.setWhatsAppFile(null);
            }
        }
        whatsAppActivityService.save(targetActivity);

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

        List<WhatsAppFile> files = whatsAppFileService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromWhatsAppFiles(files)
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
        WhatsAppType targetType = getWhatsAppType(userDetails.getUser(), typeId);
        Page<WhatsAppIndexRow> page = whatsAppIndexRowService.searchByFilters(
                targetType,
                filters,
                true,
                pageable
        );

        SearchResultPayload<WhatsAppIndexRowPayload> searchResultPayload =
                SearchResultPayload.<WhatsAppIndexRowPayload>builder()
                        .items(page.get().map(WhatsAppIndexRowPayload::from).collect(Collectors.toList()))
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
        WhatsAppType targetType = getWhatsAppType(userDetails.getUser(), typeId);
        Page<WhatsAppIndexRow> page = whatsAppIndexRowService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<WhatsAppIndexRowPayload> searchResultPayload =
                SearchResultPayload.<WhatsAppIndexRowPayload>builder()
                        .items(page.get().map(WhatsAppIndexRowPayload::from).collect(Collectors.toList()))
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
        WhatsAppType targetType = getWhatsAppType(user, typeId);

        List<WhatsAppActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            WhatsAppActivity targetActivity = getWhatsAppActivity(Long.parseLong(activityId), targetType);

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
        Workbook workbook = whatsAppReportService.generateWhatsAppReport(targetActivities, targetReportTypes, zoneId, encryptAttachmentPassword);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB10013, "Failed to generate report for transactional email type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during transactional email report generation");
            }
        }
    }

    private WhatsAppType getWhatsAppType(
            final User user,
            final Long typeId
    ) {
        WhatsAppType targetType = whatsAppTypeService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10001,
                        "WhatsApp type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private WhatsAppActivity getWhatsAppActivity(
            final Long activityId,
            final WhatsAppType whatsAppType
    ) {
        return whatsAppActivityService.getByIdAndType(activityId, whatsAppType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB10003,
                        "WhatsApp activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void purgeAllFiles(
            final GrabbillUserDetails userDetails,
            final WhatsAppActivity targetActivity
    ) {
        User user = userDetails.getUser();
        for (WhatsAppIndexRow indexRow : targetActivity.getWhatsAppIndexRows()) {
            indexRow.setWhatsAppFile(null);
        }
        long totalFileSize = 0;
        for (WhatsAppFile targetFile : targetActivity.getWhatsAppFiles()) {
            totalFileSize -= targetFile.getFileSize();
            fileStorageService.delete(
                    user.getAccount(),
                    FileObjectType.WHATSAPP,
                    targetActivity.getId().toString(),
                    targetFile.getName()
            );
        }
        targetActivity.getWhatsAppFiles().clear();

        targetActivity.setPurgedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        targetActivity.setPurgedBy(userDetails.getUsername());
        whatsAppActivityService.saveAndFlush(targetActivity);

        updateStorageUsageStatistic(user.getAccount(), totalFileSize);
    }

    private void updateAllIndexFieldsAsSoftReferenced(final WhatsAppType targetType) {
        boolean typeChanged = false;
        for (WhatsAppIndexField indexField : targetType.getWhatsAppIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            whatsAppTypeService.save(targetType);
        }
    }

    private WhatsAppFile getFileInActivityById(
            final WhatsAppActivity targetActivity,
            final Long fileId
    ) {
        WhatsAppFile targetFile = null;
        for (WhatsAppFile whatsAppFile : targetActivity.getWhatsAppFiles()) {
            if (whatsAppFile.getId().equals(fileId)) {
                targetFile = whatsAppFile;
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
        return DomainType.WHATSAPP;
    }
}
