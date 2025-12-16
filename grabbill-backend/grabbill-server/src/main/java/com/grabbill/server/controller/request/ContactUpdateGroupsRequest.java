package com.grabbill.server.controller.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactUpdateGroupsRequest {

    private List<Integer> groups = new ArrayList<>();

}
