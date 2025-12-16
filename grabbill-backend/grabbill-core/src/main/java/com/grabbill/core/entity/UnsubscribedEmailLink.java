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
@Table(name = "unsubscribed_email_link")
public class UnsubscribedEmailLink {

    @Id
    @SequenceGenerator(name = "UNSUBSCRIBED_EMAIL_LINK_SEQ", sequenceName = "unsubscribed_email_link_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "UNSUBSCRIBED_EMAIL_LINK_SEQ")
    private Long id;

    @Column(name = "link_id", unique = true, nullable = false)
    private String linkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type")
    private DomainType domainType;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "email")
    private String email;

    @Column(name = "unsubscribed_date")
    private OffsetDateTime unsubscribedDate;

}
