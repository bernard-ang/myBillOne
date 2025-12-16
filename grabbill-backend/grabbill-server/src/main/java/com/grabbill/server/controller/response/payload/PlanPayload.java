package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmailCampaignPlanOption;
import com.grabbill.core.entity.Plan;
import com.grabbill.core.entity.StoragePlanOption;
import com.grabbill.core.entity.TransactionalEmailPlanOption;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class PlanPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String description;

    List<PlanOptionPayload> storageOptions;

    List<PlanOptionPayload> transactionalEmailOptions;

    List<PlanOptionPayload> emailCampaignOptions;

    private Long maxAttachmentSize;

    private Boolean grabbillLogo;

    private Boolean customSmtp;

    private Integer maxUser;

    private Integer supportDays;

    private Boolean reporting;

    private Boolean scheduleEmail;

    private Boolean exportFile;

    public static PlanPayload from(final Plan plan) {
        PlanPayload instance = new PlanPayload();
        instance.id = plan.getId();
        instance.name = plan.getName();
        instance.description = plan.getDescription();
        instance.maxAttachmentSize = plan.getMaxAttachmentSize();
        instance.grabbillLogo = plan.getGrabbillLogo();
        instance.customSmtp = plan.getCustomSmtp();
        instance.maxUser = plan.getMaxUser();
        instance.supportDays = plan.getSupportDays();
        instance.reporting = plan.getReporting();
        instance.scheduleEmail = plan.getScheduleEmail();
        instance.exportFile = plan.getExportFile();

        List<PlanOptionPayload> storageOptions = new ArrayList<>();
        for (StoragePlanOption option : plan.getStorageOptions()) {
            PlanOptionPayload planOptionPayload = new PlanOptionPayload();
            planOptionPayload.copyFrom(option.getId(), option);
            storageOptions.add(planOptionPayload);
        }
        instance.setStorageOptions(storageOptions);

        List<PlanOptionPayload> transactionalEmailOptions = new ArrayList<>();
        for (TransactionalEmailPlanOption option : plan.getTransactionalEmailOptions()) {
            PlanOptionPayload planOptionPayload = new PlanOptionPayload();
            planOptionPayload.copyFrom(option.getId(), option);
            transactionalEmailOptions.add(planOptionPayload);
        }
        instance.setTransactionalEmailOptions(transactionalEmailOptions);

        List<PlanOptionPayload> emailCampaignOptions = new ArrayList<>();
        for (EmailCampaignPlanOption option : plan.getEmailCampaignOptions()) {
            PlanOptionPayload planOptionPayload = new PlanOptionPayload();
            planOptionPayload.copyFrom(option.getId(), option);
            emailCampaignOptions.add(planOptionPayload);
        }
        instance.setEmailCampaignOptions(emailCampaignOptions);

        return instance;
    }

}
