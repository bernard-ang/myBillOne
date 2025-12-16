package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class EmbeddedLinkClickSummaryPayload implements ApiPayload {

    private int totalClicks;
    private int totalUniqueClicks;

}
