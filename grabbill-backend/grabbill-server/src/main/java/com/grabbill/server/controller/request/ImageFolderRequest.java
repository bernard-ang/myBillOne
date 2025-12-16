package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author michaellow
 */
@Data
public class ImageFolderRequest {

    @NotBlank
    private String name;

}
