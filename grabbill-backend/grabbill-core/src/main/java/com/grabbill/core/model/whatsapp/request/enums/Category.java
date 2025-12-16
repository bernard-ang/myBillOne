package com.grabbill.core.model.whatsapp.request.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    /**
     * @deprecated do not use. undocumented
     */
    AUTHENTICATION("AUTHENTICATION"),
    MARKETING("MARKETING"),
    UTILITY("UTILITY");

    @JsonValue
    private String category;

    Category(String category) {
        this.category = category;
    }
}
