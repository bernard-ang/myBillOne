package com.grabbill.core.model.plan;

import com.grabbill.core.entity.AccountUsageStatistic;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsUsage {

    private Long sent;
    private Integer smsCredit;
    private Integer smsCreditUsed;


    public static SmsUsage from(final AccountUsageStatistic accountUsageStatistic) {
        SmsUsage smsUsage = new SmsUsage();
        smsUsage.setSent(accountUsageStatistic.getTotalSmsSent());
        smsUsage.setSmsCredit(accountUsageStatistic.getSmsCredit());
        smsUsage.setSmsCreditUsed(accountUsageStatistic.getSmsCreditUsed());

        return smsUsage;
    }

}
