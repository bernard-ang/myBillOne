package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactGroup;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.ContactGroupService;
import com.grabbill.core.service.ContactService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.ContactGroupBindContactsRequest;
import com.grabbill.server.controller.request.ContactGroupRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.ContactGroupBasicPayload;
import com.grabbill.server.controller.response.payload.ContactGroupBindContactsResponse;
import com.grabbill.server.controller.response.payload.ContactGroupPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/contact-groups")
public class ContactGroupController extends PaymentAwareController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactGroupService contactGroupService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> searchContactGroups(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ContactGroup> page = contactGroupService.searchContactGroups(userDetails.getUser(), name, pageable);

        SearchResultPayload<ContactGroupBasicPayload> searchResultPayload =
                SearchResultPayload.<ContactGroupBasicPayload>builder()
                        .items(page.get()
                                .map(ContactGroupBasicPayload::from)
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
    @GetMapping(value = "/{contactGroupId}")
    public ResponseEntity<GrabbillApiResponse> getContactGroupDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactGroupId
    ) {
        ContactGroup contactGroup = getContactGroup(userDetails.getUser(), contactGroupId);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactGroupPayload.from(contactGroup, contactGroup.getContacts())
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newContactGroup(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ContactGroupRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        ContactGroup newInstance = new ContactGroup();
        request.to(newInstance);
        newInstance.setAccount(account);

        ContactGroup savedInstance = contactGroupService.save(newInstance);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                getDomainType(),
                ActionType.CREATE,
                savedInstance.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactGroupBasicPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PostMapping(value = "/{contactGroupId}/bind-contacts")
    public ResponseEntity<GrabbillApiResponse> bindContactsToContactGroup(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactGroupId,
            @Valid @RequestBody ContactGroupBindContactsRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        ContactGroup targetContractGroup = getContactGroup(user, contactGroupId);

        Set<String> validEmails = new HashSet<>();
        Set<String> invalidEmails = new HashSet<>();
        for (String email : request.getEmails()) {

            List<Contact> contacts = contactService.getByEmail(user, email);
            if (!contacts.isEmpty()) {
                for (Contact contact : contacts) {
                    contact.getContactGroups().add(targetContractGroup);
                    validEmails.add(contactService.save(contact).getEmail());
                }

            } else {
                invalidEmails.add(email);
            }
        }

        ContactGroupBindContactsResponse response = new ContactGroupBindContactsResponse();
        response.setEmailsUpdated(validEmails);
        response.setInvalidEmails(invalidEmails);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(GrabbillServerApiVersion.V1.getVersion(), response)
        );
    }

    @Transactional
    @PutMapping(value = "/{contactGroupId}")
    public ResponseEntity<GrabbillApiResponse> updateContactGroup(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactGroupId,
            @Valid @RequestBody ContactGroupRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        ContactGroup target = getContactGroup(user, contactGroupId);
        request.to(target);
        ContactGroup savedInstance = contactGroupService.save(target);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                getDomainType(),
                ActionType.UPDATE,
                savedInstance.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactGroupBasicPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{contactGroupId}")
    public ResponseEntity<GrabbillApiResponse> deleteContactGroup(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactGroupId
    ) {
        ContactGroup contactGroup = getContactGroup(userDetails.getUser(), contactGroupId);
        contactGroupService.delete(contactGroup);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                contactGroupId.longValue(),
                getDomainType(),
                ActionType.DELETE,
                contactGroup.getName(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Contact group with id [" + contactGroupId + "] removed successfully.")
                )
        );
    }


    private ContactGroup getContactGroup(
            final User user,
            final Integer id
    ) {
        return contactGroupService.getById(user, id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4301,
                        "Contact group with ID [" + id + "] is not found!"
                )
        );
    }

    private DomainType getDomainType() {
        return DomainType.CONTACT_GROUP;
    }

}
