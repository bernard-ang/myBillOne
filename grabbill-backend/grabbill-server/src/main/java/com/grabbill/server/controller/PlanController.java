package com.grabbill.server.controller;

import com.grabbill.core.entity.Plan;
import com.grabbill.core.service.PlanService;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.PlanPayload;
import com.grabbill.server.controller.response.payload.PlansPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides endpoint to retrieve plans.
 *
 * @author seez
 */
@RestController
@RequestMapping("/plans")
public class PlanController {
    @Autowired
    private PlanService planService;

    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getPlans() {
        List<Plan> plans = planService.getAll();
        List<PlanPayload> planPayloads = plans.stream().map(PlanPayload::from).collect(Collectors.toList());

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new PlansPayload(planPayloads)
        );
        return ResponseEntity.ok().body(response);
    }
}
