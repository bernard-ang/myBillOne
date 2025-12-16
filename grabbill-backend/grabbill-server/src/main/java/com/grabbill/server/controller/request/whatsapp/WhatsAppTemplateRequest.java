package com.grabbill.server.controller.request.whatsapp;

import com.grabbill.core.model.whatsapp.request.enums.Category;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author seez
 */
@Data
public class WhatsAppTemplateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    private Category category;

    private boolean hasAttachment;

    @Size(max = 255)
    private String body;

    @Size(max = 255)
    private String footer;

    private boolean hasAcknowledgementButton;
}
