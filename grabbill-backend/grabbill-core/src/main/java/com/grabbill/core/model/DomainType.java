package com.grabbill.core.model;

/**
 * @author michaellow
 */
public enum DomainType {

    AUTH,

    DIGITAL_FILING,

    TRANSACTIONAL_EMAIL,

    MT_TRANSACTIONAL_EMAIL,

    EMAIL_CAMPAIGN,

    SMS,

    WHATSAPP,

    MT_WHATSAPP,

    USER,

    USER_PROFILE,

    MAIL_SERVER,

    IMAGE,

    IMAGE_FOLDER,

    ACCOUNT,

    PLAN,

    CONTACT,

    CONTACT_GROUP,

    CONTACT_FIELD,

    BOUNCED_EMAIL,

    WHATSAPP_TEMPLATE,

    UNSUBSCRIBED_EMAIL,

    BILLING_INFO,

    INVOICE,

    PAYMENT_METHOD,

    AFFILIATE_CODE,

    PROMO_CODE,

    JOB;


    public static DomainType from(final String value) {
        for (DomainType domainType : DomainType.values()) {
            if (domainType.name().equalsIgnoreCase(value)) {
                return domainType;
            }
        }

        return null;
    }

}
