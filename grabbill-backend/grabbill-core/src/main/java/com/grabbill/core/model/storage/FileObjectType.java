package com.grabbill.core.model.storage;

/**
 * @author michaellow
 */
public enum FileObjectType {

    DIGITAL_FILING("dgfiling"),

    TRANSACTIONAL_EMAIL("trxemail"),

    EMAIL_CAMPAIGN("ecemail"),

    WHATSAPP("whatsapp"),

    IMAGES("images");

    private final String shortname;


    FileObjectType(final String shortname) {
        this.shortname = shortname;
    }

    public String getShortname() {
        return this.shortname;
    }

}
