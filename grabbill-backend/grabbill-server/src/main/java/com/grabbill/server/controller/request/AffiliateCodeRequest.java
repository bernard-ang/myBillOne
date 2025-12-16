package com.grabbill.server.controller.request;

import com.grabbill.core.entity.AffiliateCode;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class AffiliateCodeRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;


    public void to(final AffiliateCode affiliateCode) {
        affiliateCode.setName(this.name);
        affiliateCode.setCode(this.code);
    }

}
