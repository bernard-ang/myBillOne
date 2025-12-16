package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class RootImageFolderPayload implements ApiPayload {

    private List<ImagePayload> images = new ArrayList<>();

    private List<ImageFolderPayload> imageFolders = new ArrayList<>();

    private int imageCount;

}
