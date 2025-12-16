package com.grabbill.server.controller.request;

import com.grabbill.core.model.TwoFactorAuthType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author michaellow
 **/
@Data
public class TwoFactorAuthorizationRequest {

    @NotNull
    private TwoFactorAuthType authType;

    @NotBlank
    private String email;

    private int otp;

}
