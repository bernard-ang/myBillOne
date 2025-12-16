package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author seez
 **/
@Data
public class ForgetPasswordRequest {

    @NotBlank
    private String email;

}
