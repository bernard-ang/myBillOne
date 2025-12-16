package com.grabbill.server;


/**
 * @author michaellow
 */
public enum GrabbillServerErrorCode {

    // general server errors, GRB0001 - GRB0999
    GRB0001("Unknown server error"),
    GRB0002("Http method argument validation error"),
    GRB0003("One or more required parameters are missing"),
    GRB0004("Media type unsupported"),
    GRB0005("Media type unacceptable"),
    GRB0006("Unsupported method"),
    GRB0007("Requested resource not found"),
    GRB0008("Not implemented yet"),
    GRB0009("Token expired"),
    GRB0010("Authentication failed"),
    GRB0011("Http message not readable"),
    GRB0012("Unauthorized access"),
    GRB0013("User account has not been verified"),
    GRB0014("User account is disabled"),
    GRB0015("User account already verified"),
    GRB0016("API not supported in current deployment mode"),
    GRB0017("Forbidden access"),
    GRB0101("Unsupported external authenticator"),
    GRB0102("Failed to authorize token"),
    GRB0103("Failed to revoke authorization token"),
    GRB0999("Broken reference"),

    // general business exception
    GRB1001("Email already associated with another user account"),
    GRB1002("User does not exist"),
    GRB1003("Password does not match"),
    GRB1004("Invalid plan"),
    GRB1005("Role does not exist"),
    GRB1006("Owner is not allowed to be delete"),
    GRB1007("Invalid email unsubscription link"),
    GRB1008("Invalid size"),
    GRB1009("Invalid email verification code"),
    GRB1010("Image not found"),
    GRB1011("Image folder not found"),
    GRB1012("Image type if not supported"),
    GRB1013("Image folder already exists"),
    GRB1014("Image with same name already exists"),
    GRB1015("Google 2 factor authentication not enabled"),
    GRB1016("Email based 2 factor authentication not enabled"),
    GRB1020("No active plan subscription found for account"),
    GRB1021("Account not found"),
    GRB1022("Job not found"),
    GRB1023("Account already has an active plan subscription"),
    GRB1024("Unable to downgrade plan as exceeded max storage size limit"),
    GRB1025("Activity not in the right status for retry"),
    GRB1026("Email already unsubscribed"),
    GRB1027("Merchant account is not configured"),
    GRB1028("Switch plan is not allowed with unpaid invoice(s)"),
    GRB1029("Unauthorized access by user without respective type code"),
    GRB1030("Invalid embedded link click request"),
    GRB1031("Embedded link not found"),
    GRB1032("Affiliate code not found"),
    GRB1033("Affiliate code already exist"),
    GRB1034("Affiliate code is referenced"),
    GRB1035("Affiliate code is invalid"),
    GRB1036("No usage statistic found for account"),

    GRB1037("Promo code not found"),
    GRB1038("Promo code already exist"),
    GRB1039("Invalid discount type for promo code"),
    GRB1040("Failed to create promo code coupon (Stripe)"),
    GRB1041("Failed to update promo code coupon (Stripe)"),
    GRB1042("Role already exist"),
    GRB1043("User is not owner"),
    GRB1044("Owner role is not editable"),
    GRB1045("Role with active reference is not deletable"),


    // whatsapp related business exception
    GRB1500("WABA unknown error"),
    GRB1501("WABA login failed"),
    GRB1502("WABA webhook unregistered failed"),
    GRB1503("WABA webhook registered failed"),
    GRB1504("WABA get template failed"),
    GRB1505("WABA create template failed"),
    GRB1506("WABA delete template failed"),
    GRB1507("WABA delete template not found"),

    GRB1901("Storage plan limit exceeded"),
    GRB1902("Transactional email plan limit exceeded"),
    GRB1903("Insufficient sms credits"),


    // digital filing related business exception
    GRB2001("Digital filing type not found"),
    GRB2002("Digital filing type deletion failed"),
    GRB2003("Digital filing activity not found"),
    GRB2004("Digital filing activity file purging failed"),
    GRB2005("Digital filing activity file upload failed"),
    GRB2006("Digital filing activity update failed"),
    GRB2007("Digital filing activity deletion failed"),
    GRB2008("Digital filing file not found"),
    GRB2009("Digital filing activity has no uploaded file"),
    GRB2010("Digital filing activity is not processed"),
    GRB2011("Digital filing activity files deletion failed"),
    GRB2012("Failed to generate digital filing report"),

    // transactional email related business exception
    GRB3001("Transactional email type not found"),
    GRB3002("Transactional email type deletion failed"),
    GRB3003("Transactional email activity not found"),
    GRB3004("Transactional email activity file purging failed"),
    GRB3005("Transactional email activity file upload failed"),
    GRB3006("Transactional email activity update failed"),
    GRB3007("Transactional email activity deletion failed"),
    GRB3008("Transactional email file not found"),
    GRB3009("Transactional email activity has no uploaded file"),
    GRB3010("Transactional email activity is not processed"),
    GRB3011("Transactional email activity files deletion failed"),
    GRB3012("Transactional email index row not found"),
    GRB3013("Failed to generate transactional email report"),
    GRB3014("Transaction email activity file had been expired"),

    // email campaign related business exception
    GRB4001("Email campaign type not found"),
    GRB4002("Email campaign type deletion failed"),
    GRB4003("Email campaign activity not found"),
    GRB4004("Uploaded file type is not supported"),
    GRB4005("Email campaign activity file upload failed"),
    GRB4006("Email campaign activity update failed"),
    GRB4007("Email campaign activity deletion failed"),
    GRB4008("Email campaign file not found"),
    GRB4009("Email campaign index row not found"),
    GRB4010("Failed to generate email campaign report"),
    GRB4101("Contact field not found"),
    GRB4201("Contact not found"),
    GRB4202("Invalid bulk contacts deletion request"),
    GRB4203("Invalid bulk contacts creation request"),
    GRB4301("Contact group not found"),

    // bounced email related business exception
    GRB5001("Bounced email type not found"),

    // unsubscribed email related business exception
    GRB6001("Unsubscribed email type not found"),

    // admin user related business exception
    GRB7001("Admin user not found"),
    GRB7002("Admin user is inactive"),
    GRB7003("Admin user with same email already exist"),
    GRB7004("No admin user with given email found"),
    GRB7005("Invalid email verification code"),

    // payment related business exception
    GRB8001("Payment event not found"),
    GRB8002("No invoice to process payment with"),
    GRB8003("No registered payment method"),
    GRB8004("Manage card transaction not found"),
    GRB8005("Payment grace period expired"),
    GRB8006("Payment charge failed"),

    // invoice related business exception
    GRB8101("Invoice not found"),
    GRB8102("Invalid invoice status for payment"),
    GRB8103("Failed to generate invoice report"),

    // billing information related business exception
    GRB8201("Billing Information not found"),

    // account reports related business exception
    GRB8301("Failed to generate accounts report"),
    GRB8302("Failed to generate account sales report"),

    // transactional email related business exception
    GRB9001("Sms type not found"),
    GRB9002("Sms type deletion failed"),
    GRB9003("Sms activity not found"),
    GRB9004("Sms activity update failed"),
    GRB9005("Sms activity deletion failed"),
    GRB9010("Failed to generate sms report"),
    GRB9011("Sms endpoints are disabled"),

    // transactional email related business exception
    GRB10001("WhatsApp type not found"),
    GRB10002("WhatsApp type deletion failed"),
    GRB10003("WhatsApp activity not found"),
    GRB10004("WhatsApp activity file purging failed"),
    GRB10005("WhatsApp activity file upload failed"),
    GRB10006("WhatsApp activity update failed"),
    GRB10007("WhatsApp activity deletion failed"),
    GRB10008("WhatsApp file not found"),
    GRB10009("WhatsApp activity has no uploaded file"),
    GRB10010("WhatsApp activity is not processed"),
    GRB10011("WhatsApp activity files deletion failed"),
    GRB10012("WhatsApp index row not found"),
    GRB10013("Failed to generate whatsapp report"),
    GRB10014("WhatsApp activity file had been expired");

    private final String errorMessage;

    GrabbillServerErrorCode(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
