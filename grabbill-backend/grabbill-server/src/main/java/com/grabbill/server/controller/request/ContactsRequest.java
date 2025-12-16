package com.grabbill.server.controller.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactsRequest {

    private List<ContactRequest> contacts = new ArrayList<>();

    private DuplicateOption duplicateOption = null;

    public static enum DuplicateOption {

        SKIP_DUPLICATE,

        UPDATE_DUPLICATE

    }

}
