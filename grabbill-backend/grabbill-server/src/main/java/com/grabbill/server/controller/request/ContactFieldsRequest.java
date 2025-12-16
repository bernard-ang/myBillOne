package com.grabbill.server.controller.request;

import lombok.Data;

import java.util.List;

/**
 * @author michaellow
 **/
@Data
public class ContactFieldsRequest {

    private List<ContactFieldRequest> contactFieldRequests;

}
