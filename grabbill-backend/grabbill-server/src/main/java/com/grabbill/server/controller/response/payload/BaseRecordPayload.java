package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseRecord;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class BaseRecordPayload implements ApiPayload {

    Long id;
    String name;
    Integer priority;
    Integer retryCount;
    String message;
    ProcessStatus status;
    BaseIndexRowPayload indexRow;


    public void copyFrom(final BaseRecord record) {
        this.setName(record.getName());
        this.setPriority(record.getPriority());
        this.setRetryCount(record.getRetryCount());
        this.setMessage(record.getMessage());
        this.setStatus(record.getStatus());
    }
}
