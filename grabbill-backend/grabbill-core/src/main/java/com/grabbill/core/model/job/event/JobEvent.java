package com.grabbill.core.model.job.event;

import com.grabbill.core.model.DomainType;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Default Grabbill Job event (Digital Filing / Transactional Email / Email Campaign).
 *
 * @author michaellow
 */
@Data
public class JobEvent implements Serializable {

    private Long jobId;

    private JobEventType type;

    private DomainType domainType;

    private Long activityId;

    private OffsetDateTime dateTime;


    @Override
    public String toString() {
        return "JobEvent{" +
                "jobId=" + jobId +
                ", type=" + type +
                ", domainType=" + domainType +
                ", activityId=" + activityId +
                ", dateTime=" + dateTime +
                '}';
    }

}
