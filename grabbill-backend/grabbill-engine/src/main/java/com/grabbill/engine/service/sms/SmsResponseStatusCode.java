package com.grabbill.engine.service.sms;

import lombok.Getter;

/**
 * @author michaellow
 */
@Getter
public enum SmsResponseStatusCode {

    OK(0, "OK. No error encountered."),
    AUTH_FAIL(1, "Authorization failed. Invalid GW-username or GW-password."),
    INSUFFICIENT_BAL(2, "Insufficient balance. Not enough credit in the account to send to ALL receivers."),
    UNAUTHORIZED_NUMBER(3, "Unauthorized destination number."),
    DEST_NUMBER_NOT_WHITELISTED(4, "At least one of the destination numbers is not white listed."),
    DEST_NUMBER_BLACKLISTED(5, "At least one of the destination numbers is black listed."),
    NO_DEST_NUMBER(6, "No destination number specified."),
    SENDER_ID_NOT_FOUND(7, "Sender ID not found."),
    INVALID_UDH(9, "Invalid UDH field."),
    INVALID_MCLASS(10, "Invalid mclass field."),
    INVALID_VALIDITY(17, "Invalid validity field."),
    INVALID_CHARSET(19, "Invalid charset on sender or message body."),
    INSUFFICIENT_HEADERS(20, "Insufficient headers for sending SMS."),
    EMPTY_GATEWAY(23, "Empty GW-text."),
    UNKNOWN_ERROR(24, "Unknown error."),
    TOO_MANY_RECEIVERS(27, "Too many receivers in gw-to."),
    INVALID_RECEIVER(28, "Invalid receiver."),
    MSG_BODY_TOO_LONG(29, "Message body is too long."),
    MSG_THROTTLED(32, "Message throttled."),
    INVALID_REQUEST(34, "Invalid request received."),
    INVALID_SENDER_LENGTH(37, "Invalid sender id length used."),
    SYSTEM_DOWN(40, "System down for maintenance, please try again."),
    SENDER_NOT_ALLOWED(44, "Sender not allowed."),
    SYSTEM_ERROR(45, "System error, please try again."),
    CONTENT_NOT_ALLOWED(50, "SMS content is not allowed.");

    private int code;
    private String description;


    SmsResponseStatusCode(
            final int code,
            final String description
    ) {
        this.code = code;
        this.description = description;
    }

    public static SmsResponseStatusCode from(final String code) {
        int codeString = Integer.parseInt(code);
        for (SmsResponseStatusCode value : SmsResponseStatusCode.values()) {
            if (codeString == value.code) {
                return value;
            }
        }
        return null;
    }

}
