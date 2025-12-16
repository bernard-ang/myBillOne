package com.grabbill.core.model.plan;

import com.grabbill.core.entity.AccountUsageStatistic;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class WhatsappUsage {

    private long sent;

    public static WhatsappUsage from(final AccountUsageStatistic accountUsageStatistic) {
        WhatsappUsage whatsappUsage = new WhatsappUsage();
        whatsappUsage.setSent(accountUsageStatistic.getTotalWhatsappMessageSent());

        return whatsappUsage;
    }

}
