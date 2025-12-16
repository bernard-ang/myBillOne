package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Account;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author michaellow
 */
@Data
public class UserAccountWithWabaDetailsPayload implements ApiPayload {

    private String companyName;

    private String companyContactNo;

    private String addrLine1;

    private String addrLine2;

    private String city;

    private String state;

    private String postcode;

    private String country;

    private String countryIsoCode;

    private String affiliateCode;

    private PlanPayload plan;

    private String wabaId;

    private String wabaGuid;

    private String wabaAutoReplyMessage;

    private String wabaEmail;
    private String wabaName;
    private String wabaPhone;
    private String wabaPhoneId;
    private String wabaWebhookId;
    private String wabaWebhookUrl;

    public static UserAccountWithWabaDetailsPayload from(final Account account) {
        UserAccountWithWabaDetailsPayload instance = new UserAccountWithWabaDetailsPayload();
        instance.companyName = account.getCompanyName();
        instance.companyContactNo = account.getCompanyContactNo();
        instance.addrLine1 = account.getAddrLine1();
        instance.addrLine2 = account.getAddrLine2();
        instance.city = account.getCity();
        instance.state = account.getState();
        instance.postcode = account.getPostcode();
        instance.country = account.getCountry();
        instance.countryIsoCode = account.getCountryIsoCode();
        instance.wabaId = account.getWabaId();
        instance.wabaGuid = account.getWabaGuid();
        instance.wabaAutoReplyMessage = account.getWabaAutoReplyMessage();
        instance.setWabaEmail(account.getWabaEmail());
        instance.setWabaName(account.getWabaName());
        instance.setWabaPhoneId(account.getWabaPhoneId());
        instance.setWabaPhone(account.getWabaPhone());
        instance.setWabaWebhookId(account.getWabaWebhookId());
        instance.setWabaWebhookUrl(account.getWabaWebhookUrl());

        if (StringUtils.hasLength(account.getAffiliateMasterCode())) {
            if (StringUtils.hasLength(account.getAffiliateSubCode())) {
                instance.affiliateCode = account.getAffiliateMasterCode() + "-" + account.getAffiliateSubCode();
            } else {
                instance.affiliateCode = account.getAffiliateMasterCode();
            }
        }

        return instance;
    }

}
