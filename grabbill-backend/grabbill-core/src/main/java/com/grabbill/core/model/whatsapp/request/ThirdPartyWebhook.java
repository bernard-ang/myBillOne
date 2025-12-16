package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThirdPartyWebhook {
    private String name;
    private String callbackURL;
    private int service;
    private String serviceId;
}
