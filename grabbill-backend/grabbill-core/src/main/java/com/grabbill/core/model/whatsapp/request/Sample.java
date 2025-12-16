package com.grabbill.core.model.whatsapp.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Sample {
    private long fileSize;
    private String fileData;
    private String fileMimeType;
}
