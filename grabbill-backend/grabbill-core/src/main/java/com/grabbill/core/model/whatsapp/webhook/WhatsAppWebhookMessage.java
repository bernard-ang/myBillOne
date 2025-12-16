package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class WhatsAppWebhookMessage {
    private String Id;
    private String Json;
    private String CreateDate;
}
