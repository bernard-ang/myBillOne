package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BillingInformation;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class BillingInformationBasicPayload implements ApiPayload {

    private Integer id;
    private String name;
    private String contactNo;
    private String email;
    private boolean isDefault;

    private String createdBy;
    private OffsetDateTime createdDate;
    private String lastModifiedBy;
    private OffsetDateTime lastModifiedDate;


    public static BillingInformationBasicPayload from (final BillingInformation billingInformation) {
        BillingInformationBasicPayload payload = new BillingInformationBasicPayload();
        payload.setId(billingInformation.getId());
        payload.setName(billingInformation.getName());
        payload.setContactNo(billingInformation.getContactNo());
        payload.setEmail(billingInformation.getEmail());
        payload.setDefault(billingInformation.isDefaultBillingInfo());

        payload.setCreatedBy(billingInformation.getCreatedBy());
        payload.setCreatedDate(billingInformation.getCreatedDate());
        payload.setLastModifiedBy(billingInformation.getLastModifiedBy());
        payload.setLastModifiedDate(billingInformation.getLastModifiedDate());

        return payload;
    }
}
