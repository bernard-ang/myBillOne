package com.grabbill.core.service;

import com.grabbill.core.entity.BasePlanOption;
import com.grabbill.core.entity.Plan;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface PlanService {

    Optional<Plan> getById(String id);

    List<Plan> getAll();

    BasePlanOption getBasePlanOption(
            Long targetSize,
            List<? extends BasePlanOption> options,
            String type
    );

}
