package com.grabbill.core.model.whatsapp.components;

import com.grabbill.core.model.whatsapp.request.enums.ButtonType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Button {
    private ButtonType type;
    private String text;
    private String url;
    private String[] example;
}
