package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_type")
@EntityListeners(AuditingEntityListener.class)
public class EmailCampaignType extends PurgeableType {

    @Id
    @SequenceGenerator(name = "EC_TYPE_SEQ", sequenceName = "ec_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_TYPE_SEQ")
    private Long id;

    @Column(name = "email_from")
    private String emailFrom;

    @Column(name = "email_from_name")
    private String emailFromName;

    @Column(name = "email_subject")
    private String emailSubject;

    @Column(name = "email_content", columnDefinition = "LONGTEXT")
    private String emailContent;

    @Column(name = "email_mjml_content", columnDefinition = "LONGTEXT")
    private String emailMjmlContent;

    @Column(name = "has_attachment")
    private boolean hasAttachment;

    @Column(name = "last_sent_by")
    private String lastSentBy;

    @Column(name = "last_sent_date")
    private OffsetDateTime lastSentDate;

    @OneToOne
    @JoinColumn(name = "contact_group_id", referencedColumnName = "id")
    private ContactGroup contactGroup;

}
