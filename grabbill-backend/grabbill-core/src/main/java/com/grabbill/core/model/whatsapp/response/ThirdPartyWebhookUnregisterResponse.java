package com.grabbill.core.model.whatsapp.response;


import lombok.Data;

@Data
public class ThirdPartyWebhookUnregisterResponse {
    private boolean status;
    private String code;
    private String message;
}
