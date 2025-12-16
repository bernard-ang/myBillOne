package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.model.plan.EmailCampaignUsage;
import com.grabbill.core.model.plan.StorageUsage;
import com.grabbill.core.model.plan.TransactionalEmailUsage;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface PlanUsageService {

    boolean isStorageUsageExceeded(
            Account account,
            long additionalSize
    );

    StorageUsage calcStorageUsage(
            Account account,
            AccountSubscription accountSubscription
    );

    boolean isTransactionalEmailUsageExceeded(
            Account account,
            long additionalEmailCount
    );

    TransactionalEmailUsage calcTransactionalEmailUsage(
            Account account,
            AccountSubscription accountSubscription
    );

    boolean isEmailCampaignUsageExceeded(
            Account account,
            long additionalEmailCount
    );

    EmailCampaignUsage calcEmailCampaignUsage(
            Account account,
            AccountSubscription accountSubscription
    );

    long getSmsRemainingCredits(Account account);

    long getSmsTotalCreditsUsed(Account account, OffsetDateTime from, OffsetDateTime to);

}
