package com.grabbill.server.controller.request;

import com.grabbill.core.entity.BillingInformation;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class BillingInformationRequest {

    private String name;
    private String contactNo;
    private String email;
    private String addrLine1;
    private String addrLine2;
    private String city;
    private String state;
    private String postcode;
    private String country;
    private String countryIsoCode;
    private boolean defaultBillInfo;

    public void to(final BillingInformation billingInformation) {
        billingInformation.setName(this.name);
        billingInformation.setContactNo(this.contactNo);
        billingInformation.setEmail(this.email);
        billingInformation.setAddrLine1(this.addrLine1);
        billingInformation.setAddrLine2(this.addrLine2);
        billingInformation.setCity(this.city);
        billingInformation.setState(this.state);
        billingInformation.setPostcode(this.postcode);
        billingInformation.setCountry(this.country);
        billingInformation.setCountryIsoCode(this.countryIsoCode);
        billingInformation.setDefaultBillingInfo(this.defaultBillInfo);
    }

}
