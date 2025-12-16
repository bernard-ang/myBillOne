package com.grabbill.core.service;

import com.grabbill.core.entity.BasePlanOption;
import com.grabbill.core.entity.Plan;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class PlanServiceImpl implements PlanService {

    @Autowired
    private PlanRepository planRepository;


    @Override
    public Optional<Plan> getById(String id) {
        return planRepository.findById(Integer.valueOf(id));
    }

    @Override
    public List<Plan> getAll() {
        return planRepository.findAll();
    }

    @Override
    public BasePlanOption getBasePlanOption(
            final Long targetSize,
            final List<? extends BasePlanOption> options,
            final String type
    ) {
        BasePlanOption basePlanOption = null;
        for (BasePlanOption option : options) {
            if (option.getSize().equals(targetSize)) {
                basePlanOption = option;
                break;
            }
        }
        if (basePlanOption == null) {
            throw new GrabbillException("Invalid " + type + " size [" + targetSize + "]!");
        }

        return basePlanOption;
    }
}
