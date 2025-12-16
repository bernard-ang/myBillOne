package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class JobBasicPayload implements ApiPayload {

    private Long id;
    private DomainType domainType;
    private JobEventType eventType;
    private JobExecutionMode executionMode;
    private Integer accountId;
    private String accountName;
    private Long typeId;
    private String typeName;
    private Long activityId;
    private String activityName;
    private JobStatus status;
    private String reason;
    private OffsetDateTime createdTimestamp;
    private OffsetDateTime scheduledExecutionTimestamp;


    public static JobBasicPayload from(final Job job) {
        JobBasicPayload payload = new JobBasicPayload();
        payload.setId(job.getId());
        payload.setDomainType(job.getDomainType());
        payload.setEventType(job.getEventType());
        payload.setExecutionMode(job.getExecutionMode());
        payload.setAccountId(job.getAccount().getId());
        payload.setAccountName(job.getAccount().getCompanyName());
        payload.setTypeId(job.getTypeId());
        payload.setTypeName(job.getTypeName());
        payload.setActivityId(job.getActivityId());
        payload.setActivityName(job.getActivityName());
        payload.setStatus(job.getStatus());
        payload.setReason(job.getErrorMessage());
        payload.setCreatedTimestamp(job.getCreatedTimestamp());
        payload.setScheduledExecutionTimestamp(job.getScheduledExecutionTimestamp());

        return payload;
    }

}
