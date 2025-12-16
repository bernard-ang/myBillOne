package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseActivity;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class BaseActivityBasicPayload implements ApiPayload {

    Long id;
    String name;
    ProcessStatus status;
    int noOfFiles;
    Integer priority;
    Integer retryCount;
    String message;
    OffsetDateTime draftTimestamp;
    OffsetDateTime submittedTimestamp;
    OffsetDateTime processingTimestamp;
    OffsetDateTime processedTimestamp;
    OffsetDateTime errorTimestamp;
    OffsetDateTime expectedPurgedTimestamp;
    OffsetDateTime purgedTimestamp;
    String purgedBy;
    String createdBy;
    OffsetDateTime createdDate;
    String lastModifiedBy;
    OffsetDateTime lastModifiedDate;


    protected void copyFrom(final BaseActivity activity) {
        this.setName(activity.getName());
        this.setStatus(activity.getStatus());
        this.setPriority(activity.getPriority());
        this.setRetryCount(activity.getRetryCount());
        this.setMessage(activity.getMessage());
        this.setDraftTimestamp(activity.getDraftTimestamp());
        this.setSubmittedTimestamp(activity.getSubmittedTimestamp());
        this.setProcessingTimestamp(activity.getProcessingTimestamp());
        this.setProcessedTimestamp(activity.getProcessedTimestamp());
        this.setErrorTimestamp(activity.getErrorTimestamp());
        this.setCreatedBy(activity.getCreatedBy());
        this.setCreatedDate(activity.getCreatedDate());
        this.setLastModifiedBy(activity.getLastModifiedBy());
        this.setLastModifiedDate(activity.getLastModifiedDate());
        this.setExpectedPurgedTimestamp(activity.getExpectedPurgedTimestamp());
        this.setPurgedTimestamp(activity.getPurgedTimestamp());
        this.setPurgedBy(activity.getPurgedBy());
    }

}
