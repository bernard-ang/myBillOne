package com.grabbill.core.model.whatsapp.request.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ButtonType {
    /**
     * @deprecated do not use. undocumented
     */
    PHONE_NUMBER("PHONE_NUMBER"),

    URL("URL"),

    /**
     * @deprecated do not use. undocumented
     */
    QUICK_REPLY("QUICK_REPLY"),

    /**
     * @deprecated do not use. undocumented
     */
    COPY_CODE("COPY_CODE");

    @JsonValue
    private String type;

    ButtonType(String type) {
        this.type = type;
    }
}
