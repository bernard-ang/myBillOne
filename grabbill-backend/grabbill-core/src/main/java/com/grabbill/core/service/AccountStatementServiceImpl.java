package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountStatement;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AccountUsageStatistic;
import com.grabbill.core.repository.AccountStatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author michaellow
 */
public class AccountStatementServiceImpl implements AccountStatementService {

    @Autowired
    private AccountStatementRepository repository;


    @Override
    public Page<AccountStatement> getAllByAccountId(
            final Integer accountId,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Pageable pageable
    ) {
        if (from != null && to != null) {
            return repository.findAllByAccountIdAndStartDateIsAfterAndEndDateIsBefore(accountId, from, to, pageable);
        }
        return repository.findAllByAccountId(accountId, pageable);
    }

    @Override
    public AccountStatement save(final AccountStatement accountStatement) {
        return repository.save(accountStatement);
    }

    @Override
    public AccountStatement newStatement(
            final Account account,
            final AccountSubscription accountSubscription,
            final AccountUsageStatistic usageStatistic
    ) {
        AccountStatement accountStatement = new AccountStatement();
        accountStatement.setStartDate(accountSubscription.getCycleStartDate());
        accountStatement.setEndDate(accountSubscription.getCycleEndDate());
        accountStatement.setMaxStorageSize(accountSubscription.getStorageSize());
        accountStatement.setMaxTransactionalEmailSent(accountSubscription.getTransactionalEmailSize());
        accountStatement.setMaxEmailCampaignSent(accountSubscription.getEmailCampaignSize());
        accountStatement.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        accountStatement.setStoragePrice(accountSubscription.getStoragePrice());
        accountStatement.setTransactionalEmailPrice(accountSubscription.getTransactionalEmailPrice());
        accountStatement.setEmailCampaignPrice(accountSubscription.getEmailCampaignPrice());
        accountStatement.setTotalPrice(
                accountSubscription.getStoragePrice() +
                accountSubscription.getTransactionalEmailPrice() +
                accountSubscription.getEmailCampaignPrice()
        );

        if (usageStatistic != null) {
            accountStatement.setTotalStorageUsed(usageStatistic.getTotalStorageUsed());
            accountStatement.setTotalTransactionalEmailSent(usageStatistic.getTotalTransactionalEmailSent());
            accountStatement.setTotalEmailCampaignSent(usageStatistic.getTotalEmailCampaignSent());
            accountStatement.setTotalWhatsappMessageSent(usageStatistic.getTotalWhatsappMessageSent());
            accountStatement.setTotalSmsSent(usageStatistic.getTotalSmsSent());
            accountStatement.setSmsCreditBalance(usageStatistic.getSmsCredit());
            accountStatement.setSmsCreditUsed(usageStatistic.getSmsCreditUsed());

        } else {
            accountStatement.setTotalWhatsappMessageSent(0L);
            accountStatement.setTotalSmsSent(0L);
            accountStatement.setSmsCreditBalance(0);
            accountStatement.setSmsCreditUsed(0);
        }

        accountStatement.setAccountSubscription(accountSubscription);
        accountStatement.setAccount(account);

        return repository.save(accountStatement);
    }
}
