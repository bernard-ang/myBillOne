package com.grabbill.core.model.whatsapp.request.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ComponentType {
    HEADER("HEADER"),
    BODY("BODY"),
    FOOTER("FOOTER"),
    BUTTONS("BUTTONS"),
    ;

    @JsonValue
    private String type;

    ComponentType(String type) {
        this.type = type;
    }
}
