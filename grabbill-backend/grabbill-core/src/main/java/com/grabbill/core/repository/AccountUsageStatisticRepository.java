package com.grabbill.core.repository;

import com.grabbill.core.entity.AccountUsageStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * @author michaellow
 */
public interface AccountUsageStatisticRepository extends JpaRepository<AccountUsageStatistic, Integer> {

    Optional<AccountUsageStatistic> findByAccountId(Integer accountId);

}
