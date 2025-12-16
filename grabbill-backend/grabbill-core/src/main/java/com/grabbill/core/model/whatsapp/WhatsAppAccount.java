package com.grabbill.core.model.whatsapp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WhatsAppAccount {
    private String email;
    private String password;
    private String wabaId;
    private String wabaName;
    private String wabaPhone;
    private String wabaPhoneId;
    private String wabaGuid;
}
