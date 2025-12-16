package com.grabbill.core.entity;

import com.grabbill.core.model.AdminDomainType;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "admin_audit_log")
@EntityListeners(AuditingEntityListener.class)
public class AdminAuditLog {

    @Id
    @SequenceGenerator(name = "ADMIN_AUDIT_LOG_SEQ", sequenceName = "admin_audit_log_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "ADMIN_AUDIT_LOG_SEQ")
    private Long id;

    @Enumerated(EnumType.STRING)
    private AdminDomainType adminDomainType;

    @Column(nullable = false)
    private Integer targetId;

    private String actionType;

    private String description;

    // TODO: add DomainType (in case more functions in future)

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

}
