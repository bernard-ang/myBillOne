package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailWhatsappTemplateParam;
import com.grabbill.core.entity.WhatsappTemplateParam;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author seez
 */
@Data
public class WhatsappTemplateParamPayload implements ApiPayload {

    private Long id;
    private String index;
    private String field;


    void copyFrom(final TransactionalEmailWhatsappTemplateParam param) {
        this.setId(param.getId());
        this.setIndex(param.getIndex());
        this.setField(param.getField());
    }

    public void copyFrom(final WhatsappTemplateParam param) {
        this.setId(param.getId());
        this.setIndex(param.getIndex());
        this.setField(param.getField());
    }

}
