package com.grabbill.core.service;

import com.grabbill.core.entity.AccountSubscription;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface ProRateCalculator {

    double calculateOffset(AccountSubscription accountSubscription, OffsetDateTime targetEndDate);

    long calculateRemainingDays(AccountSubscription accountSubscription, OffsetDateTime targetEndDate);

}
