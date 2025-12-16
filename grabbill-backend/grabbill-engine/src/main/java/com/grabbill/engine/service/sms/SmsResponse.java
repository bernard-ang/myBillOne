package com.grabbill.engine.service.sms;

import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsResponse {

    private String raw;
    private SmsResponseStatusCode statusCode;
    private String messageId;
    private int smsSplit;
    private String errorMessage;

}
