package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author michaellow
 */
@Data
public class ContactGroupBindContactsResponse implements ApiPayload {

    private Collection<String> emailsUpdated = new ArrayList<>();

    private Collection<String> invalidEmails = new ArrayList<>();

}
