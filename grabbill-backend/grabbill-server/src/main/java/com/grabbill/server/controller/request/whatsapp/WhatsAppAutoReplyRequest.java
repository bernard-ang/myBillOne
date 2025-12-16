package com.grabbill.server.controller.request.whatsapp;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author seez
 */
@Data
public class WhatsAppAutoReplyRequest {

    @NotBlank
    @Size(max = 255)
    private String message;
}
