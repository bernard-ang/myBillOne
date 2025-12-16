package com.grabbill.core.model.whatsapp.request.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ComponentFormat {
    TEXT("TEXT"),
    /**
     * @deprecated do not use. undocumented
     */
    LOCATION("LOCATION"),
    IMAGE("IMAGE"),
    /**
     * @deprecated unsupported
     */
    VIDEO("VIDEO"),
    BUTTON("BUTTON"),
    DOCUMENT("DOCUMENT");

    @JsonValue
    private String format;

    ComponentFormat(String format) {
        this.format = format;
    }
}
