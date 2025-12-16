package com.grabbill.server.controller.request;

import lombok.Data;

/**
 * @author michaellow
 **/
@Data
public class UserAccountUpdateWabaRequest {
    private String wabaEmail;
    private String wabaPassword;
    private String wabaId;
    private String wabaGuid;
    private String wabaName;
    private String wabaPhone;
    private String wabaPhoneId;
}
