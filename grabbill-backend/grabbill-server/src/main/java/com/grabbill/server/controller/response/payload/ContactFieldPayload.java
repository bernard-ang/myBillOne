package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.ContactField;
import com.grabbill.core.model.DataType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class ContactFieldPayload implements ApiPayload {

    private Integer id;
    private int seqOrder;
    private String name;
    private String label;
    private boolean required;
    private DataType dataType;
    private boolean referenced;


    public static ContactFieldPayload from(ContactField contactField) {
        ContactFieldPayload payload = new ContactFieldPayload();
        payload.id = contactField.getId();
        payload.seqOrder = contactField.getSeqOrder();
        payload.name = contactField.getName();
        payload.label = contactField.getLabel();
        payload.required = contactField.isRequired();
        payload.dataType = contactField.getDataType();
        payload.referenced = contactField.isReferenced();

        return payload;
    }

}
