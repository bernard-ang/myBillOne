package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsAppBusinessMessage {
    private String from;
    private String id;
    private String timestamp;
    private WhatsAppBusinessMessageText text;
    private String type;
}
