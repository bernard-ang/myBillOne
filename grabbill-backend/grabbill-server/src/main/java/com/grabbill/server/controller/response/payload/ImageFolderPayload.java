package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Image;
import com.grabbill.core.entity.ImageFolder;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michael
 */
@Data
public class ImageFolderPayload implements ApiPayload {

    private Long id;

    private String name;

    private List<ImagePayload> images = new ArrayList<>();


    public static ImageFolderPayload from(final ImageFolder imageFolder) {
        ImageFolderPayload target = new ImageFolderPayload();
        target.setId(imageFolder.getId());
        target.setName(imageFolder.getName());

        for (Image image : imageFolder.getImages()) {
            target.getImages().add(ImagePayload.from(image));
        }

        return target;
    }

}
