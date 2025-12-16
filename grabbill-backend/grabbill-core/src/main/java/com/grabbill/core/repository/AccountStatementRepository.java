package com.grabbill.core.repository;

import com.grabbill.core.entity.AccountStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;


/**
 * @author michaellow
 */
public interface AccountStatementRepository extends JpaRepository<AccountStatement, Integer> {

    Page<AccountStatement> findAllByAccountId(Integer accountId, Pageable pageable);

    Page<AccountStatement> findAllByAccountIdAndStartDateIsAfterAndEndDateIsBefore(
            Integer accountId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

}
