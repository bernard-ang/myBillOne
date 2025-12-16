package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessageTemplateRequestLanguage {
    private String code;
}
