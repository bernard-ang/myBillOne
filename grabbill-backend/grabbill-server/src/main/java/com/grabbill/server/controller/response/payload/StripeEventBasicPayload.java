package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class StripeEventBasicPayload implements ApiPayload {

    private Long id;
    private String eventId;
    private String eventType;
    private StripeEventType type;
    private String refId;
    private Integer accountId;
    private String accountName;

    private OffsetDateTime createdDate;
    private String createdBy;


    public static StripeEventBasicPayload from(final StripeEvent stripeEvent) {
        StripeEventBasicPayload payload = new StripeEventBasicPayload();
        payload.setId(stripeEvent.getId());
        payload.setEventId(stripeEvent.getEventId());
        payload.setEventType(stripeEvent.getEventType());
        payload.setType(stripeEvent.getType());
        payload.setRefId(stripeEvent.getRefId());

        Account account = stripeEvent.getAccount();
        if (account != null) {
            payload.setAccountId(account.getId());
            payload.setAccountName(account.getCompanyName());
        }

        payload.setCreatedDate(stripeEvent.getCreatedDate());
        payload.setCreatedBy(stripeEvent.getCreatedBy());

        return payload;
    }

}
