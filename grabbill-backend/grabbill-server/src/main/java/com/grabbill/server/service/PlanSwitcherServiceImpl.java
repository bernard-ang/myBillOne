package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.service.AccountSubscriptionService;
import com.grabbill.core.service.AccountUsageStatisticService;
import com.grabbill.core.service.PlanService;
import com.grabbill.server.controller.request.UserPlanUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public class PlanSwitcherServiceImpl implements PlanSwitcherService {

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private PlanService planService;


    @Override
    public AccountSubscription terminateAccountSubscription(
            final AccountSubscription accountSubscription,
            final OffsetDateTime terminationDateTime
    ) {
        accountSubscription.setEndDate(terminationDateTime);
        return accountSubscriptionService.save(accountSubscription);
    }

    @Override
    public AccountSubscription toNewAccountSubscriptionPersistent(
            final Account account,
            final UserPlanUpdateRequest request,
            final Plan plan,
            final PromoCode promoCode
    ) {
        BasePlanOption storageOption = planService.getBasePlanOption(
                request.getStorageSize(), plan.getStorageOptions(), "storage");
        BasePlanOption transactionalEmailOption = planService.getBasePlanOption(
                request.getTransactionalEmailSize(), plan.getTransactionalEmailOptions(), "transactional email");
        BasePlanOption emailCampaignOptions = planService.getBasePlanOption(
                request.getEmailCampaignSize(), plan.getEmailCampaignOptions(), "email campaign");

        return accountSubscriptionService.createNewPersistent(
                account,
                request.getSubscriptionMode(),
                plan,
                storageOption,
                transactionalEmailOption,
                emailCampaignOptions,
                promoCode
        );
    }

    @Override
    public AccountSubscription toNewAccountSubscriptionTransient(
            final Account account,
            final UserPlanUpdateRequest request,
            final Plan plan
    ) {
        BasePlanOption storageOption = planService.getBasePlanOption(
                request.getStorageSize(), plan.getStorageOptions(), "storage");
        BasePlanOption transactionalEmailOption = planService.getBasePlanOption(
                request.getTransactionalEmailSize(), plan.getTransactionalEmailOptions(), "transactional email");
        BasePlanOption emailCampaignOptions = planService.getBasePlanOption(
                request.getEmailCampaignSize(), plan.getEmailCampaignOptions(), "email campaign");

        return accountSubscriptionService.createNewTransient(
                account,
                request.getSubscriptionMode(),
                plan,
                storageOption,
                transactionalEmailOption,
                emailCampaignOptions
        );
    }

    @Override
    public void updateNewPlanLimitsToAccountUsageStatistic(
            final AccountUsageStatistic accountUsageStatistic,
            final AccountSubscription newAccountSubscription
    ) {
        // DO NOT accountUsageStatistic.setTotalStorageUsed(xxx), this is supposed to keep rolling
        accountUsageStatistic.setTotalTransactionalEmailSent(0L);
        accountUsageStatistic.setTotalEmailCampaignSent(0L);

        accountUsageStatistic.setMaxStorageSize(newAccountSubscription.getStorageSize());
        accountUsageStatistic.setMaxTransactionalEmailSent(newAccountSubscription.getTransactionalEmailSize());
        accountUsageStatistic.setMaxEmailCampaignSent(newAccountSubscription.getEmailCampaignSize());
        accountUsageStatisticService.save(accountUsageStatistic);
    }

}
