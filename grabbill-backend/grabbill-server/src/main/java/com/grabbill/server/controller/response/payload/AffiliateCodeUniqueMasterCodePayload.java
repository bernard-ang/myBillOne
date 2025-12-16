package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class AffiliateCodeUniqueMasterCodePayload implements ApiPayload {

    private String code;

    public static AffiliateCodeUniqueMasterCodePayload from(
            final String uniqueMasterCode
    ) {
        AffiliateCodeUniqueMasterCodePayload payload = new AffiliateCodeUniqueMasterCodePayload();
        payload.setCode(uniqueMasterCode);
        return payload;
    }

}
