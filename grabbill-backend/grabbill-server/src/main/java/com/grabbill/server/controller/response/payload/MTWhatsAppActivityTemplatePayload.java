package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppActivityTemplate;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppActivityTemplatePayload {

    private String whatsappTemplateName;
    private String whatsappBodyContent;
    private boolean whatsappDocument;
    private String whatsappFooterContent;
    private String whatsappButton;


    public static MTWhatsAppActivityTemplatePayload from(final MTWhatsAppActivityTemplate mtWhatsAppActivityTemplate) {
        MTWhatsAppActivityTemplatePayload instance = new MTWhatsAppActivityTemplatePayload();
        instance.setWhatsappTemplateName(mtWhatsAppActivityTemplate.getWhatsAppTemplateName());
        instance.setWhatsappDocument(mtWhatsAppActivityTemplate.getWhatsAppDocument() != null && mtWhatsAppActivityTemplate.getWhatsAppDocument());
        instance.setWhatsappBodyContent(mtWhatsAppActivityTemplate.getWhatsAppBodyContent());
        instance.setWhatsappFooterContent(mtWhatsAppActivityTemplate.getWhatsAppFooterContent());
        instance.setWhatsappButton(mtWhatsAppActivityTemplate.getWhatsAppButton());

        return instance;
    }

}
