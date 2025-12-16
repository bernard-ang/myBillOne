package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * @author michaellow
 */
@Data
public class UserAccountUpdateRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String companyContactNo;

    private String addrLine1;

    private String addrLine2;

    private String city;

    private String state;

    private String postcode;

    @NotBlank
    private String country;

    @NotBlank
    private String countryIsoCode;

    private String affiliateCode;

    private String logo;

}
