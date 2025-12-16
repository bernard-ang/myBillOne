package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Image;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class ImagePayload implements ApiPayload {

    private Long id;

    private String filename;

    private String fileType;

    private Long fileSize;

    private String linkId;

    private String externalUrl;


    public static ImagePayload from(final Image image) {
        ImagePayload target = new ImagePayload();
        target.setId(image.getId());
        target.setFilename(image.getFilename());
        target.setFileType(image.getFileType());
        target.setFileSize(image.getFileSize());
        target.setLinkId(image.getLinkId());

        return target;
    }

}
