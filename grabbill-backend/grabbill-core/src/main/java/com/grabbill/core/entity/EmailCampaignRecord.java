package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_record")
public class EmailCampaignRecord extends BaseRecord {

    @Id
    @SequenceGenerator(name = "EC_REC_SEQ", sequenceName = "ec_rec_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_REC_SEQ")
    private Long id;

    @Column(name = "email_content", columnDefinition = "LONGTEXT")
    private String emailContent;

    @Column(name = "email_status_sent")
    private boolean emailStatusSent;

    @Column(name = "email_status_skip_unsubscribed")
    private boolean emailStatusSkipUnsubscribed;

    @Column(name = "email_status_skip_bounced")
    private boolean emailStatusSkipBounced;

    @Column(name = "processed_timestamp")
    private OffsetDateTime processedTimestamp;

    @Column(name = "email_status_soft_bounce")
    private boolean emailStatusSoftBounce;

    @Column(name = "email_status_hard_bounce")
    private boolean emailStatusHardBounce;

    @Column(name = "email_dsn_received_confirmation")
    private boolean emailDsnReceivedConfirmation;

    @Column(name = "email_dsn_message", columnDefinition = "LONGTEXT")
    private String emailDsnMessage;

    @Column(name = "dsn_processed_timestamp")
    private OffsetDateTime dsnProcessedTimestamp;

    @Column(name = "email_status_user_read_timestamp")
    private OffsetDateTime emailStatusUserReadTimestamp;

    @Column(name = "email_status_unsubscribed_timestamp")
    private OffsetDateTime emailStatusUnsubscribedTimestamp;

    @Column(name = "email_status_unsubscribed_reason")
    private String emailStatusUnsubscribedReason;

    @OneToOne(mappedBy = "emailCampaignRecord")
    private EmailCampaignIndexRow emailCampaignIndexRow;

    @ManyToOne
    @JoinColumn(name = "ec_activity_id", nullable = false)
    private EmailCampaignActivity emailCampaignActivity;

}
