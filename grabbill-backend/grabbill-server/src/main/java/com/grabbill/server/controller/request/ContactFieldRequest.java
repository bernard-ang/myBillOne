package com.grabbill.server.controller.request;

import com.grabbill.core.entity.ContactField;
import com.grabbill.core.model.DataType;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 **/
@Data
public class ContactFieldRequest {

    private Integer id;

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String label;

    private boolean required;

    private DataType dataType;


    public void to(final ContactField contactField) {
        contactField.setName(this.name);
        contactField.setLabel(this.label);
        contactField.setRequired(this.required);
        contactField.setDataType(this.dataType);
    }

}
