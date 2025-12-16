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
import com.grabbill.server.controller.request.ContactRequest;
import com.grabbill.server.controller.request.ContactUpdateGroupsRequest;
import com.grabbill.server.controller.request.ContactsBulkDeleteRequest;
import com.grabbill.server.controller.request.ContactsRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.ContactBasicPayload;
import com.grabbill.server.controller.response.payload.ContactPayload;
import com.grabbill.server.controller.response.payload.ContactsBasicPayload;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/contacts")
public class ContactController extends PaymentAwareController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactGroupService contactGroupService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> searchContacts(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) Map<String, String> filters,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Contact> page = contactService.searchByFilters(userDetails.getUser(), filters, pageable);

        SearchResultPayload<ContactBasicPayload> searchResultPayload =
                SearchResultPayload.<ContactBasicPayload>builder()
                        .items(page.get()
                                .map(ContactBasicPayload::from)
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
    @GetMapping(value = "/{contactId}")
    public ResponseEntity<GrabbillApiResponse> getContactDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactId
    ) {
        Contact contact = getContact(userDetails.getUser(), contactId);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactPayload.from(contact, contact.getContactGroups())
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newContact(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ContactRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        Contact newInstance = new Contact();
        request.to(newInstance);
        newInstance.setAccount(account);
        newInstance.setContactGroups(new HashSet<>());

        if (contactService.existsByEmail(user, request.getEmail())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4203,
                    "Existing contact with email [" + request.getEmail() + "] exists!"
            );
        }

        if (request.getGroups() != null) {
            for (Integer groupId : request.getGroups()) {
                ContactGroup contactGroup = contactGroupService.getById(user, groupId).orElseThrow(
                        () -> new GrabbillServerException(
                                GrabbillServerErrorCode.GRB4301,
                                "Contact group with ID [" + groupId + "] is not found!"
                        )
                );
                newInstance.getContactGroups().add(contactGroup);
            }
        }

        Contact savedInstance = contactService.save(newInstance);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                getDomainType(),
                ActionType.CREATE,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactPayload.from(savedInstance, savedInstance.getContactGroups())
                )
        );
    }

    @Transactional
    @PostMapping("/bulk-upload")
    public ResponseEntity<GrabbillApiResponse> bulkUploadContacts(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ContactsRequest contactsRequest
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        List<String> contactEmails = new ArrayList<>();
        for (ContactRequest contactRequest : contactsRequest.getContacts()) {
            if (contactEmails.contains(contactRequest.getEmail())) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4203,
                        "Duplicate contact with email [" + contactRequest.getEmail() + "] in the given contacts list!"
                );
            }
            contactEmails.add(contactRequest.getEmail());
        }

        if (contactsRequest.getDuplicateOption() == null) {
            for (ContactRequest contactRequest : contactsRequest.getContacts()) {
                if (contactService.existsByEmail(user, contactRequest.getEmail())) {
                    throw new GrabbillServerException(
                            GrabbillServerErrorCode.GRB4203,
                            "Existing contact with email [" + contactRequest.getEmail() + "] exists!"
                    );
                }
            }
        }

        List<Contact> contactInstances = new ArrayList<>();
        for (ContactRequest contactRequest : contactsRequest.getContacts()) {

            if (contactService.existsByEmail(user, contactRequest.getEmail())) {
                if (ContactsRequest.DuplicateOption.UPDATE_DUPLICATE.equals(contactsRequest.getDuplicateOption())) {
                    for (Contact targetInstance : contactService.getByEmail(user, contactRequest.getEmail())) {
                        contactRequest.to(targetInstance);
                        contactInstances.add(targetInstance);
                    }
                }

            } else {
                Contact newInstance = new Contact();
                contactRequest.to(newInstance);
                newInstance.setAccount(account);
                contactInstances.add(newInstance);
            }
        }

        List<Contact> savedInstances = contactService.saveAll(contactInstances);
        for (Contact savedInstance : savedInstances) {
            auditLogService.log(
                    account.getId(),
                    Optional.empty(),
                    savedInstance.getId().longValue(),
                    getDomainType(),
                    ActionType.CREATE,
                    savedInstance.getEmail(),
                    userDetails.getUsername()
            );
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactsBasicPayload.from(savedInstances)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{contactId}")
    public ResponseEntity<GrabbillApiResponse> updateContactDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactId,
            @Valid @RequestBody ContactRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        Contact contact = getContact(user, contactId);
        request.to(contact);
        Contact savedInstance = contactService.save(contact);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                getDomainType(),
                ActionType.CONTACT_UPDATE_DETAILS,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactPayload.from(savedInstance, savedInstance.getContactGroups())
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{contactId}/groups")
    public ResponseEntity<GrabbillApiResponse> updateContactGroups(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactId,
            @Valid @RequestBody ContactUpdateGroupsRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        Contact contact = getContact(user, contactId);
        contact.getContactGroups().clear();
        for (Integer groupId : request.getGroups()) {
            ContactGroup contactGroup = contactGroupService.getById(user, groupId).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB4301,
                            "Contact group with ID [" + groupId + "] is not found!"
                    )
            );
            contact.getContactGroups().add(contactGroup);
        }
        Contact savedInstance = contactService.save(contact);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                getDomainType(),
                ActionType.CONTACT_UPDATE_GROUPS,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        ContactPayload.from(savedInstance, savedInstance.getContactGroups())
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{contactId}")
    public ResponseEntity<GrabbillApiResponse> deleteContact(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer contactId
    ) {
        Contact contact = getContact(userDetails.getUser(), contactId);
        contactService.delete(contact);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                contactId.longValue(),
                getDomainType(),
                ActionType.DELETE,
                contact.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Contact with id [" + contactId + "] removed successfully.")
                )
        );
    }

    @Transactional
    @DeleteMapping
    public ResponseEntity<GrabbillApiResponse> bulkDeleteContacts(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ContactsBulkDeleteRequest request
    ) {
        if (request.getContactIds().isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4202,
                    "No contact id(s) given for bulk deletion!"
            );
        }

        User user = userDetails.getUser();
        contactService.deleteAllByIds(user, request.getContactIds());

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                user.getId().longValue(),
                getDomainType(),
                ActionType.BULK_DELETE,
                "",
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Bulk deletion of contacts completed successfully.")
                )
        );
    }

    private Contact getContact(final User user, final Integer id) {
        return contactService.getById(user, id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4201,
                        "Contact with ID [" + id + "] is not found!"
                )
        );
    }

    private DomainType getDomainType() {
        return DomainType.CONTACT;
    }

}
