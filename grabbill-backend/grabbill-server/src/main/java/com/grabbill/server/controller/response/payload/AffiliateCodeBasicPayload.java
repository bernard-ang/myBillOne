package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AffiliateCode;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AffiliateCodeBasicPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String code;

    private OffsetDateTime createdTime;

    private OffsetDateTime lastModifiedTime;


    public static AffiliateCodeBasicPayload from(
            final AffiliateCode affiliateCode
    ) {
        AffiliateCodeBasicPayload payload = new AffiliateCodeBasicPayload();

        payload.setId(affiliateCode.getId());
        payload.setName(affiliateCode.getName());
        payload.setCode(affiliateCode.getCode());
        payload.setCreatedTime(affiliateCode.getCreatedDate());
        payload.setLastModifiedTime(affiliateCode.getLastModifiedDate());

        return payload;
    }

}
