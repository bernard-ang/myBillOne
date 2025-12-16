package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsAppBusinessConversation {
    private String id;
    private String expiration_timestamp;
    private WhatsAppBusinessOrigin origin;

}
