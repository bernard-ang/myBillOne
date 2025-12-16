package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WhatsAppBusinessEntry {
    private String id;
    private List<WhatsAppBusinessChange> changes;
}
