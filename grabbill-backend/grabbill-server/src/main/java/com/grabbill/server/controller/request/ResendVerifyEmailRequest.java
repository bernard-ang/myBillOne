package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author seez
 */
@Data
public class ResendVerifyEmailRequest {
    @NotBlank
    private String email;
}
