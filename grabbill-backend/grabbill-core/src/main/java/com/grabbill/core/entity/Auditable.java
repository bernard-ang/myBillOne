package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class Auditable {

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by", nullable = false)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private OffsetDateTime lastModifiedDate;

}
