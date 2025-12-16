package com.grabbill.server.controller.response.payload;

import com.grabbill.core.model.plan.EmailCampaignUsage;
import com.grabbill.core.model.plan.TransactionalEmailUsage;
import com.grabbill.core.model.plan.StorageUsage;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class DashboardStatisticsPayload implements ApiPayload {

    private OffsetDateTime startDateTime;

    private OffsetDateTime endDateTime;

    private long storageUsed;

    private long storageLimit;

    private long transactionalEmailSent;

    private long transactionalEmailLimit;

    private long emailCampaignSent;

    private long emailCampaignLimit;

    private long smsRemainingCredits;

    private long smsTotalCreditsUsed;   // total sms credits used for this current billing cycle


    public static DashboardStatisticsPayload from(
            final OffsetDateTime startDateTime,
            final OffsetDateTime endDateTime,
            final StorageUsage storageUsage,
            final TransactionalEmailUsage transactionalEmailUsage,
            final EmailCampaignUsage emailCampaignUsage,
            final long smsRemainingCredits,
            final long smsTotalCreditsUsed
    ) {
        DashboardStatisticsPayload payload = new DashboardStatisticsPayload();
        payload.setStartDateTime(startDateTime);
        payload.setEndDateTime(endDateTime);
        payload.setStorageUsed(storageUsage.getUsed());
        payload.setStorageLimit(storageUsage.getLimit());
        payload.setTransactionalEmailSent(transactionalEmailUsage.getSent());
        payload.setTransactionalEmailLimit(transactionalEmailUsage.getLimit());
        payload.setEmailCampaignSent(emailCampaignUsage.getSent());
        payload.setEmailCampaignLimit(emailCampaignUsage.getLimit());
        payload.setSmsRemainingCredits(smsRemainingCredits);
        payload.setSmsTotalCreditsUsed(smsTotalCreditsUsed);

        return payload;
    }

}
