package com.grabbill.server.controller.request;

import lombok.Data;

/**
 * @author michaellow
 **/
@Data
public class AdminUserRequest {

    private String name;

    private String email;

    private String password;

}
