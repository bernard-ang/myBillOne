package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactGroup;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author michaellow
 */
@Data
public class ContactGroupPayload implements ApiPayload {

    private Integer id;
    private String name;
    private String description;

    private String createdBy;
    private OffsetDateTime createdDate;
    private String lastModifiedBy;
    private OffsetDateTime lastModifiedDate;

    private List<ContactPayload> contacts = new ArrayList<>();


    public static ContactGroupPayload from(final ContactGroup contactGroup, final Set<Contact> contacts) {
        ContactGroupPayload payload = new ContactGroupPayload();
        payload.setId(contactGroup.getId());
        payload.setName(contactGroup.getName());
        payload.setDescription(contactGroup.getDescription());

        payload.setCreatedBy(contactGroup.getCreatedBy());
        payload.setCreatedDate(contactGroup.getCreatedDate());
        payload.setLastModifiedBy(contactGroup.getLastModifiedBy());
        payload.setLastModifiedDate(contactGroup.getLastModifiedDate());

        for (Contact contact : contacts) {
            payload.getContacts().add(ContactPayload.from(contact));
        }

        return payload;
    }

    @Data
    public static class ContactPayload {

        private Integer id;
        private String email;
        private String mobileNo;


        public static ContactPayload from(final Contact contact) {
            ContactPayload payload = new ContactPayload();

            payload.setId(contact.getId());
            payload.setEmail(contact.getEmail());
            payload.setMobileNo(contact.getMobileNo());

            return payload;
        }

    }

}
