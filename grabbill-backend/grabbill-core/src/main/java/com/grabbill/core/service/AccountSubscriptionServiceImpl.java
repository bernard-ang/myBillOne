package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.repository.AccountSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public class AccountSubscriptionServiceImpl implements AccountSubscriptionService {

    private static final double ANNUAL_DISCOUNT_RATE = 0.9;

    @Autowired
    private AccountSubscriptionRepository repository;

    @Override
    public Page<AccountSubscription> getSubscriptionsByAccount(Account account, Pageable pageable) {
        return repository.findByAccountId(account.getId(), pageable);
    }

    @Override
    public Optional<AccountSubscription> getSubscriptionByIdAndAccountId(Account account, Integer subscriptionId) {
        return repository.findByAccountIdAndId(account.getId(), subscriptionId);
    }

    @Override
    public Optional<AccountSubscription> getActiveSubscriptionByAccountId(
            final Integer accountId
    ) {
        return repository.findByAccountIdAndEndDateIsNull(accountId);
    }

    @Override
    public AccountSubscription getLatestSubscriptionByAccountId(final Integer accountId) {
        List<AccountSubscription> accountSubscriptions = repository.findByAccountIdOrderByCreatedDateAsc(accountId);
        return !accountSubscriptions.isEmpty() ? accountSubscriptions.get(accountSubscriptions.size() - 1) : null;
    }

    @Override
    public List<AccountSubscription> getAllActiveSubscriptions() {
        return repository.findAllByEndDateIsNull();
    }

    @Override
    public List<AccountSubscription> getAllActiveSubscriptionsByCycleEndDateBefore(
            final OffsetDateTime end
    ) {
        return repository.findAllByCycleEndDateIsLessThanEqualAndEndDateIsNull(end);
    }

    @Override
    public List<AccountSubscription> getAllActiveSubscriptionsByCycleEndDateBetween(
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        return repository.findAllByCycleEndDateIsGreaterThanEqualAndCycleEndDateIsLessThanEqualAndEndDateIsNull(
                start,
                end
        );
    }

    @Override
    public AccountSubscription save(final AccountSubscription accountSubscription) {
        return repository.save(accountSubscription);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public AccountSubscription createNewPersistent(
            final Account account,
            final SubscriptionMode subscriptionMode,
            final Plan plan,
            final BasePlanOption storageOption,
            final BasePlanOption transactionalEmailOption,
            final BasePlanOption emailCampaignOptions,
            final PromoCode promoCode
    ) {
        AccountSubscription accountSubscription = createInternal(
                account, subscriptionMode, plan, storageOption, transactionalEmailOption, emailCampaignOptions);

        if (promoCode != null) {
            accountSubscription.setPromoCode(promoCode.getCode());
            accountSubscription.setDiscountOccurrence(promoCode.getDiscountOccurrence());
            accountSubscription.setDiscountOccurrenceCount(promoCode.getDiscountOccurrenceCount());
        }

        return save(accountSubscription);
    }

    @Override
    public AccountSubscription createNewTransient(
            final Account account,
            final SubscriptionMode subscriptionMode,
            final Plan plan,
            final BasePlanOption storageOption,
            final BasePlanOption transactionalEmailOption,
            final BasePlanOption emailCampaignOptions
    ) {
        return createInternal(account, subscriptionMode, plan, storageOption, transactionalEmailOption, emailCampaignOptions);
    }

    private AccountSubscription createInternal(
            final Account account,
            final SubscriptionMode subscriptionMode,
            final Plan plan,
            final BasePlanOption storageOption,
            final BasePlanOption transactionalEmailOption,
            final BasePlanOption emailCampaignOptions
    ) {
        AccountSubscription accountSubscription = new AccountSubscription();
        accountSubscription.setMode(subscriptionMode);
        accountSubscription.setStartDate(OffsetDateTime.now());
        accountSubscription.setPlanName(plan.getName());
        accountSubscription.setPlanDescription(plan.getDescription());
        accountSubscription.setStorageSize(storageOption.getSize());
        accountSubscription.setStoragePrice(storageOption.getPrice());
        accountSubscription.setTransactionalEmailSize(transactionalEmailOption.getSize());
        accountSubscription.setTransactionalEmailPrice(transactionalEmailOption.getPrice());
        accountSubscription.setEmailCampaignSize(emailCampaignOptions.getSize());
        accountSubscription.setEmailCampaignPrice(emailCampaignOptions.getPrice());
        accountSubscription.setGrabbillLogo(plan.getGrabbillLogo());
        accountSubscription.setCustomSmtp(plan.getCustomSmtp());
        accountSubscription.setMaxUser(plan.getMaxUser());
        accountSubscription.setMaxAttachmentSize(plan.getMaxAttachmentSize());
        accountSubscription.setSupportDays(plan.getSupportDays());
        accountSubscription.setReporting(plan.getReporting());
        accountSubscription.setScheduleEmail(plan.getScheduleEmail());
        accountSubscription.setExportFile(plan.getExportFile());
        accountSubscription.setAccount(account);

        // NOTE: this is used to track monthly usage cycle (for plan usage statistic reset)!
        accountSubscription.setCycleStartDate(accountSubscription.getStartDate());
        accountSubscription.setCycleEndDate(accountSubscription.getStartDate().plusMonths(1));

        return accountSubscription;
    }

    @Override
    public AccountSubscription renewMonthlyUsageCycle(
            final AccountSubscription accountSubscription,
            final OffsetDateTime now
    ) {
        accountSubscription.setCycleStartDate(now);
        // NOTE: new cycle start date is now, and new cycle end date is 1 month from current cycle's end date
        //       reason being monthly cycle can be renewed before cycle end date reached due to
        //       (cron job which fetch account subscription "which expiring in 24 hours" to perform monthly cycle renew)
        accountSubscription.setCycleEndDate(accountSubscription.getCycleEndDate().plusMonths(1));

        return save(accountSubscription);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public AccountSubscription terminate(
            final AccountSubscription accountSubscription,
            final OffsetDateTime now
    ) {
        accountSubscription.setEndDate(now);
        return repository.saveAndFlush(accountSubscription);
    }

    @Override
    public boolean isMaturedForAnnualCharge(
            final AccountSubscription accountSubscription,
            final OffsetDateTime now
    ) {
        if (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode())) {
            OffsetDateTime then = now.plusDays(1);
            OffsetDateTime currrent = accountSubscription.getStartDate().plusYears(1);

            while (currrent.isBefore(then)) {
                if (ChronoUnit.DAYS.between(currrent, then) <= 7) {
                    return true;
                }
                currrent = currrent.plusYears(1);
            }
        }

        return false;
    }

    @Override
    public double getMonthlyRate(final AccountSubscription accountSubscription) {
        return accountSubscription.getStoragePrice()
                + accountSubscription.getEmailCampaignPrice()
                + accountSubscription.getTransactionalEmailPrice();
    }

    @Override
    public double getAnnuallyRate(final AccountSubscription accountSubscription) {
        double monthlyRate = getMonthlyRate(accountSubscription);
        double annualRate = monthlyRate * 12;
        return annualRate * ANNUAL_DISCOUNT_RATE;
    }

}
