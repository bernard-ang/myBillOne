package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.ContactField;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactFieldsPayload implements ApiPayload {

    private List<ContactFieldPayload> contactFieldPayloads = new ArrayList<>();


    public static ContactFieldsPayload from(List<ContactField> contactFields) {
        ContactFieldsPayload payload = new ContactFieldsPayload();
        for (ContactField contactField : contactFields) {
            payload.getContactFieldPayloads().add(ContactFieldPayload.from(contactField));
        }

        return payload;
    }

}
