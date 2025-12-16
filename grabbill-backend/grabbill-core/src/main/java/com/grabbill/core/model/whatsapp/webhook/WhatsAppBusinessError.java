package com.grabbill.core.model.whatsapp.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsAppBusinessError {
    private long code;
    private String title;
    private String message;
    private WhatsAppBusinessErrorData error_data;
}
