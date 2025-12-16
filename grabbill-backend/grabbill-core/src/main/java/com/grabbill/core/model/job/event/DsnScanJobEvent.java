package com.grabbill.core.model.job.event;

import com.grabbill.core.model.DomainType;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Delivery Status Notification email scan job event.
 *
 * @author michaellow
 */
@Data
public class DsnScanJobEvent implements Serializable {

    private DomainType domainType;

    private Integer accountId;

    private Long activityId;

    private OffsetDateTime expireAt;

    private OffsetDateTime lastExecutedAt;


    @Override
    public String toString() {
        return "DsnScanJobEvent{" +
                "domainType=" + domainType +
                ", accountId=" + accountId +
                ", activityId=" + activityId +
                ", expireAt=" + expireAt +
                ", lastExecutedAt=" + lastExecutedAt +
                '}';
    }

}
