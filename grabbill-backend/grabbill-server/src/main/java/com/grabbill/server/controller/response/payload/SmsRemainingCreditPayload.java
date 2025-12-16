package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AccountUsageStatistic;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsRemainingCreditPayload implements ApiPayload {

    private Integer smsCredit;


    public static SmsRemainingCreditPayload from (final AccountUsageStatistic accountUsageStatistic) {
        SmsRemainingCreditPayload instance = new SmsRemainingCreditPayload();
        instance.setSmsCredit(accountUsageStatistic.getSmsCredit());
        return instance;
    }

}
