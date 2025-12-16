package com.grabbill.core.service.whatsapp;

/**
 * @author seez
 */
public enum WhatsAppErrorCode {
    // whatsapp related business exception
    GRB1501("WABA login failed"),
    GRB1502("WABA webhook unregistered failed"),
    GRB1503("WABA webhook registered failed"),
    GRB1504("WABA get template failed"),
    GRB1505("WABA create template failed"),
    GRB1506("WABA delete template failed"),
    GRB1507("WABA delete template not found");

    private final String errorMessage;

    WhatsAppErrorCode(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
