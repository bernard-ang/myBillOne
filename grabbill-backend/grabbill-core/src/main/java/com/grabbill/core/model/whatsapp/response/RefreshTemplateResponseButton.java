package com.grabbill.core.model.whatsapp.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RefreshTemplateResponseButton {
    private String type;
    private String text;
    private String url;
    private String phone_Number;
}
