package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @author michaellow
 */
@Data
public class SmsCreditTopupRequest {

    @NotNull
    private Integer optionId;

}
