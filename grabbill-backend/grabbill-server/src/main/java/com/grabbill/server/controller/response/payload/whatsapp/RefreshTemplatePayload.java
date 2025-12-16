package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponseComponent;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RefreshTemplatePayload extends RefreshTemplateResponse implements ApiPayload {
    public static RefreshTemplatePayload from(final RefreshTemplateResponse response) {
        RefreshTemplatePayload apiResponse = new RefreshTemplatePayload();
        apiResponse.setId(response.getId());
        apiResponse.setName(response.getName());
        apiResponse.setLanguage(response.getLanguage());
        apiResponse.setStatus(response.getStatus());
        apiResponse.setCategory(response.getCategory());
        apiResponse.setWaTemplateId(response.getWaTemplateId());
        apiResponse.setWhatsappId(response.getWhatsappId());

        List<RefreshTemplateResponseComponent> component = new ArrayList<>(response.getComponent());
        apiResponse.setComponent(component);

        apiResponse.setNoOfParams(response.getNoOfParams());
        apiResponse.setActive(response.isActive());
        apiResponse.setUpdatedBy(response.getUpdatedBy());
        apiResponse.setUpdatedDate(response.getUpdatedDate());
        return apiResponse;
    }
}
