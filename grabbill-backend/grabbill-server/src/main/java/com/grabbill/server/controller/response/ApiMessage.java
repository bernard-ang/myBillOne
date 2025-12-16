package com.grabbill.server.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author michaellow
 */
@AllArgsConstructor
@Data
public class ApiMessage implements ApiPayload {

    private String message;

}
