package com.grabbill.server.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author michaellow
 **/
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrabbillApiResponse {

    private String apiVersion;

    private ApiPayload data;

}
