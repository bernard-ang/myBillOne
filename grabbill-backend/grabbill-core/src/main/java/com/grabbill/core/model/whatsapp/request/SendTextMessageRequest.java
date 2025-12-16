package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendTextMessageRequest {
    private String whatsappId;
    private String to;
    private String message;
}
