package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class HostedPaymentUIPayload implements ApiPayload {

    private String url;


    public static HostedPaymentUIPayload from(final String url) {
        HostedPaymentUIPayload payload = new HostedPaymentUIPayload();
        payload.setUrl(url);
        return payload;
    }

}
