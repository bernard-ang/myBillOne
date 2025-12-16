package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author michaellow
 */
@Data
public class NewUserAccountRegistrationRequest {

    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String affiliateCode;

}
