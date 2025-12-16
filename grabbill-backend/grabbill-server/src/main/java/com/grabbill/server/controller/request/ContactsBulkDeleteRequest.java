package com.grabbill.server.controller.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactsBulkDeleteRequest {

    private List<Integer> contactIds = new ArrayList<>();

}
