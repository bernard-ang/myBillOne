package com.grabbill.server.controller.request;

import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class TwoFactorAuthUpdateRequest {

    private boolean google2FAEnabled;

    private boolean email2FAEnabled;

}
