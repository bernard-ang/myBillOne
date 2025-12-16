package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseFile;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class BaseFilePayload implements ApiPayload {

    Long id;
    String name;
    Long fileSize;
    String fileType;

    public void copyFrom(final BaseFile file) {
        this.setName(file.getName());
        this.setFileType(file.getFileType());
        this.setFileSize(file.getFileSize());
    }

}
