package com.grabbill.core.service;

import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.model.SubscriptionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author michaellow
 */
class ProRateCalculatorImplTest {

    ProRateCalculatorImpl instance;

    @BeforeEach
    void setUp() {
        instance = new ProRateCalculatorImpl();
        instance.setAccountSubscriptionService(new AccountSubscriptionServiceImpl());
    }

    // cycle starts - 01-10-2022 00:00:00 AM
    // cycle ends   - 01-11-2022 00:00:00 AM
    // switch plan  - 15-10-2022 08:00:00 AM
    @Test
    void scenario1() {
        // given
        AccountSubscription accountSubscription = new AccountSubscription();
        accountSubscription.setMode(SubscriptionMode.MONTHLY);
        accountSubscription.setStoragePrice(100d);
        accountSubscription.setEmailCampaignPrice(100d);
        accountSubscription.setTransactionalEmailPrice(100d);
        accountSubscription.setCycleStartDate(OffsetDateTime.of(
                2022, 10, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));
        accountSubscription.setCycleEndDate(OffsetDateTime.of(
                2022, 11, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));

        // when
        accountSubscription.setEndDate(OffsetDateTime.of(
                2022, 10, 15, 8, 0, 0, 0,
                ZoneOffset.ofHours(8)));
        double result = instance.calculateOffset(accountSubscription, accountSubscription.getEndDate());

        // then
        assertEquals(154.84, result);
    }

    // cycle starts - 01-10-2022 00:00:00 AM
    // cycle ends   - 01-11-2022 00:00:00 AM
    // switch plan  - 30-10-2022 23:59:59 AM
    @Test
    void scenario2() {
        // given
        AccountSubscription accountSubscription = new AccountSubscription();
        accountSubscription.setMode(SubscriptionMode.MONTHLY);
        accountSubscription.setStoragePrice(100d);
        accountSubscription.setEmailCampaignPrice(100d);
        accountSubscription.setTransactionalEmailPrice(100d);
        accountSubscription.setCycleStartDate(OffsetDateTime.of(
                2022, 10, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));
        accountSubscription.setCycleEndDate(OffsetDateTime.of(
                2022, 11, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));

        // when
        accountSubscription.setEndDate(OffsetDateTime.of(
                2022, 10, 30, 23, 59, 59, 0,
                ZoneOffset.ofHours(8)));
        double result = instance.calculateOffset(accountSubscription, accountSubscription.getEndDate());

        // then
        assertEquals(9.68, result);
    }

    // cycle starts - 01-10-2022 00:00:00 AM
    // cycle ends   - 01-11-2022 00:00:00 AM
    // switch plan  - 31-10-2022 23:00:00 AM
    @Test
    void scenario3() {
        // given
        AccountSubscription accountSubscription = new AccountSubscription();
        accountSubscription.setMode(SubscriptionMode.MONTHLY);
        accountSubscription.setStoragePrice(100d);
        accountSubscription.setEmailCampaignPrice(100d);
        accountSubscription.setTransactionalEmailPrice(100d);
        accountSubscription.setCycleStartDate(OffsetDateTime.of(
                2022, 10, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));
        accountSubscription.setCycleEndDate(OffsetDateTime.of(
                2022, 11, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(8)));

        // when
        accountSubscription.setEndDate(OffsetDateTime.of(
                2022, 10, 31, 23, 0, 0, 0,
                ZoneOffset.ofHours(8)));
        double result = instance.calculateOffset(accountSubscription, accountSubscription.getEndDate());

        // then
        assertEquals(0.0, result);
    }
}