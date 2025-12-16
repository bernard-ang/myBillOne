package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WhatsAppBusinessStatus {
    private String id;
    private String status;
    private String timestamp;
    private String recipient_id;
    private WhatsAppBusinessConversation conversation;
    private WhatsAppBusinessPricing pricing;
    private List<WhatsAppBusinessError> errors;
}
