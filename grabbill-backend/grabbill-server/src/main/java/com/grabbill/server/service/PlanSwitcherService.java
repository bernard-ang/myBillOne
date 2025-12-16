package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.request.UserPlanUpdateRequest;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface PlanSwitcherService {

    AccountSubscription terminateAccountSubscription(
            AccountSubscription accountSubscription,
            OffsetDateTime terminationDateTime
    );

    AccountSubscription toNewAccountSubscriptionPersistent(
            Account account,
            UserPlanUpdateRequest request,
            Plan plan,
            PromoCode promoCode
    );

    AccountSubscription toNewAccountSubscriptionTransient(
            Account account,
            UserPlanUpdateRequest request,
            Plan plan
    );

    void updateNewPlanLimitsToAccountUsageStatistic(
            AccountUsageStatistic accountUsageStatistic,
            AccountSubscription newAccountSubscription
    );

}
