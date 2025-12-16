package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.plan.*;
import com.grabbill.core.utils.AffiliateCodeUtils;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AccountDetailsPayload implements ApiPayload {

    private String stripeCustomerId;

    private String companyName;

    private String companyContactNo;

    private String address;

    private String plan;

    private OffsetDateTime createdTime;

    private OffsetDateTime lastModifiedTime;

    private long storageUsed;

    private long storageLimit;

    private long transactionalEmailSent;

    private long transactionalEmailLimit;

    private long emailCampaignSent;

    private long emailCampaignLimit;

    private boolean active;

    private boolean paymentExempted;

    private String affiliateCode;

    private long maxUser;

    private String wabaEmail;
    private String wabaId;
    private String wabaGuid;
    private String wabaName;
    private String wabaPhone;
    private String wabaPhoneId;
    private String wabaWebhookId;
    private String wabaWebhookUrl;

    private Long totalWhatsappMessageSent;
    private Long totalSmsSent;
    private Integer smsCredit;
    private Integer smsCreditUsed;


    public static AccountDetailsPayload from(
            final Account account,
            final User user,
            final String planName,
            final StorageUsage storageUsage,
            final TransactionalEmailUsage transactionalEmailUsage,
            final EmailCampaignUsage emailCampaignUsage,
            final SmsUsage smsUsage,
            final WhatsappUsage whatsappUsage
    ) {
        AccountDetailsPayload payload = new AccountDetailsPayload();
        payload.setStripeCustomerId(account.getStripeCustomerId());
        payload.setCompanyName(account.getCompanyName());
        payload.setCompanyContactNo(account.getCompanyContactNo());
        payload.setActive(user.isAccountActive());
        payload.setPaymentExempted(account.isPaymentExempted());
        payload.setAffiliateCode(AffiliateCodeUtils.toAffiliateCode(account.getAffiliateMasterCode(), account.getAffiliateSubCode()));
        payload.setPlan(planName);
        if (!user.getAccount().getSubscriptions().isEmpty()) {
            AccountSubscription latestAccountSubscription = user.getAccount().getSubscriptions().get(user.getAccount().getSubscriptions().size() - 1);
            payload.maxUser = latestAccountSubscription.getMaxUser();
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.hasLength(account.getAddrLine1())) {
            stringBuilder.append(account.getAddrLine1()).append(", ");
        }
        if (StringUtils.hasLength(account.getAddrLine2())) {
            stringBuilder.append(account.getAddrLine2()).append(", ");
        }
        if (StringUtils.hasLength(account.getCity())) {
            stringBuilder.append(account.getCity()).append(", ");
        }
        if (StringUtils.hasLength(account.getState())) {
            stringBuilder.append(account.getState()).append(", ");
        }
        if (StringUtils.hasLength(account.getPostcode())) {
            stringBuilder.append(account.getPostcode()).append(", ");
        }
        if (StringUtils.hasLength(account.getCountry())) {
            stringBuilder.append(account.getCountry());
        }
        payload.setAddress(stringBuilder.toString());
        payload.setCreatedTime(account.getCreatedDate());
        payload.setLastModifiedTime(account.getLastModifiedDate());

        if (storageUsage != null) {
            payload.setStorageUsed(storageUsage.getUsed());
            payload.setStorageLimit(storageUsage.getLimit());
        }

        if (transactionalEmailUsage != null) {
            payload.setTransactionalEmailSent(transactionalEmailUsage.getSent());
            payload.setTransactionalEmailLimit(transactionalEmailUsage.getLimit());
        }

        if (emailCampaignUsage != null) {
            payload.setEmailCampaignSent(emailCampaignUsage.getSent());
            payload.setEmailCampaignLimit(emailCampaignUsage.getLimit());
        }

        if (smsUsage != null) {
            payload.setTotalSmsSent(smsUsage.getSent());
            payload.setSmsCredit(smsUsage.getSmsCredit());
            payload.setSmsCreditUsed(smsUsage.getSmsCreditUsed());
        }

        if (whatsappUsage != null) {
            payload.setTotalWhatsappMessageSent(whatsappUsage.getSent());
        }

        payload.setWabaEmail(account.getWabaEmail());
        payload.setWabaId(account.getWabaId());
        payload.setWabaGuid(account.getWabaGuid());
        payload.setWabaName(account.getWabaName());
        payload.setWabaPhoneId(account.getWabaPhoneId());
        payload.setWabaPhone(account.getWabaPhone());
        payload.setWabaWebhookId(account.getWabaWebhookId());
        payload.setWabaWebhookUrl(account.getWabaWebhookUrl());

        return payload;
    }

}
