package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.ContactGroup;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class ContactGroupBasicPayload implements ApiPayload {

    private Integer id;
    private String name;
    private String description;

    private String createdBy;
    private OffsetDateTime createdDate;
    private String lastModifiedBy;
    private OffsetDateTime lastModifiedDate;


    public static ContactGroupBasicPayload from(ContactGroup contactGroup) {
        ContactGroupBasicPayload payload = new ContactGroupBasicPayload();
        payload.setId(contactGroup.getId());
        payload.setName(contactGroup.getName());
        payload.setDescription(contactGroup.getDescription());

        payload.setCreatedBy(contactGroup.getCreatedBy());
        payload.setCreatedDate(contactGroup.getCreatedDate());
        payload.setLastModifiedBy(contactGroup.getLastModifiedBy());
        payload.setLastModifiedDate(contactGroup.getLastModifiedDate());

        return payload;
    }

}
