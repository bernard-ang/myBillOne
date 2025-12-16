package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_activity")
@EntityListeners(AuditingEntityListener.class)
public class EmailCampaignActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "EC_ACT_SEQ", sequenceName = "ec_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_ACT_SEQ")
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

    @Column(name = "email_attachment_file_name")
    private String emailAttachmentFileName;

    @Column(name = "has_attachment")
    private boolean hasAttachment;

    @Column(name = "email_status_sent")
    private int emailStatusSent;

    @Column(name = "email_status_unsubscribed")
    private int emailStatusUnsubscribedSkip;

    @Column(name = "email_status_bounced")
    private int emailStatusBounced;

    @Column(name = "email_status_bounced_skip")
    private Integer emailStatusBouncedSkip;

    @Column(name = "email_status_opened")
    private int emailStatusOpened;

    @Column(name = "scheduled_timestamp")
    private OffsetDateTime scheduledTimestamp;

    @OneToMany(mappedBy = "emailCampaignActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCampaignFile> emailCampaignFiles = new ArrayList<>();

    @OneToMany(mappedBy = "emailCampaignActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCampaignIndexField> emailCampaignIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "emailCampaignActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCampaignIndexRow> emailCampaignIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "emailCampaignActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCampaignRecord> emailCampaignRecords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ec_type_id", nullable = false)
    private EmailCampaignType emailCampaignType;

    @OneToOne
    @JoinColumn(name = "contact_group_id", referencedColumnName = "id")
    private ContactGroup contactGroup;

}
