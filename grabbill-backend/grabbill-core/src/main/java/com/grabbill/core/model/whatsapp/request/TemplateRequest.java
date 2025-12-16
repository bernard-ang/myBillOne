package com.grabbill.core.model.whatsapp.request;

import com.grabbill.core.model.whatsapp.components.AbstractComponent;
import com.grabbill.core.model.whatsapp.request.enums.Category;
import com.grabbill.core.model.whatsapp.request.enums.Language;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/#creating-templates
 */
@Data
@Builder
public class TemplateRequest {
    /**
     * TODO: validate only lowercase letters and underscores.
     */
    private String name;
    private Language language;
    private Category category;
    private String whatsappId;

    private List<? extends AbstractComponent> components;

    private Sample sample;
}
