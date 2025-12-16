package com.grabbill.core.entity;

import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.DomainType;
import lombok.Data;

import javax.persistence.*;

import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "job")
public class Job {

    @Id
    @SequenceGenerator(name = "JOB_SEQ", sequenceName = "job_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "JOB_SEQ")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobExecutionMode executionMode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobEventType eventType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private Long typeId;

    @Column(nullable = false)
    private String typeName;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false)
    private String activityName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private String errorMessage;

    private String createdBy;

    private OffsetDateTime createdTimestamp;

    private OffsetDateTime scheduledExecutionTimestamp;

    private OffsetDateTime executionStartTimestamp;

    private OffsetDateTime executionEndTimestamp;

    private boolean retry;

    private OffsetDateTime retryTimestamp;

}
