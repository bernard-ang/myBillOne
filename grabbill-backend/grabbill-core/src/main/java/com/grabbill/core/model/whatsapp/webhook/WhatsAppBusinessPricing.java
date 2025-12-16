package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class WhatsAppBusinessPricing {
    private boolean billable;
    private String pricing_model;
    private String category;
}
