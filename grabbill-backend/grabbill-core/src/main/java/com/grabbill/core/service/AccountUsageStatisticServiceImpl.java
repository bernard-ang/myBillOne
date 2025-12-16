package com.grabbill.core.service;

import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AccountUsageStatistic;
import com.grabbill.core.repository.AccountUsageStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public class AccountUsageStatisticServiceImpl implements AccountUsageStatisticService {

    @Autowired
    private DeploymentProperties deploymentProperties;
    @Autowired
    private AccountUsageStatisticRepository repository;


    @Override
    public Optional<AccountUsageStatistic> getByAccountId(final Integer accountId) {
        return repository.findByAccountId(accountId);
    }

    @Override
    public AccountUsageStatistic save(final AccountUsageStatistic accountUsageStatistic) {
        return repository.save(accountUsageStatistic);
    }

    @Override
    public AccountUsageStatistic resetByAccountId(final Integer accountId) {
        Optional<AccountUsageStatistic> optional = repository.findByAccountId(accountId);
        if (optional.isPresent()) {
            AccountUsageStatistic target = optional.get();
            target.setTotalTransactionalEmailSent(0L);
            target.setTotalEmailCampaignSent(0L);
            target.setTotalWhatsappMessageSent(0L);
            target.setTotalSmsSent(0L);
            target.setSmsCreditUsed(0);

            // if on-prem, reset sms credit balance to Integer.MAX_VALUE
            if (deploymentProperties.isOnPremise()) {
                target.setSmsCredit(Integer.MAX_VALUE);
            }

            return repository.save(target);
        }
        return null;
    }

    @Override
    public AccountUsageStatistic createNew(final Account account) {
        AccountUsageStatistic newAccountUsageStatistic = new AccountUsageStatistic();
        newAccountUsageStatistic.setAccount(account);
        newAccountUsageStatistic.setTotalStorageUsed(0L);
        newAccountUsageStatistic.setTotalTransactionalEmailSent(0L);
        newAccountUsageStatistic.setTotalEmailCampaignSent(0L);

        return repository.save(newAccountUsageStatistic);
    }

    @Override
    public AccountUsageStatistic getOrCreateNew(
            final Account account,
            final AccountSubscription accountSubscription
    ) {
        AccountUsageStatistic target;

        Optional<AccountUsageStatistic> usageStatisticOptional = getByAccountId(account.getId());
        if (usageStatisticOptional.isPresent()) {
            target =  usageStatisticOptional.get();

        } else {
            AccountUsageStatistic newAccountUsageStatistic = new AccountUsageStatistic();
            newAccountUsageStatistic.setAccount(account);
            newAccountUsageStatistic.setTotalStorageUsed(0L);
            newAccountUsageStatistic.setTotalTransactionalEmailSent(0L);
            newAccountUsageStatistic.setTotalEmailCampaignSent(0L);
            newAccountUsageStatistic.setTotalWhatsappMessageSent(0L);
            newAccountUsageStatistic.setTotalSmsSent(0L);
            newAccountUsageStatistic.setMaxStorageSize(accountSubscription.getStorageSize());
            newAccountUsageStatistic.setMaxTransactionalEmailSent(accountSubscription.getTransactionalEmailSize());
            newAccountUsageStatistic.setMaxEmailCampaignSent(accountSubscription.getEmailCampaignSize());
            newAccountUsageStatistic.setSmsCredit(0);
            newAccountUsageStatistic.setSmsCreditUsed(0);

            target = repository.save(newAccountUsageStatistic);
        }

        return target;
    }

}
