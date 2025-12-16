package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.User;
import com.grabbill.core.utils.AffiliateCodeUtils;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author michaellow
 */
@Data
public class AccountBasicPayload implements ApiPayload {

    private Integer id;

    private String stripeCustomerId;

    private String companyName;

    private String companyContactNo;

    private String plan;

    private String email;

    private boolean active;

    private boolean paymentExempted;

    private String affiliateCode;

    private OffsetDateTime createdTime;

    private OffsetDateTime lastModifiedTime;



    public static AccountBasicPayload from(
            final Account account,
            final Optional<User> userOptional,
            final Optional<AccountSubscription> accountSubscriptionOptional
    ) {
        AccountBasicPayload payload = new AccountBasicPayload();

        payload.setId(account.getId());
        payload.setStripeCustomerId(account.getStripeCustomerId());
        payload.setCompanyName(account.getCompanyName());
        payload.setCompanyContactNo(account.getCompanyContactNo());
        payload.setCreatedTime(account.getCreatedDate());
        payload.setLastModifiedTime(account.getLastModifiedDate());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            payload.setEmail(user.getEmail());
            payload.setActive(user.isAccountActive());
        } else {
            payload.setActive(false);
        }
        payload.setPaymentExempted(account.isPaymentExempted());
        payload.setAffiliateCode(AffiliateCodeUtils.toAffiliateCode(account.getAffiliateMasterCode(), account.getAffiliateSubCode()));
        accountSubscriptionOptional.ifPresent(accountSubscription -> payload.setPlan(accountSubscription.getPlanName()));

        return payload;
    }

}
