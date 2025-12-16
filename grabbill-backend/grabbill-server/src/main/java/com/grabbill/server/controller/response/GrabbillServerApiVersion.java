package com.grabbill.server.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author michaellow
 */
@Getter
@AllArgsConstructor
public enum GrabbillServerApiVersion implements ApiVersion {

    V1("v1");

    String version;

}
