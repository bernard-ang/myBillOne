package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemplatesPayload implements ApiPayload {
    private List<RefreshTemplateResponse> templates = new ArrayList<>();

    public static TemplatesPayload from(final List<RefreshTemplateResponse> templates) {
        TemplatesPayload payload = new TemplatesPayload();
        payload.setTemplates(templates);
        return payload;
    }
}
