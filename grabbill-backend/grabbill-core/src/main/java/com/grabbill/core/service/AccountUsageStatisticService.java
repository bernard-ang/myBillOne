package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AccountUsageStatistic;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface AccountUsageStatisticService {

    Optional<AccountUsageStatistic> getByAccountId(Integer accountId);

    AccountUsageStatistic save(AccountUsageStatistic accountUsageStatistic);

    AccountUsageStatistic resetByAccountId(Integer accountId);

    AccountUsageStatistic createNew(Account account);

    AccountUsageStatistic getOrCreateNew(Account account, AccountSubscription accountSubscription);

}
