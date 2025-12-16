package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.SubscriptionMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface AccountSubscriptionService {
    Page<AccountSubscription> getSubscriptionsByAccount(Account account, Pageable pageable);

    Optional<AccountSubscription> getActiveSubscriptionByAccountId(Integer accountId);

    Optional<AccountSubscription> getSubscriptionByIdAndAccountId(Account account, Integer subscriptionId);

    AccountSubscription getLatestSubscriptionByAccountId(Integer accountId);

    List<AccountSubscription> getAllActiveSubscriptions();

    List<AccountSubscription> getAllActiveSubscriptionsByCycleEndDateBefore(OffsetDateTime end);

    List<AccountSubscription> getAllActiveSubscriptionsByCycleEndDateBetween(OffsetDateTime start, OffsetDateTime end);

    AccountSubscription save(AccountSubscription accountSubscription);

    AccountSubscription createNewPersistent(
            Account account,
            SubscriptionMode subscriptionMode,
            Plan plan,
            BasePlanOption storageOption,
            BasePlanOption transactionalEmailOption,
            BasePlanOption emailCampaignOptions,
            PromoCode promoCode
    );

    AccountSubscription createNewTransient(
            Account account,
            SubscriptionMode subscriptionMode,
            Plan plan,
            BasePlanOption storageOption,
            BasePlanOption transactionalEmailOption,
            BasePlanOption emailCampaignOptions
    );

    AccountSubscription renewMonthlyUsageCycle(AccountSubscription accountSubscription, OffsetDateTime now);

    AccountSubscription terminate(AccountSubscription accountSubscription, OffsetDateTime now);

    boolean isMaturedForAnnualCharge(AccountSubscription accountSubscription, OffsetDateTime now);

    double getMonthlyRate(AccountSubscription accountSubscription);

    double getAnnuallyRate(AccountSubscription accountSubscription);

}
