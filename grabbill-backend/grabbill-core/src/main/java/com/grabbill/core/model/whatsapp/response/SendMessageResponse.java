package com.grabbill.core.model.whatsapp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SendMessageResponse {
    @JsonProperty("messaging_product")
    private String messagingProduct;

    private List<SendMessageResponseContact> contacts = new ArrayList<>();

    private List<SendMessageResponseMessage> messages = new ArrayList<>();
}
