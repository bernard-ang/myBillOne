package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WhatsAppBusinessValue {
    private String messaging_product;
    private WhatsAppBusinessMetadata metadata;
    private List<WhatsAppBusinessStatus> statuses;
    private List<WhatsAppBusinessMessage> messages;
}
