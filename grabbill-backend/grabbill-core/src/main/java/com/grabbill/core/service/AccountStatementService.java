package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountStatement;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AccountUsageStatistic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface AccountStatementService {

    Page<AccountStatement> getAllByAccountId(
            Integer accountId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    AccountStatement newStatement(
            Account account,
            AccountSubscription accountSubscription,
            AccountUsageStatistic usageStatistic
    );

    AccountStatement save(AccountStatement accountStatement);

}
