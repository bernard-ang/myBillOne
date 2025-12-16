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
public class StripeEventPayload implements ApiPayload {

    private Long id;
    private String eventId;
    private String eventType;
    private StripeEventType type;
    private String refId;
    private String jsonObject;

    private Integer accountId;
    private String accountName;

    private String createdBy;
    private OffsetDateTime createdDate;


    public static StripeEventPayload from(final StripeEvent stripeEvent) {
        StripeEventPayload payload = new StripeEventPayload();

        payload.setId(stripeEvent.getId());
        payload.setEventId(stripeEvent.getEventId());
        payload.setEventType(stripeEvent.getEventType());
        payload.setType(stripeEvent.getType());
        payload.setRefId(stripeEvent.getRefId());
        payload.setJsonObject(stripeEvent.getJsonObject());

        Account account = stripeEvent.getAccount();
        if (account != null) {
            payload.setAccountId(account.getId());
            payload.setAccountName(account.getCompanyName());
        }

        payload.setCreatedBy(stripeEvent.getCreatedBy());
        payload.setCreatedDate(stripeEvent.getCreatedDate());

        return payload;
    }

}
