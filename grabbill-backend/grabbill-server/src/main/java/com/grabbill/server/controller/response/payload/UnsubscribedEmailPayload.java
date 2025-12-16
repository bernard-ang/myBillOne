package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.UnsubscribedEmail;
import com.grabbill.core.model.DomainType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class UnsubscribedEmailPayload implements ApiPayload {

    private Long id;

    private DomainType domainType;

    private String email;

    private Long typeId;

    private String typeName;

    private Long activityId;

    private String activityName;

    private String reason;

    private String createdBy;

    private OffsetDateTime createdDate;



    public static UnsubscribedEmailPayload from (
            final UnsubscribedEmail unsubscribedEmail
    ) {
        UnsubscribedEmailPayload instance = new UnsubscribedEmailPayload();
        instance.setId(unsubscribedEmail.getId());
        instance.setDomainType(unsubscribedEmail.getDomainType());
        instance.setEmail(unsubscribedEmail.getEmail());
        instance.setTypeId(unsubscribedEmail.getTypeId());
        instance.setTypeName(unsubscribedEmail.getTypeName());
        instance.setActivityId(unsubscribedEmail.getActivityId());
        instance.setActivityName(unsubscribedEmail.getActivityName());
        instance.setReason(unsubscribedEmail.getReason());
        instance.setCreatedBy(unsubscribedEmail.getCreatedBy());
        instance.setCreatedDate(unsubscribedEmail.getCreatedDate());

        return instance;
    }

}
