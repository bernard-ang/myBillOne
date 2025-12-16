package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.ContactField;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.ContactFieldService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.ContactFieldRequest;
import com.grabbill.server.controller.request.ContactFieldsRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.ContactFieldPayload;
import com.grabbill.server.controller.response.payload.ContactFieldsPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/contact-fields")
public class ContactFieldController extends PaymentAwareController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ContactFieldService contactFieldService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getAllContactFields(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        List<ContactField> contactFields = contactFieldService.getAll(userDetails.getUser());
        contactFields.sort(Comparator.comparingInt(ContactField::getSeqOrder));

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactFieldsPayload.from(contactFields)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/{contactFieldId}")
    public ResponseEntity<GrabbillApiResponse> getContactField(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactFieldId
    ) {
        ContactField contactField = getContactFieldById(userDetails, contactFieldId);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactFieldPayload.from(contactField)
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> saveContactFields(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ContactFieldsRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        List<ContactFieldRequest> contactFieldRequests = request.getContactFieldRequests();
        List<Integer> existingContactFieldIds = contactFieldRequests.stream()
                .map(ContactFieldRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<ContactField> contactFields = contactFieldService.getAll(user);
        contactFields.sort(Comparator.comparingInt(ContactField::getSeqOrder));

        List<ContactField> contactFieldsToBeRemoved = new ArrayList<>();
        for (ContactField contactField : contactFields) {
            if (!existingContactFieldIds.contains(contactField.getId())) {
                contactFieldsToBeRemoved.add(contactField);
            }
        }

        for (ContactField contactField : contactFieldsToBeRemoved) {
            contactFieldService.delete(contactField);

            auditLogService.log(
                    account.getId(),
                    Optional.empty(),
                    contactField.getId().longValue(),
                    getDomainType(),
                    ActionType.DELETE,
                    contactField.getName(),
                    userDetails.getUsername()
            );
        }

        contactFields = contactFieldService.getAll(user);
        Map<Integer, ContactField> contactFieldMap = contactFields.stream()
                .collect(Collectors.toMap(ContactField::getId, Function.identity()));

        List<ContactField> savedContactFields = new ArrayList<>();
        int seqNo = 1;
        for (ContactFieldRequest contactFieldRequest : contactFieldRequests) {
            if (contactFieldRequest.getId() != null) {
                ContactField target = contactFieldMap.get(contactFieldRequest.getId());
                contactFieldRequest.to(target);
                target.setSeqOrder(seqNo);
                target = contactFieldService.save(target);
                savedContactFields.add(target);

                auditLogService.log(
                        account.getId(),
                        Optional.empty(),
                        target.getId().longValue(),
                        getDomainType(),
                        ActionType.UPDATE,
                        target.getName(),
                        userDetails.getUsername()
                );

            } else {
                ContactField newInstance = new ContactField();
                contactFieldRequest.to(newInstance);
                newInstance.setAccount(account);
                newInstance.setSeqOrder(seqNo);
                newInstance = contactFieldService.save(newInstance);
                savedContactFields.add(newInstance);

                auditLogService.log(
                        account.getId(),
                        Optional.empty(),
                        newInstance.getId().longValue(),
                        getDomainType(),
                        ActionType.CREATE,
                        newInstance.getName(),
                        userDetails.getUsername()
                );
            }
            seqNo++;
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactFieldsPayload.from(savedContactFields)
                )
        );
    }

    private ContactField getContactFieldById(
            final GrabbillUserDetails userDetails,
            final Integer contactFieldId
    ) {
        return contactFieldService.getById(userDetails.getUser(), contactFieldId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4101,
                        "Contact field of id [" + contactFieldId + "] not found!"
                )
        );
    }

    private DomainType getDomainType() {
        return DomainType.CONTACT_FIELD;
    }

}
