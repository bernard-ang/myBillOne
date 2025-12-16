package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author michaellow
 **/
@Data
public class LoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
