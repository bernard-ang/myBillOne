package com.grabbill.core.service;

import com.grabbill.core.entity.CreditsPlanOption;
import com.grabbill.core.model.CreditType;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface CreditsPlanOptionService {

    List<CreditsPlanOption> getByType(CreditType type);

    Optional<CreditsPlanOption> getById(Integer id);

}
