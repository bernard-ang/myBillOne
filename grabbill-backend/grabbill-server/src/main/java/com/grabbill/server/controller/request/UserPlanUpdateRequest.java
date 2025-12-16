package com.grabbill.server.controller.request;

import com.grabbill.core.model.SubscriptionMode;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


/**
 * @author michaellow
 */
@Data
public class UserPlanUpdateRequest {

    @NotEmpty
    private String planId;

    @NotNull
    private Long storageSize;

    @NotNull
    private Long transactionalEmailSize;

    @NotNull
    private Long emailCampaignSize;

    @NotNull
    private SubscriptionMode subscriptionMode;

    private String promoCode;

}
