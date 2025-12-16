package com.grabbill.server.controller.request;

import com.grabbill.core.entity.MTWhatsappTemplateParam;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class MTWhatsappTemplateParamRequest {

    @Size(max = 2)
    private String index;

    @Size(max = 255)
    private String field;


    public void to(final MTWhatsappTemplateParam param) {
        param.setIndex(this.index);
        param.setField(this.field);
    }

}
