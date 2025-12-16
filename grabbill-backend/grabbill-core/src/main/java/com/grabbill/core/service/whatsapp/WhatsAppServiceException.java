package com.grabbill.core.service.whatsapp;

import lombok.Getter;

@Getter
public class WhatsAppServiceException extends RuntimeException {
    WhatsAppErrorCode errorCode;

    public WhatsAppServiceException(String message) {
        super(message);
    }

    public WhatsAppServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public WhatsAppServiceException(WhatsAppErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
