package com.grabbill.core.entity;

import com.grabbill.core.model.ProcessStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseRecord {

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "message")
    private String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

}
