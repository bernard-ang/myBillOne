package com.grabbill.server.controller.request.whatsapp;

import com.grabbill.core.entity.MTTransactionalEmailWhatsappTemplateParam;
import com.grabbill.core.entity.TransactionalEmailWhatsappTemplateParam;
import com.grabbill.core.entity.WhatsappTemplateParam;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author seez
 */
@Data
public class WhatsappTemplateParamRequest {

    @Size(max = 2)
    private String index;

    @Size(max = 255)
    private String field;


    public void to(final TransactionalEmailWhatsappTemplateParam param) {
        param.setIndex(this.index);
        param.setField(this.field);
    }

    public void to(final MTTransactionalEmailWhatsappTemplateParam param) {
        param.setIndex(this.index);
        param.setField(this.field);
    }

    public void to(final WhatsappTemplateParam param) {
        param.setIndex(this.index);
        param.setField(this.field);
    }

}
