package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class SmsActivityCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

}
