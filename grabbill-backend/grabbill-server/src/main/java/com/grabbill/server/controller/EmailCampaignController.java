package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.*;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.*;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.EmailCampaignActivityCreateRequest;
import com.grabbill.server.controller.request.EmailCampaignActivityRequest;
import com.grabbill.server.controller.request.EmailCampaignTypeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.EmailCampaignReportService;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/ec-types")
public class EmailCampaignController extends BaseController {

    @Autowired
    @Qualifier("emailCampaignTypeService")
    private BaseTypeService<EmailCampaignType, EmailCampaignActivity> ectService;

    @Autowired
    @Qualifier("emailCampaignActivityService")
    private BaseActivityService<EmailCampaignType, EmailCampaignActivity> ecaService;

    @Autowired
    @Qualifier("emailCampaignFileService")
    private BaseFileService<EmailCampaignType, EmailCampaignActivity, EmailCampaignFile> ecfService;

    @Autowired
    @Qualifier("emailCampaignIndexRowService")
    private BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> ecirService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactGroupService contactGroupService;

    @Autowired
    private ContactFieldService contactFieldService;

    @Autowired
    private EmailCampaignReportService emailCampaignReportService;

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

        Page<EmailCampaignType> page = StringUtils.hasLength(name) ?
                ectService.getAllByName(user, name, pageable) :
                ectService.getAll(user, pageable);

        SearchResultPayload<EmailCampaignTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<EmailCampaignTypeBasicPayload>builder()
                        .items(page.get()
                                .map(transactionalEmailType -> EmailCampaignTypeBasicPayload.from(
                                        transactionalEmailType,
                                        ecaService.getByTypeAndStatusIn(
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
        EmailCampaignType target = getEmailCampaignType(userDetails.getUser(), typeId);
        List<EmailCampaignActivity> targetActivities = ecaService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        EmailCampaignTypePayload.from(
                                target,
                                targetActivities,
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.EMAIL_CAMPAIGN,
                                        typeId
                                ))
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody EmailCampaignTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        EmailCampaignType newInstance = new EmailCampaignType();
        request.to(newInstance);
        if (request.getContactGroupId() != null) {
            Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
            contactGroupOptional.ifPresent(newInstance::setContactGroup);
        }
        newInstance.setAccount(account);
        EmailCampaignType savedInstance = ectService.save(newInstance);

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
                        EmailCampaignTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody EmailCampaignTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = ectService.findNextDuplicateName(request.getName());

        EmailCampaignType newInstance = new EmailCampaignType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        EmailCampaignType savedInstance = ectService.save(newInstance);

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
                        EmailCampaignTypePayload.from(savedInstance, Collections.emptyList(), 0)
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        Optional<EmailCampaignType> typeOptional = ectService.getByName(userDetails.getUser(), name);

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
            @Valid @RequestBody EmailCampaignTypeRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        request.to(targetType);

        if (request.getContactGroupId() != null) {
            if (!Objects.equals(request.getContactGroupId(), targetType.getContactGroup().getId())) {
                Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
                contactGroupOptional.ifPresent(targetType::setContactGroup);
            }
        } else {
            targetType.setContactGroup(null);
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        EmailCampaignType updatedTargetType = ectService.save(targetType);

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
                        EmailCampaignTypePayload.from(
                                updatedTargetType,
                                ecaService.getByTypeAndStatusIn(
                                        updatedTargetType,
                                        Collections.singletonList(ProcessStatus.COMPLETED)
                                ),
                                unsubscribedEmailService.totalUnsubscribedEmailByType(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.EMAIL_CAMPAIGN,
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
        EmailCampaignType target = getEmailCampaignType(user, typeId);
        if (!ecaService.getByType(target).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB4002, "Type is referenced by activity(s).");
        }

        ectService.delete(target);

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
                        new ApiMessage("Email campaign type with id [" + typeId + "] removed successfully.")
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
        EmailCampaignType target = getEmailCampaignType(userDetails.getUser(), typeId);
        Page<EmailCampaignActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = ecaService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = ecaService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = ecaService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = ecaService.getByType(target, pageable);
        }

        SearchResultPayload<EmailCampaignActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<EmailCampaignActivityBasicPayload>builder()
                        .items(page.get().map(EmailCampaignActivityBasicPayload::from).collect(Collectors.toList()))
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
        EmailCampaignType target = getEmailCampaignType(userDetails.getUser(), typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, target);

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
                        EmailCampaignActivityPayload.from(
                                targetActivity,
                                embeddedLinkPayloads,
                                unsubscribedEmailService.totalUnsubscribedEmailByActivity(
                                        userDetails.getUser().getAccount().getId(),
                                        DomainType.EMAIL_CAMPAIGN,
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
            @Valid @RequestBody EmailCampaignActivityCreateRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity newActivityInstance = new EmailCampaignActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setEmailFrom(targetType.getEmailFrom());
        newActivityInstance.setEmailFromName(targetType.getEmailFromName());
        newActivityInstance.setEmailContent(targetType.getEmailContent());
        newActivityInstance.setEmailMjmlContent(targetType.getEmailMjmlContent());
        newActivityInstance.setEmailSubject(targetType.getEmailSubject());
        newActivityInstance.setEmailCampaignType(targetType);
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        newActivityInstance.setContactGroup(targetType.getContactGroup());
        EmailCampaignActivity savedActivityInstance = ecaService.save(newActivityInstance);

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
                        EmailCampaignActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        EmailCampaignType targetType = getEmailCampaignType(userDetails.getUser(), typeId);
        Optional<EmailCampaignActivity> activityOptional = ecaService.getByNameAndType(name, targetType);

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
            @Valid @RequestBody EmailCampaignActivityRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);

        if (ProcessStatus.DRAFT.equals(request.getStatus())) {
            request.to(targetActivity);
            if (request.getContactGroupId() != null) {
                Integer activityContactGroupId  = targetActivity.getContactGroup() != null ? targetActivity.getContactGroup().getId(): null;
                if (!Objects.equals(request.getContactGroupId(), activityContactGroupId)) {
                    Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
                    contactGroupOptional.ifPresent(targetActivity::setContactGroup);
                }
            } else {
                targetActivity.setContactGroup(null);
            }
            EmailCampaignActivity updatedInstance = ecaService.save(targetActivity);

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
                            EmailCampaignActivityPayload.from(updatedInstance)
                    )
            );

        } else if (ProcessStatus.SUBMITTED.equals(request.getStatus())) {
            OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

            request.to(targetActivity);
            if (request.getContactGroupId() != null) {
                Integer activityContactGroupId  = targetActivity.getContactGroup() != null ? targetActivity.getContactGroup().getId(): null;
                if (!Objects.equals(request.getContactGroupId(), activityContactGroupId)) {
                    Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
                    contactGroupOptional.ifPresent(targetActivity::setContactGroup);
                }
            } else {
                targetActivity.setContactGroup(null);
            }
            targetActivity.setStatus(ProcessStatus.SUBMITTED);
            targetActivity.setSubmittedTimestamp(timestamp);
            targetActivity.getEmailCampaignIndexFields().clear();

            // fixed field 1 - email
            EmailCampaignIndexField emailIndexField = new EmailCampaignIndexField();
            emailIndexField.setSeqOrder(1);
            emailIndexField.setName("email");
            emailIndexField.setLabel("Email");
            emailIndexField.setRequired(true);
            emailIndexField.setDataType(DataType.EMAIL);
            emailIndexField.setEmailCampaignActivity(targetActivity);
            targetActivity.getEmailCampaignIndexFields().add(emailIndexField);

            // fixed field 2 - mobile no
            EmailCampaignIndexField mobileNoIndexField = new EmailCampaignIndexField();
            mobileNoIndexField.setSeqOrder(2);
            mobileNoIndexField.setName("mobileNo");
            mobileNoIndexField.setLabel("Mobile No");
            mobileNoIndexField.setRequired(false);
            mobileNoIndexField.setDataType(DataType.TEXT);
            mobileNoIndexField.setEmailCampaignActivity(targetActivity);
            targetActivity.getEmailCampaignIndexFields().add(mobileNoIndexField);


            for (ContactField contactField : contactFieldService.getAll(user)) {
                EmailCampaignIndexField indexField = new EmailCampaignIndexField();
                indexField.setSeqOrder(contactField.getSeqOrder() + 2);
                indexField.setName(contactField.getName());
                indexField.setLabel(contactField.getLabel());
                indexField.setRequired(contactField.isRequired());
                indexField.setDataType(contactField.getDataType());
                indexField.setEmailCampaignActivity(targetActivity);

                targetActivity.getEmailCampaignIndexFields().add(indexField);
            }

            ContactGroup contactGroup = targetActivity.getContactGroup();
            List<Contact> contacts;
            if (contactGroup != null) {
                contacts = new ArrayList<>(contactGroup.getContacts());
            } else {
                contacts = contactService.getAll(user);
            }

            int seqNo = 1;
            targetActivity.getEmailCampaignIndexRows().clear();
            for (Contact contact : contacts) {
                EmailCampaignIndexRow indexRow = new EmailCampaignIndexRow();
                indexRow.setSeqOrder(seqNo++);

                // fixed fields
                indexRow.setText1(contact.getEmail());
                indexRow.setText2(contact.getMobileNo());

                // dynamic fields
                indexRow.setText3(contact.getText1());
                indexRow.setNumber3(contact.getNumber1());
                indexRow.setDate3(contact.getDate1());
                indexRow.setText4(contact.getText2());
                indexRow.setNumber4(contact.getNumber2());
                indexRow.setDate4(contact.getDate2());
                indexRow.setText5(contact.getText3());
                indexRow.setNumber5(contact.getNumber3());
                indexRow.setDate5(contact.getDate3());
                indexRow.setText6(contact.getText4());
                indexRow.setNumber6(contact.getNumber4());
                indexRow.setDate6(contact.getDate4());
                indexRow.setText7(contact.getText5());
                indexRow.setNumber7(contact.getNumber5());
                indexRow.setDate7(contact.getDate5());
                indexRow.setText8(contact.getText6());
                indexRow.setNumber8(contact.getNumber6());
                indexRow.setDate8(contact.getDate6());
                indexRow.setText9(contact.getText7());
                indexRow.setNumber9(contact.getNumber7());
                indexRow.setDate9(contact.getDate7());
                indexRow.setText10(contact.getText8());
                indexRow.setNumber10(contact.getNumber8());
                indexRow.setDate10(contact.getDate8());

                indexRow.setEmailCampaignActivity(targetActivity);
                indexRow.setEmailCampaignType(targetType);

                targetActivity.getEmailCampaignIndexRows().add(indexRow);
            }

            targetActivity = ecaService.save(targetActivity);

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
                            EmailCampaignActivityPayload.from(targetActivity)
                    )
            );
        }

        throw new GrabbillServerException(
                GrabbillServerErrorCode.GRB4006,
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

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB3007, "Deletion only allowed for activity in draft status.");
        }

        ecaService.delete(targetActivity);

        auditLogService.log(
                account.getId(),
                Optional.of(targetType.getId()),
                targetActivity.getId(),
                getDomainType(),
                ActionType.ACTIVITY_DELETE,
                targetActivity.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Email campaign activity with id [" + activityId + "] removed successfully.")
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
        EmailCampaignType targetType = getEmailCampaignType(userDetails.getUser(), typeId);
        Page<EmailCampaignIndexRow> page = ecirService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<EmailCampaignIndexRowPayload> searchResultPayload =
                SearchResultPayload.<EmailCampaignIndexRowPayload>builder()
                        .items(page.get().map(EmailCampaignIndexRowPayload::from).collect(Collectors.toList()))
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
    @GetMapping(value = "/{typeId}/activities/{activityId}/embedded-links/{linkId}/click-summary")
    public ResponseEntity<GrabbillApiResponse> getTotalEmbeddedLinkClicksCount(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @PathVariable Long linkId
    ) {
        EmailCampaignType targetType = getEmailCampaignType(userDetails.getUser(), typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);
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
        EmailCampaignType targetType = getEmailCampaignType(userDetails.getUser(), typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);

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

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4005,
                    "File upload failed for email campaign activity of id [" + activityId + "]."
            );
        }

        if (file.getContentType() != null && file.getContentType().endsWith("pdf")) {
            if (file.getSize() > FILE_MAX_BYTES) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB4005, "PDF file size exceeded 20mb.");
            } else if (file.getSize() > currentSubscription.getMaxAttachmentSize()) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB4005, "PDF file size exceeded " + currentSubscription.getMaxAttachmentSize() + " bytes.");
            }

            try {
                fileStorageService.upload(
                        account,
                        FileObjectType.EMAIL_CAMPAIGN,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.EMAIL_CAMPAIGN,
                        targetActivity.getId().toString(),
                        file.getOriginalFilename()
                );

                Optional<EmailCampaignFile> fileOptional = ecfService.getByNameAndActivity(
                        file.getOriginalFilename(), targetActivity);
                if (fileOptional.isPresent()) {
                    EmailCampaignFile emailCampaignFile = fileOptional.get();
                    emailCampaignFile.setName(file.getOriginalFilename());
                    emailCampaignFile.setFileType(file.getContentType());
                    emailCampaignFile.setFileSize(item.size());
                    emailCampaignFile.setEmailCampaignActivity(targetActivity);
                    ecfService.save(emailCampaignFile);

                } else {
                    EmailCampaignFile newFileInstance = new EmailCampaignFile();
                    newFileInstance.setName(file.getOriginalFilename());
                    newFileInstance.setFileType(file.getContentType());
                    newFileInstance.setFileSize(item.size());
                    newFileInstance.setEmailCampaignActivity(targetActivity);
                    ecfService.save(newFileInstance);
                }

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "File upload failed for email campaign activity of id [" + activityId + "]!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4004,
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

        List<EmailCampaignFile> files = ecfService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromEmailCampaignFiles(files)
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

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);
        EmailCampaignFile targetFile = getFileInActivityById(targetActivity, fileId);

        byte[] bytes = downloadFileInternal(
                account,
                userDetails.getUsername(),
                targetType.getId(),
                activityId,
                targetActivity.getName(),
                targetFile.getName(),
                FileObjectType.EMAIL_CAMPAIGN
        );

        return new ResponseEntity<>(new ByteArrayResource(bytes), createFileDownloadHttpHeaders(targetFile), HttpStatus.OK);
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

        EmailCampaignType targetType = getEmailCampaignType(user, typeId);
        EmailCampaignActivity targetActivity = getEmailCampaignActivity(activityId, targetType);
        if (ProcessStatus.PROCESSING.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4007,
                    "File deletion failed for email campaign activity of id [" + activityId + "]."
            );
        }

        EmailCampaignFile targetFile = getFileInActivityById(targetActivity, fileId);
        if (targetFile == null) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB4007, "File with id [" + fileId + "] is not found.");
        }


        targetActivity.getEmailCampaignFiles().remove(targetFile);
        ecaService.save(targetActivity);

        fileStorageService.delete(
                account,
                FileObjectType.EMAIL_CAMPAIGN,
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

        List<EmailCampaignFile> files = ecfService.getByActivity(targetActivity);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        FilesPayload.fromEmailCampaignFiles(files)
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
        EmailCampaignType targetType = getEmailCampaignType(user, typeId);

        List<EmailCampaignActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            EmailCampaignActivity targetActivity = getEmailCampaignActivity(Long.parseLong(activityId), targetType);

            if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB4010, "Activity [" + activityId + "] not in right state for report generation.");
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
        Workbook workbook = emailCampaignReportService.generateEmailCampaignReport(targetActivities, targetReportTypes, zoneId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB4010, "Failed to generate report for email campaign type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during email campaign report generation");
            }
        }
    }

    private EmailCampaignType getEmailCampaignType(
            final User user,
            final Long typeId
    ) {
        EmailCampaignType targetType = ectService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4001,
                        "Email campaign type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private EmailCampaignActivity getEmailCampaignActivity(
            final Long activityId,
            final EmailCampaignType emailCampaignType
    ) {
        return ecaService.getByIdAndType(activityId, emailCampaignType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4003,
                        "Email campaign activity of id [" + activityId + "] not found!"
                )
        );
    }

    private EmailCampaignFile getFileInActivityById(
            final EmailCampaignActivity targetActivity,
            final Long fileId
    ) {
        EmailCampaignFile targetFile = null;
        for (EmailCampaignFile emailCampaignFile : targetActivity.getEmailCampaignFiles()) {
            if (emailCampaignFile.getId().equals(fileId)) {
                targetFile = emailCampaignFile;
                break;
            }
        }

        if (targetFile == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4008,
                    "No file with given id [" + fileId + "] found!"
            );
        }
        return targetFile;
    }

    @Override
    DomainType getDomainType() {
        return DomainType.EMAIL_CAMPAIGN;
    }

}
