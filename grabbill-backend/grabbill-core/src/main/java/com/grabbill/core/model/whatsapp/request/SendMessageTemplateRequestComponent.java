package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SendMessageTemplateRequestComponent {
    private String type;
    private String sub_type;
    private String index;

    @Builder.Default
    private List<SendMessageTemplateRequestComponentParameter> parameters = new ArrayList<>();
}
