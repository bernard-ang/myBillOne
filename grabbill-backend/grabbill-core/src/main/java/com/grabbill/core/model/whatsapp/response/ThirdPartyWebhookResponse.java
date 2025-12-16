package com.grabbill.core.model.whatsapp.response;


import lombok.Data;

@Data
public class ThirdPartyWebhookResponse {
    private String id;
    private String name;
    private String callbackURL;
    private int service;
    private String serviceId;
    private boolean isActive;
    private String updatedBy;
    private String updatedDate;
}
