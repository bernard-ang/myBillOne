package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author seez
 */
@Data
public class NameCheckPayload implements ApiPayload {
    private boolean exist;

    public static NameCheckPayload from(boolean exist) {
        NameCheckPayload instance = new NameCheckPayload();
        instance.setExist(exist);
        return instance;
    }
}
