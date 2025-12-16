package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author seez
 **/
@Data
public class ResetPasswordRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String code;

}
