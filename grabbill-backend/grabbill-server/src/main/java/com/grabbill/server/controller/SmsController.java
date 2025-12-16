package com.grabbill.server.controller;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.grabbill.core.entity.*;
import com.grabbill.core.model.*;
import com.grabbill.core.service.*;
import com.grabbill.core.utils.SmsUtils;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.*;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.SmsReportService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController  extends BaseController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${sms.endpoints.enabled:false}")
    private boolean enabled;

    @Autowired
    @Qualifier("smsTypeService")
    private BaseTypeService<SmsType, SmsActivity> smsTypeService;

    @Autowired
    @Qualifier("smsActivityService")
    private BaseActivityService<SmsType, SmsActivity> smsActivityService;

    @Autowired
    @Qualifier("smsIndexRowService")
    private BaseIndexRowService<SmsType, SmsActivity, SmsIndexRow> smsIndexRowService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactGroupService contactGroupService;

    @Autowired
    private ContactFieldService contactFieldService;

    @Autowired
    private IndexRowHelper indexRowHelper;

    @Autowired
    private SmsReportService smsReportService;

    @Autowired
    private PlanUsageService planUsageService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getTypes(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();

        Page<SmsType> page = StringUtils.hasLength(name) ?
                smsTypeService.getAllByName(user, name, pageable) :
                smsTypeService.getAll(user, pageable);

        SearchResultPayload<SmsTypeBasicPayload> searchResultPayload =
                SearchResultPayload.<SmsTypeBasicPayload>builder()
                        .items(page.get()
                                .map(SmsTypeBasicPayload::from)
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        SmsType target = getSmsType(userDetails.getUser(), typeId);
        List<SmsActivity> targetActivities = smsActivityService.getByTypeAndStatusIn(
                target, Collections.singletonList(ProcessStatus.COMPLETED));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsTypePayload.from(target, targetActivities)
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody SmsTypeRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        SmsType newInstance = new SmsType();
        request.to(newInstance);
        newInstance.setAccount(account);
        if (request.getContactGroupId() != null) {
            Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
            contactGroupOptional.ifPresent(newInstance::setContactGroup);
        }
        SmsType savedInstance = smsTypeService.save(newInstance);

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
                        SmsTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @PostMapping("/duplicate")
    public ResponseEntity<GrabbillApiResponse> duplicateType(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody SmsTypeRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        String nextDuplicateName = smsTypeService.findNextDuplicateName(request.getName());

        SmsType newInstance = new SmsType();
        request.to(newInstance);
        newInstance.setName(nextDuplicateName);
        newInstance.setAccount(account);
        SmsType savedInstance = smsTypeService.save(newInstance);

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
                        SmsTypePayload.from(savedInstance, Collections.emptyList())
                )
        );
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateTypeName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable String name
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        Optional<SmsType> typeOptional = smsTypeService.getByName(userDetails.getUser(), name);

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
            @Valid @RequestBody SmsTypeRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        SmsType targetType = getSmsType(user, typeId);
        request.to(targetType);

        if (request.getContactGroupId() != null) {
            Integer typeContactGroupId  = targetType.getContactGroup() != null ? targetType.getContactGroup().getId(): null;
            if (!Objects.equals(request.getContactGroupId(), typeContactGroupId)) {
                Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
                contactGroupOptional.ifPresent(targetType::setContactGroup);
            }
        } else {
            targetType.setContactGroup(null);
        }

        // if there is DRAFT activity(s), set all index fields as soft referenced
        if (SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType()) &&
                !smsActivityService.getByTypeAndStatusIn(
                        targetType, Collections.singletonList(ProcessStatus.DRAFT)).isEmpty()) {
            for (SmsIndexField indexField : targetType.getSmsIndexFields()) {
                // new index field added
                if (!indexField.isHardRef()) {
                    indexField.setSoftRef(true);
                }
            }
        }

        // KLUDGE: mark entity "dirty" forcefully to update the entity audit
        targetType.setLastModifiedDate(OffsetDateTime.now(ZoneOffset.UTC));
        SmsType updatedTargetType = smsTypeService.save(targetType);

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
                        SmsTypePayload.from(
                                updatedTargetType,
                                smsActivityService.getByTypeAndStatusIn(
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        SmsType targetType = getSmsType(user, typeId);
        if (!smsActivityService.getByType(targetType).isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9002, "Type is referenced by activity(s).");
        }

        smsTypeService.delete(targetType);

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
                        new ApiMessage("Sms type with id [" + typeId + "] removed successfully.")
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        SmsType target = getSmsType(userDetails.getUser(), typeId);
        Page<SmsActivity> page;
        if (StringUtils.hasLength(name) && status != null) {
            page = smsActivityService.getByTypeAndNameAndStatus(target, name, status, pageable);

        } else if (StringUtils.hasLength(name)) {
            page = smsActivityService.getByTypeAndName(target, name, pageable);

        } else if (status != null) {
            page = smsActivityService.getByTypeAndStatus(target, status, pageable);

        } else {
            page = smsActivityService.getByType(target, pageable);
        }


        SearchResultPayload<SmsActivityBasicPayload> searchResultPayload =
                SearchResultPayload.<SmsActivityBasicPayload>builder()
                        .items(page.get().map(SmsActivityBasicPayload::from).collect(Collectors.toList()))
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

    @GetMapping(path = "/usage-summary")
    public ResponseEntity<GrabbillApiResponse> getUsageSummary(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate
    ) {
        User user = userDetails.getUser();
        SmsUsageSummaryPayload payload = new SmsUsageSummaryPayload();
        for (SmsActivity activity : smsActivityService.getAllByAccountBetween(user.getAccount(), startDate, endDate)) {
            if (ProcessStatus.COMPLETED.equals(activity.getStatus())) {
                payload.setSent(payload.getSent() + activity.getSmsStatusSent());
                payload.setError(payload.getError() + activity.getSmsStatusError());
                payload.setCreditUsed(payload.getCreditUsed() + activity.getSmsCreditUsed());
            }
        }

        return ResponseEntity.ok().body(new GrabbillApiResponse(GrabbillServerApiVersion.V1.getVersion(), payload));
    }

    @Transactional
    @GetMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> getActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        SmsType target = getSmsType(userDetails.getUser(), typeId);
        SmsActivity targetActivity = getSmsActivity(activityId, target);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsActivityPayload.from(targetActivity)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities")
    public ResponseEntity<GrabbillApiResponse> newActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @Valid @RequestBody SmsActivityCreateRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        SmsType targetType = getSmsType(user, typeId);
        SmsActivity newActivityInstance = new SmsActivity();
        newActivityInstance.setName(request.getName());
        newActivityInstance.setSmsFrom(targetType.getSmsFrom());
        newActivityInstance.setSmsContent(SmsUtils.appendRM0(targetType.getSmsContent()));
        newActivityInstance.setSmsType(targetType);
        newActivityInstance.setContactGroup(targetType.getContactGroup());
        newActivityInstance.setStatus(ProcessStatus.DRAFT);
        newActivityInstance.setDraftTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        SmsActivity savedActivityInstance = smsActivityService.save(newActivityInstance);

        // mark all index fields as soft referenced (only for index field type)
        if (SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType())) {
            updateAllIndexFieldsAsSoftReferenced(targetType);
        }

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
                        SmsActivityPayload.from(savedActivityInstance)
                )
        );
    }

    @GetMapping(value = "/{typeId}/activities/name/{name}")
    public ResponseEntity<GrabbillApiResponse> validateActivityName(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable String name
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        SmsType targetType = getSmsType(userDetails.getUser(), typeId);
        Optional<SmsActivity> activityOptional = smsActivityService.getByNameAndType(name, targetType);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        NameCheckPayload.from(activityOptional.isPresent())
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{typeId}/activities/{activityId}/count-credit-usage")
    public ResponseEntity<GrabbillApiResponse> countActivityCreditUsage(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @Valid @RequestBody SmsActivityCountCreditUsageRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        SmsType targetType = getSmsType(user, typeId);
        SmsActivity targetActivity = getSmsActivity(activityId, targetType);

        int totalSms = 0;
        int estimatedCreditUsage = 0;
        List<SmsActivityCreditUsagePayload.IndexRowUsage> indexRowUsages = new ArrayList<>();
        try {
            List<SmsActivityIndexField> indexFields = new ArrayList<>();
            List<SmsIndexRow> indexRows = new ArrayList<>();

            Handlebars handlebars = new Handlebars();
            if (SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType())) {

                indexFields.addAll(smsIndexFieldToActivityIndexField(targetType.getSmsIndexFields(), targetActivity));
                for (BaseIndexRowRequest baseIndexRowRequest : request.getIndexRows()) {
                    SmsIndexRow indexRow = new SmsIndexRow();
                    baseIndexRowRequest.to(indexRow);
                    indexRows.add(indexRow);
                }

            } else if (SmsFieldType.CONTACT_FIELD.equals(targetType.getSmsFieldType())) {

                indexFields.addAll(contactFieldToActivityIndexField(contactFieldService.getAll(user), targetActivity));
                Optional<ContactGroup> contactGroupOptional = contactGroupService.getById(user, request.getContactGroupId());
                List<Contact> contacts;
                if (contactGroupOptional.isPresent()) {
                    contacts = new ArrayList<>(contactGroupOptional.get().getContacts());
                } else {
                    contacts = contactService.getAll(user);
                }
                indexRows.addAll(toSmsIndexRows(contacts, targetType, targetActivity));

            } else {
                return ResponseEntity.badRequest().body(
                        new GrabbillApiResponse(
                                GrabbillServerApiVersion.V1.getVersion(),
                                new ApiMessage("Invalid sms field type " + targetType.getSmsFieldType())
                        )
                );
            }

            for (SmsIndexRow indexRow : indexRows) {
                Map<String, String> parameterMap = buildParameterMap(indexRow, indexFields);
                Template smsTemplate = handlebars.compileInline(SmsUtils.appendRM0(request.getSmsContent()));
                String processedSmsContent = smsTemplate.apply(parameterMap);

                int totalBytes = SmsUtils.countBytes(processedSmsContent);
                int credits = SmsUtils.estimateCreditUsage(processedSmsContent);

                SmsActivityCreditUsagePayload.IndexRowUsage indexRowUsage = new SmsActivityCreditUsagePayload.IndexRowUsage();
                indexRowUsage.setEmail(SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType()) ? null : indexRow.getText1());
                indexRowUsage.setMobileNo(SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType()) ? indexRow.getText1() : indexRow.getText2());
                indexRowUsage.setSmsContent(processedSmsContent);
                indexRowUsage.setValidMobileNo(SmsUtils.isValidPhoneNumber(indexRowUsage.getMobileNo()));
                indexRowUsage.setTotalBytes(totalBytes);
                indexRowUsage.setTotalCredits(indexRowUsage.isValidMobileNo() ? credits : 0);
                indexRowUsages.add(indexRowUsage);
                estimatedCreditUsage += credits;
                totalSms++;
            }

        } catch (Exception e) {
            // TODO: how to handle?
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsActivityCreditUsagePayload.from(indexRowUsages, totalSms, estimatedCreditUsage)
                )
        );
    }

    Map<String, String> buildParameterMap(
            final SmsIndexRow indexRow,
            final List<SmsActivityIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            SmsActivityIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = indexRowHelper.getText(i + 1, indexRow);
                parameterMap.put(indexField.getName(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = indexRowHelper.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getName(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = indexRowHelper.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getName(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }

    @Transactional
    @PutMapping(value = "/{typeId}/activities/{activityId}")
    public ResponseEntity<GrabbillApiResponse> updateActivity(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long typeId,
            @PathVariable Long activityId,
            @Valid @RequestBody SmsActivityRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        SmsType targetType = getSmsType(user, typeId);
        SmsActivity targetActivity = getSmsActivity(activityId, targetType);

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

            SmsActivity updatedInstance = smsActivityService.save(targetActivity);

            // mark all index fields as soft referenced (only for index field type)
            if (SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType())) {
                updateAllIndexFieldsAsSoftReferenced(targetType);
            }

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
                            SmsActivityPayload.from(updatedInstance)
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

            targetActivity.getSmsActivityIndexFields().clear();
            if (SmsFieldType.CONTACT_FIELD.equals(targetType.getSmsFieldType())) {
                targetActivity.getSmsActivityIndexFields()
                        .addAll(contactFieldToActivityIndexField(contactFieldService.getAll(user), targetActivity));

                targetActivity.getSmsIndexRows().clear();
                ContactGroup contactGroup = targetActivity.getContactGroup();
                List<Contact> contacts;
                if (contactGroup != null) {
                    contacts = new ArrayList<>(contactGroup.getContacts());
                } else {
                    contacts = contactService.getAll(user);
                }
                targetActivity.getSmsIndexRows().addAll(toSmsIndexRows(contacts, targetType, targetActivity));


            } else if (SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType())) {
                targetActivity.getSmsActivityIndexFields()
                        .addAll(smsIndexFieldToActivityIndexField(targetType.getSmsIndexFields(), targetActivity));
            }

            int estimatedCreditUsage = 0;
            try {
                Handlebars handlebars = new Handlebars();
                for (SmsIndexRow smsIndexRow : targetActivity.getSmsIndexRows()) {
                    String phoneNumber = smsIndexRow.getText1();
                    if (SmsUtils.isValidPhoneNumber(phoneNumber) && !phoneNumber.startsWith("6")) {
                        smsIndexRow.setText1("6" + phoneNumber);
                    }

                    Map<String, String> parameterMap = buildParameterMap(smsIndexRow, targetActivity.getSmsActivityIndexFields());
                    Template smsTemplate = handlebars.compileInline(request.getSmsContent());
                    String processedSmsContent = smsTemplate.apply(parameterMap);

                    int credits = SmsUtils.estimateCreditUsage(processedSmsContent);
                    estimatedCreditUsage += credits;
                }
            } catch (Exception e) {
                // TODO: how to handle?
            }

            // check if sms credit enough!!!
            if (planUsageService.getSmsRemainingCredits(account) < estimatedCreditUsage) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1903,
                        "Not enough sms credits to process this activity"
                );
            }

            targetActivity = smsActivityService.save(targetActivity);

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
                            SmsActivityPayload.from(targetActivity)
                    )
            );
        }

        throw new GrabbillServerException(
                GrabbillServerErrorCode.GRB9004,
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        Account account = user.getAccount();

        SmsType targetType = getSmsType(user, typeId);
        SmsActivity targetActivity = getSmsActivity(activityId, targetType);
        if (!ProcessStatus.DRAFT.equals(targetActivity.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9005, "Deletion only allowed for activity in draft status.");
        }

        smsActivityService.delete(targetActivity);

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
                        new ApiMessage("Sms activity with id [" + activityId + "] removed successfully.")
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        SmsType targetType = getSmsType(userDetails.getUser(), typeId);
        Page<SmsIndexRow> page = smsIndexRowService.searchByFilters(
                targetType,
                filters,
                false,
                pageable
        );

        SearchResultPayload<SmsIndexRowPayload> searchResultPayload =
                SearchResultPayload.<SmsIndexRowPayload>builder()
                        .items(page.get().map(SmsIndexRowPayload::from).collect(Collectors.toList()))
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
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        SmsType targetType = getSmsType(user, typeId);

        List<SmsActivity> targetActivities = new ArrayList<>();
        for (String activityId : activityIds) {
            SmsActivity targetActivity = getSmsActivity(Long.parseLong(activityId), targetType);

            if (!ProcessStatus.COMPLETED.equals(targetActivity.getStatus())) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB9010, "Activity [" + activityId + "] not in right state for report generation.");
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
        Workbook workbook = smsReportService.generateSmsReport(targetActivities, targetReportTypes, zoneId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(typeId, baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9010, "Failed to generate report for sms type [" + typeId + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during sms report generation");
            }
        }
    }


    private SmsType getSmsType(
            final User user,
            final Long typeId
    ) {
        SmsType targetType = smsTypeService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB9001,
                        "Sms type of id [" + typeId + "] not found!"
                )
        );
        verifyIfTypeCodeIsAllowed(user, targetType.getCode());
        return targetType;
    }

    private SmsActivity getSmsActivity(
            final Long activityId,
            final SmsType smsType
    ) {
        return smsActivityService.getByIdAndType(activityId, smsType).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB9003,
                        "Sms activity of id [" + activityId + "] not found!"
                )
        );
    }

    private void updateAllIndexFieldsAsSoftReferenced(final SmsType targetType) {
        boolean typeChanged = false;
        for (SmsIndexField indexField : targetType.getSmsIndexFields()) {
            if (!indexField.isSoftRef()) {
                indexField.setSoftRef(true);
                typeChanged = true;
            }
        }

        if (typeChanged) {
            smsTypeService.save(targetType);
        }
    }

    private List<SmsActivityIndexField> smsIndexFieldToActivityIndexField(
            final List<SmsIndexField> smsIndexFields,
            final SmsActivity targetActivity
    ) {
        List<SmsActivityIndexField> indexFields = new ArrayList<>();
        for (SmsIndexField smsIndexField : smsIndexFields) {
            SmsActivityIndexField indexField = new SmsActivityIndexField();
            indexField.setSeqOrder(smsIndexField.getSeqOrder() + 2);
            indexField.setName(smsIndexField.getHeader());
            indexField.setLabel(smsIndexField.getLabel());
            indexField.setRequired(smsIndexField.isRequired());
            indexField.setDataType(smsIndexField.getDataType());
            indexField.setSmsActivity(targetActivity);
            indexFields.add(indexField);
        }

        return indexFields;
    }

    private List<SmsActivityIndexField> contactFieldToActivityIndexField(
            final List<ContactField> contactFields,
            final SmsActivity targetActivity
    ) {
        List<SmsActivityIndexField> indexFields = new ArrayList<>();

        // fixed field 1 - email
        SmsActivityIndexField emailIndexField = new SmsActivityIndexField();
        emailIndexField.setSeqOrder(1);
        emailIndexField.setName("email");
        emailIndexField.setLabel("Email");
        emailIndexField.setRequired(true);
        emailIndexField.setDataType(DataType.EMAIL);
        emailIndexField.setSmsActivity(targetActivity);
        indexFields.add(emailIndexField);

        // fixed field 2 - mobile no
        SmsActivityIndexField mobileNoIndexField = new SmsActivityIndexField();
        mobileNoIndexField.setSeqOrder(2);
        mobileNoIndexField.setName("mobileNo");
        mobileNoIndexField.setLabel("Mobile No");
        mobileNoIndexField.setRequired(false);
        mobileNoIndexField.setDataType(DataType.TEXT);
        mobileNoIndexField.setSmsActivity(targetActivity);
        indexFields.add(mobileNoIndexField);

        for (ContactField contactField : contactFields) {
            SmsActivityIndexField indexField = new SmsActivityIndexField();
            indexField.setSeqOrder(contactField.getSeqOrder() + 2);
            indexField.setName(contactField.getName());
            indexField.setLabel(contactField.getLabel());
            indexField.setRequired(contactField.isRequired());
            indexField.setDataType(contactField.getDataType());
            indexField.setSmsActivity(targetActivity);
            indexFields.add(indexField);
        }

        return indexFields;
    }

    private List<SmsIndexRow> toSmsIndexRows(
            final List<Contact> contacts,
            final SmsType targetType,
            final SmsActivity targetActivity
    ) {
        int seqNo = 1;
        List<SmsIndexRow> smsIndexRows = new ArrayList<>();
        for (Contact contact : contacts) {
            SmsIndexRow indexRow = new SmsIndexRow();
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

            indexRow.setSmsActivity(targetActivity);
            indexRow.setSmsType(targetType);

            smsIndexRows.add(indexRow);
        }

        return smsIndexRows;
    }

    @Override
    DomainType getDomainType() {
        return DomainType.SMS;
    }

}
