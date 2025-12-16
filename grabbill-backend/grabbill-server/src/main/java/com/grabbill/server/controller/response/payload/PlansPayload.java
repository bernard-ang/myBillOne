package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author seez
 */
@Data
@AllArgsConstructor
public class PlansPayload implements ApiPayload {

    private List<PlanPayload> plans;

}
