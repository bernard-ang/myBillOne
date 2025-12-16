package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsUsageSummaryPayload implements ApiPayload {

    private Long sent = 0L;
    private Long error = 0L;
    private Integer creditUsed = 0;

}
