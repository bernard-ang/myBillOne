package com.grabbill.core.repository;

import com.grabbill.core.entity.CreditsPlanOption;
import com.grabbill.core.model.CreditType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author michaellow
 */
public interface CreditsPlanOptionRepository extends JpaRepository<CreditsPlanOption, Integer> {

    List<CreditsPlanOption> findByTypeIs(CreditType creditType);

}
