package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
@Data
public class AccountSubscriptionPayload implements ApiPayload {

    private Integer id;

    private String planName;

    private String planDescription;

    private Long storageSize;

    private Double storagePrice;

    private Long transactionalEmailSize;

    private Double transactionalEmailPrice;

    private Long emailCampaignSize;

    private Double emailCampaignPrice;

    private Long maxAttachmentSize;

    private Boolean grabbillLogo;

    private Boolean customSmtp;

    private Integer maxUser;

    private Integer supportDays;

    private Boolean reporting;

    private Boolean scheduleEmail;

    private Boolean exportFile;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    private OffsetDateTime cycleStartDate;

    private OffsetDateTime cycleEndDate;

    private SubscriptionMode mode;

    public static AccountSubscriptionPayload from(final AccountSubscription subscription) {
        AccountSubscriptionPayload instance = null;
        if (subscription != null) {
            instance = new AccountSubscriptionPayload();
            instance.id = subscription.getId();
            instance.planName = subscription.getPlanName();
            instance.planDescription = subscription.getPlanDescription();
            instance.storageSize = subscription.getStorageSize();
            instance.storagePrice = subscription.getStoragePrice();
            instance.transactionalEmailSize = subscription.getTransactionalEmailSize();
            instance.transactionalEmailPrice = subscription.getTransactionalEmailPrice();
            instance.emailCampaignSize = subscription.getEmailCampaignSize();
            instance.emailCampaignPrice = subscription.getEmailCampaignPrice();
            instance.maxAttachmentSize = subscription.getMaxAttachmentSize();
            instance.grabbillLogo = subscription.getGrabbillLogo();
            instance.customSmtp = subscription.getCustomSmtp();
            instance.maxUser = subscription.getMaxUser();
            instance.supportDays = subscription.getSupportDays();
            instance.reporting = subscription.getReporting();
            instance.scheduleEmail = subscription.getScheduleEmail();
            instance.exportFile = subscription.getExportFile();
            instance.startDate = subscription.getStartDate();
            instance.endDate = subscription.getEndDate();
            instance.cycleStartDate = subscription.getCycleStartDate();
            instance.cycleEndDate = subscription.getCycleEndDate();
            instance.mode = subscription.getMode();
        }

        return instance;
    }

}
