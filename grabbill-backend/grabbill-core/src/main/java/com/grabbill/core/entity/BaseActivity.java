package com.grabbill.core.entity;

import com.grabbill.core.model.ProcessStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseActivity extends Auditable {

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Column(name = "priority")
    private Integer priority = 1;       // default to 1

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "message")
    private String message;

    @Column(name = "draft_timestamp")
    private OffsetDateTime draftTimestamp;

    @Column(name = "submitted_timestamp")
    private OffsetDateTime submittedTimestamp;

    @Column(name = "processing_timestamp")
    private OffsetDateTime processingTimestamp;

    @Column(name = "processed_timestamp")
    private OffsetDateTime processedTimestamp;

    @Column(name = "error_timestamp")
    private OffsetDateTime errorTimestamp;

    @Column(name = "expected_purged_timestamp")
    private OffsetDateTime expectedPurgedTimestamp;

    @Column(name = "purged_timestamp")
    private OffsetDateTime purgedTimestamp;

    @Column(name = "purged_by")
    private String purgedBy;

}
