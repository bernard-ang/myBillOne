package com.grabbill.core.model.whatsapp.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RefreshTemplateResponseComponent {
    private String type;
    private String format;
    private String text;
    private List<RefreshTemplateResponseButton> buttons = new ArrayList<>();
}
