package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Contact;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactsBasicPayload implements ApiPayload {

    private List<ContactBasicPayload> contacts = new ArrayList<>();


    public static ContactsBasicPayload from(List<Contact> contacts) {
        ContactsBasicPayload target = new ContactsBasicPayload();
        for (Contact contact : contacts) {
            target.getContacts().add(ContactBasicPayload.from(contact));
        }

        return target;
    }

}
