package com.grabbill.engine.service.sms;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author michaellow
 */
@Data
public class SmsProperties {

    @Value("${sms.gateway.url:http://110.4.44.41:11009/cgi-bin/sendsms}")
    private String url;
    @Value("${sms.gateway.username}")
    private String username;
    @Value("${sms.gateway.password}")
    private String password;
    @Value("${sms.test-mode:true}")
    private boolean testMode;

}
