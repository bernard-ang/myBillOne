package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SendMessageTemplateRequest {
    private String whatsappId;
    private String to;
    /**
     * Message template name.
     */
    private String name;
    private SendMessageTemplateRequestLanguage language;
    @Builder.Default
    private List<SendMessageTemplateRequestComponent> component = new ArrayList<>();
}
