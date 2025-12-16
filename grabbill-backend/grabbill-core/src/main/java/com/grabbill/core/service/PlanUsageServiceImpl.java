package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.plan.EmailCampaignUsage;
import com.grabbill.core.model.plan.StorageUsage;
import com.grabbill.core.model.plan.TransactionalEmailUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class PlanUsageServiceImpl implements PlanUsageService {

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    @Qualifier("smsTypeService")
    private BaseTypeService<SmsType, SmsActivity> smsTypeService;

    @Autowired
    @Qualifier("smsActivityService")
    private BaseActivityService<SmsType, SmsActivity> smsActivityService;




    @Override
    public boolean isStorageUsageExceeded(
            final Account account,
            final long additionalSize
    ) {
        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            if ((target.getTotalStorageUsed() + additionalSize) > target.getMaxStorageSize()) {
                return true;
            }

        } else {
            logNoAccountUsageStatisticFound(account);
        }
        return false;
    }

    @Override
    public StorageUsage calcStorageUsage(
            final Account account,
            final AccountSubscription accountSubscription
    ) {
        StorageUsage storageUsage = new StorageUsage();
        storageUsage.setLimit(accountSubscription.getStorageSize());

        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            storageUsage.setUsed(target.getTotalStorageUsed());
        } else {
            logNoAccountUsageStatisticFound(account);
        }

        return storageUsage;
    }

    @Override
    public boolean isTransactionalEmailUsageExceeded(
            final Account account,
            final long additionalEmailCount
    ) {
        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            if ((target.getTotalTransactionalEmailSent() + additionalEmailCount) > target.getMaxTransactionalEmailSent()) {
                return true;
            }

        } else {
            logNoAccountUsageStatisticFound(account);
        }
        return false;
    }

    @Override
    public TransactionalEmailUsage calcTransactionalEmailUsage(
            final Account account,
            final AccountSubscription accountSubscription
    ) {
        TransactionalEmailUsage transactionalEmailUsage = new TransactionalEmailUsage();
        transactionalEmailUsage.setLimit(accountSubscription.getTransactionalEmailSize());

        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            transactionalEmailUsage.setSent(target.getTotalTransactionalEmailSent());
        } else {
            logNoAccountUsageStatisticFound(account);
        }

        return transactionalEmailUsage;
    }

    @Override
    public boolean isEmailCampaignUsageExceeded(
            final Account account,
            final long additionalEmailCount
    ) {
        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            if ((target.getTotalEmailCampaignSent() + additionalEmailCount) > target.getMaxEmailCampaignSent()) {
                return true;
            }

        } else {
            logNoAccountUsageStatisticFound(account);
        }
        return false;
    }

    @Override
    public EmailCampaignUsage calcEmailCampaignUsage(
            final Account account,
            final AccountSubscription accountSubscription
    ) {
        EmailCampaignUsage emailCampaignUsage = new EmailCampaignUsage();
        emailCampaignUsage.setLimit(accountSubscription.getEmailCampaignSize());

        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            emailCampaignUsage.setSent(target.getTotalEmailCampaignSent());
        } else {
            logNoAccountUsageStatisticFound(account);
        }

        return emailCampaignUsage;
    }

    @Override
    public long getSmsRemainingCredits(final Account account) {
        Optional<AccountUsageStatistic> optional = accountUsageStatisticService.getByAccountId(account.getId());
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            return target.getSmsCredit() != null ? target.getSmsCredit(): 0L;

        } else {
            logNoAccountUsageStatisticFound(account);
        }

        return -1;
    }

    @Override
    public long getSmsTotalCreditsUsed(final Account account, final OffsetDateTime from, final OffsetDateTime to) {
        List<SmsActivity> activities = smsActivityService.getAllByAccountBetween(account, from, to);
        int totalCreditsUsed = 0;
        for (SmsActivity activity : activities) {
            totalCreditsUsed += activity.getSmsCreditUsed();
        }
        return totalCreditsUsed;
    }

    private void logNoAccountUsageStatisticFound(final Account account) {
        log.error("No account usage statistic found for account [" + account.getId() + "].");
    }

}
