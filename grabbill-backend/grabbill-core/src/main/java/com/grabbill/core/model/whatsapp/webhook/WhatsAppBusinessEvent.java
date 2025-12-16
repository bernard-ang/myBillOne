package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class WhatsAppBusinessEvent {
    private String object;
    private List<WhatsAppBusinessEntry> entry;
}
