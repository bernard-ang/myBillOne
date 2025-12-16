package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsAppBusinessMetadata {
    private String display_phone_number;
    private String phone_number_id;
}
