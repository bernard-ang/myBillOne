package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import lombok.Data;

import java.util.List;

/**
 * @author seez
 */
@Data
public class WhatsAppTemplatesPayload implements ApiPayload {

    private List<RefreshTemplateResponse> templates;

    public static WhatsAppTemplatesPayload from(
            final List<RefreshTemplateResponse> templates
    ) {
        WhatsAppTemplatesPayload instance = new WhatsAppTemplatesPayload();
        instance.setTemplates(templates);

        return instance;
    }

}
