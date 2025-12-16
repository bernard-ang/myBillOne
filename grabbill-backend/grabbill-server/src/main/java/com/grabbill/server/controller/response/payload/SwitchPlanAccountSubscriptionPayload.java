package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AccountSubscription;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SwitchPlanAccountSubscriptionPayload extends AccountSubscriptionPayload {

    private boolean paymentMethodAvailable;


    public static SwitchPlanAccountSubscriptionPayload from(
            final AccountSubscription subscription,
            final boolean paymentMethodAvailable
    ) {
        SwitchPlanAccountSubscriptionPayload instance = new SwitchPlanAccountSubscriptionPayload();
        instance.setId(subscription.getId());
        instance.setPlanName(subscription.getPlanName());
        instance.setPlanDescription(subscription.getPlanDescription());
        instance.setStorageSize(subscription.getStorageSize());
        instance.setStoragePrice(subscription.getStoragePrice());
        instance.setTransactionalEmailSize(subscription.getTransactionalEmailSize());
        instance.setTransactionalEmailPrice(subscription.getTransactionalEmailPrice());
        instance.setEmailCampaignSize(subscription.getEmailCampaignSize());
        instance.setEmailCampaignPrice(subscription.getEmailCampaignPrice());
        instance.setMaxAttachmentSize(subscription.getMaxAttachmentSize());
        instance.setGrabbillLogo(subscription.getGrabbillLogo());
        instance.setCustomSmtp(subscription.getCustomSmtp());
        instance.setMaxUser(subscription.getMaxUser());
        instance.setSupportDays(subscription.getSupportDays());
        instance.setReporting(subscription.getReporting());
        instance.setScheduleEmail(subscription.getScheduleEmail());
        instance.setExportFile(subscription.getExportFile());
        instance.setStartDate(subscription.getStartDate());
        instance.setEndDate(subscription.getEndDate());
        instance.setCycleStartDate(subscription.getCycleStartDate());
        instance.setCycleEndDate(subscription.getCycleEndDate());
        instance.setMode(subscription.getMode());
        instance.setPaymentMethodAvailable(paymentMethodAvailable);

        return instance;
    }

}
