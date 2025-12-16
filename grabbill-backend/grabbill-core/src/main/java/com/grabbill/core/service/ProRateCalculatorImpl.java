package com.grabbill.core.service;

import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.model.SubscriptionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author michaellow
 */
public class ProRateCalculatorImpl implements ProRateCalculator {

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;


    @Override
    public double calculateOffset(final AccountSubscription accountSubscription, final OffsetDateTime targetEndDate) {
        if (isFreePlan(accountSubscription)) {
            return 0;
        }

        long remainingDays = calculateRemainingDays(accountSubscription, targetEndDate);
        double offset = 0;
        if (remainingDays > 0) {

            if (SubscriptionMode.MONTHLY.equals(accountSubscription.getMode())) {
                long days = ChronoUnit.DAYS.between(accountSubscription.getCycleStartDate(), accountSubscription.getCycleEndDate());
                double monthlyRate = accountSubscriptionService.getMonthlyRate(accountSubscription);
                offset = (monthlyRate / days) * remainingDays;

            } else if (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode())) {
                OffsetDateTime currentAnnualCycleStartDate = findLastAnnualCycleStartDate(accountSubscription, targetEndDate);
                long days = ChronoUnit.DAYS.between(currentAnnualCycleStartDate, currentAnnualCycleStartDate.plusYears(1));
                double annualRate = accountSubscriptionService.getAnnuallyRate(accountSubscription);
                offset = (annualRate / days) * remainingDays;
            }

        }

        return round(offset);
    }

    @Override
    public long calculateRemainingDays(final AccountSubscription accountSubscription, final OffsetDateTime targetEndDate) {
        long remainingDays = 0;

        if (SubscriptionMode.MONTHLY.equals(accountSubscription.getMode())) {
            if (targetEndDate.isBefore(accountSubscription.getCycleEndDate())) {
                remainingDays = ChronoUnit.DAYS.between(targetEndDate, accountSubscription.getCycleEndDate());
            }

        } else if (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode())) {
            OffsetDateTime currentAnnualCycleEndDate = findLastAnnualCycleStartDate(accountSubscription, targetEndDate).plusYears(1);
            remainingDays = ChronoUnit.DAYS.between(targetEndDate, currentAnnualCycleEndDate);
        }

        return remainingDays;
    }

    private OffsetDateTime findLastAnnualCycleStartDate(
            final AccountSubscription accountSubscription,
            final OffsetDateTime targetEndDate
    ) {
        OffsetDateTime currentAnnualCycleStartDate = accountSubscription.getStartDate();
        while (currentAnnualCycleStartDate.plusYears(1).isBefore(targetEndDate)) {
            currentAnnualCycleStartDate = currentAnnualCycleStartDate.plusYears(1);
        }

        return currentAnnualCycleStartDate;
    }

    private boolean isFreePlan(final AccountSubscription accountSubscription) {
        if (accountSubscriptionService.getMonthlyRate(accountSubscription) == 0) {
            return true;
        }
        return false;
    }

    private double round(final double value) {
        BigDecimal target = BigDecimal.valueOf(value);
        target = target.setScale(2, RoundingMode.HALF_UP);
        return target.doubleValue();
    }

    protected void setAccountSubscriptionService(AccountSubscriptionService accountSubscriptionService) {
        this.accountSubscriptionService = accountSubscriptionService;
    }

}
