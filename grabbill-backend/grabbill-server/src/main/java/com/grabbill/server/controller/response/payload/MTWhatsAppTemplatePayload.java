package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppTemplate;
import com.grabbill.core.entity.MTWhatsappTemplateParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppTemplatePayload {

    private String whatsAppTemplateName;

    private List<MTWhatsappTemplateParamPayload> whatsAppTemplateParams;


    public static MTWhatsAppTemplatePayload from(final MTWhatsAppTemplate mtWhatsAppTemplate) {

        MTWhatsAppTemplatePayload payload = new MTWhatsAppTemplatePayload();
        payload.setWhatsAppTemplateName(mtWhatsAppTemplate.getWhatsAppTemplateName());

        List<MTWhatsappTemplateParamPayload> params = new ArrayList<>();
        for (MTWhatsappTemplateParam mtWhatsappTemplateParam : mtWhatsAppTemplate.getMtWhatsappTemplateParams()) {
            MTWhatsappTemplateParamPayload paramPayload = new MTWhatsappTemplateParamPayload();
            paramPayload.copyFrom(mtWhatsappTemplateParam);
            params.add(paramPayload);
        }
        payload.setWhatsAppTemplateParams(params);

        return payload;
    }

}
