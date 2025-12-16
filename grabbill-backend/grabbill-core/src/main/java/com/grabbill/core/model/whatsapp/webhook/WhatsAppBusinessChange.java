package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class WhatsAppBusinessChange {
    private WhatsAppBusinessValue value;
    private String field;
}
