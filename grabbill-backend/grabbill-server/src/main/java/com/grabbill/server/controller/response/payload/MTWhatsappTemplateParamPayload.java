package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailWhatsappTemplateParam;
import com.grabbill.core.entity.MTWhatsappTemplateParam;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTWhatsappTemplateParamPayload implements ApiPayload {

    private Long id;
    private String index;
    private String field;


    public void copyFrom(final MTWhatsappTemplateParam param) {
        this.setId(param.getId());
        this.setIndex(param.getIndex());
        this.setField(param.getField());
    }

    public void copyFrom(final MTTransactionalEmailWhatsappTemplateParam param) {
        this.setId(param.getId());
        this.setIndex(param.getIndex());
        this.setField(param.getField());
    }

}
