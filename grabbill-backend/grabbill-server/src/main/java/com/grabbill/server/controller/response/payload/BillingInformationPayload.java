package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import com.stripe.model.Address;
import com.stripe.model.Customer;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class BillingInformationPayload implements ApiPayload {

    private String id;
    private String name;
    private String contactNo;
    private String email;

    private String addrLine1;
    private String addrLine2;
    private String city;
    private String state;
    private String postcode;
    private String country;


    public static BillingInformationPayload from (final Customer customer) {
        BillingInformationPayload payload = new BillingInformationPayload();
        payload.setId(customer.getId());
        payload.setName(customer.getName());
        payload.setContactNo(customer.getPhone());
        payload.setEmail(customer.getEmail());

        Address address = customer.getAddress();
        if (address != null) {
            payload.setAddrLine1(address.getLine1());
            payload.setAddrLine2(address.getLine2());
            payload.setCity(address.getCity());
            payload.setState(address.getState());
            payload.setPostcode(address.getPostalCode());
            payload.setCountry(address.getCountry());
        }

        return payload;
    }

}
