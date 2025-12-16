package com.grabbill.core.model.whatsapp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsAppResponse<T> {
    private boolean status;
    private String code;
    private String message;
    private T data;
}
