package com.grabbill.server.controller.request;

import com.grabbill.core.entity.MTWhatsAppTemplate;
import com.grabbill.core.entity.MTWhatsappTemplateParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppTemplateRequest {

    private String whatsAppTemplateName;

    private List<MTWhatsappTemplateParamRequest> whatsAppTemplateParams = new ArrayList<>();


    public void to(final MTWhatsAppTemplate template) {
        template.setWhatsAppTemplateName(this.whatsAppTemplateName);

        List<MTWhatsappTemplateParam> targetWhatsappTemplateParams = template.getMtWhatsappTemplateParams();
        int j = 0;
        for (; j < whatsAppTemplateParams.size(); j++) {
            if (targetWhatsappTemplateParams.size() > j) {
                whatsAppTemplateParams.get(j).to(targetWhatsappTemplateParams.get(j));

            } else {
                MTWhatsappTemplateParam param = new MTWhatsappTemplateParam();
                param.setMtWhatsAppTemplate(template);
                whatsAppTemplateParams.get(j).to(param);
                template.getMtWhatsappTemplateParams().add(param);
            }
        }

        int currentParamSize = targetWhatsappTemplateParams.size();
        int newParamSize = whatsAppTemplateParams.size();
        for (; j < currentParamSize; j++) {
            template.getMtWhatsappTemplateParams().remove(newParamSize);
        }
    }

}
