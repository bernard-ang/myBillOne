package com.grabbill.engine.service.sms;

/**
 * @author michaellow
 */
public interface SmsService {

    SmsResponse send(String from, String to, String message);

}
