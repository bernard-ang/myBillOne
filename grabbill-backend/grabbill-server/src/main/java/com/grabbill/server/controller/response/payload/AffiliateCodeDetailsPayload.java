package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AffiliateCode;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AffiliateCodeDetailsPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String code;

    private OffsetDateTime createdTime;

    private OffsetDateTime lastModifiedTime;


    public static AffiliateCodeDetailsPayload from(
            final AffiliateCode affiliateCode
    ) {
        AffiliateCodeDetailsPayload payload = new AffiliateCodeDetailsPayload();

        payload.setId(affiliateCode.getId());
        payload.setName(affiliateCode.getName());
        payload.setCode(affiliateCode.getCode());
        payload.setCreatedTime(affiliateCode.getCreatedDate());
        payload.setLastModifiedTime(affiliateCode.getLastModifiedDate());

        return payload;
    }

}
