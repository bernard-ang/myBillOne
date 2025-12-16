package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ImagesPayload implements ApiPayload {

    private List<ImagePayload> images = new ArrayList<>();

}
