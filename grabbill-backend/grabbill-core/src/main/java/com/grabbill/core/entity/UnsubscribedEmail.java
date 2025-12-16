package com.grabbill.core.entity;

import com.grabbill.core.model.DomainType;
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
@Table(name = "unsubscribed_email")
@EntityListeners(AuditingEntityListener.class)
public class UnsubscribedEmail {

    @Id
    @SequenceGenerator(name = "UNSUBSCRIBED_EMAIL_SEQ", sequenceName = "unsubscribed_email_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "UNSUBSCRIBED_EMAIL_SEQ")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type")
    private DomainType domainType;

    @Column(name = "email")
    private String email;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "reason")
    private String reason;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
