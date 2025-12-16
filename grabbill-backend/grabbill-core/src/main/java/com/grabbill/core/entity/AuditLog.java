package com.grabbill.core.entity;

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
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @SequenceGenerator(name = "AUDIT_LOG_SEQ", sequenceName = "audit_log_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "AUDIT_LOG_SEQ")
    private Long id;

    @Column(nullable = false)
    private Integer accountId;

    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = true)
    private Long parentTypeid;

    private String actionType;

    private String description;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

}
