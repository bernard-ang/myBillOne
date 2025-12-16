package com.grabbill.core.model.whatsapp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendMessageResponseContact {
    private String input;

    @JsonProperty("wa_id")
    private String waId;
}
