package com.grabbill.server.controller.request;

import com.grabbill.core.entity.ContactGroup;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class ContactGroupRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String description;


    public void to(final ContactGroup contactGroup) {
        contactGroup.setName(this.getName());
        contactGroup.setDescription(this.getDescription());
    }

}
