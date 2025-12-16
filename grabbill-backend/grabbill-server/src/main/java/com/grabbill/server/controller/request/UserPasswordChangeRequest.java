package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author michaellow
 */
@Data
public class UserPasswordChangeRequest {

//    @NotBlank
    private String password;

    @NotBlank
    private String newPassword;

}
