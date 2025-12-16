package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author michaellow
 */
@Data
@AllArgsConstructor
public class PrivilegesPayload implements ApiPayload {

    private Map<Integer, String> privileges = new HashMap<>();

}
