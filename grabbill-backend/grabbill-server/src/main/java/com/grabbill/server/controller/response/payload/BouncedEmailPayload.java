package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BouncedEmail;
import com.grabbill.core.model.DomainType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class BouncedEmailPayload implements ApiPayload {

    private Long id;

    private DomainType domainType;

    private String email;

    private Long typeId;

    private String typeName;

    private Long activityId;

    private String activityName;

    private String dsnStatusCode;

    private String reason;

    private String createdBy;

    private OffsetDateTime createdDate;



    public static BouncedEmailPayload from (
            final BouncedEmail bouncedEmail
    ) {
        BouncedEmailPayload instance = new BouncedEmailPayload();
        instance.setId(bouncedEmail.getId());
        instance.setDomainType(bouncedEmail.getDomainType());
        instance.setTypeId(bouncedEmail.getTypeId());
        instance.setTypeName(bouncedEmail.getTypeName());
        instance.setEmail(bouncedEmail.getEmail());
        instance.setActivityId(bouncedEmail.getActivityId());
        instance.setActivityName(bouncedEmail.getActivityName());
        instance.setDsnStatusCode(bouncedEmail.getDsnStatusCode());
        instance.setReason(bouncedEmail.getReason());
        instance.setCreatedBy(bouncedEmail.getCreatedBy());
        instance.setCreatedDate(bouncedEmail.getCreatedDate());

        return instance;
    }

}
