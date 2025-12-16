package com.grabbill.core.repository;

import com.grabbill.core.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface PlanRepository extends JpaRepository<Plan, Integer> {
}
