package com.grabbill.core.repository;

import com.grabbill.core.entity.AccountSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface AccountSubscriptionRepository extends JpaRepository<AccountSubscription, Integer> {
    Page<AccountSubscription> findByAccountId(Integer accountId, Pageable pageable);

    Optional<AccountSubscription> findByAccountIdAndId(
            Integer accountId,
            Integer subscriptionId
    );

    Optional<AccountSubscription> findByAccountIdAndEndDateIsNull(
            Integer accountId
    );

    List<AccountSubscription> findAllByCycleEndDateIsLessThanEqualAndEndDateIsNull(
            OffsetDateTime end
    );

    List<AccountSubscription> findAllByCycleEndDateIsGreaterThanEqualAndCycleEndDateIsLessThanEqualAndEndDateIsNull(
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<AccountSubscription> findAllByEndDateIsNull();

    List<AccountSubscription> findByAccountIdOrderByCreatedDateAsc(
            Integer accountId
    );

}
