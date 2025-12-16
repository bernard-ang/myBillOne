package com.grabbill.server.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author michaellow
 */
@AllArgsConstructor
@Data
public class ApiErrorMessage implements ApiPayload {

    private String errorCode;

    private String errorDetails;

}
