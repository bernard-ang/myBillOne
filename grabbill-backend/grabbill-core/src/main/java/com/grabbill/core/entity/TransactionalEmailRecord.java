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
@Table(name = "txe_record")
public class TransactionalEmailRecord extends BaseRecord {

    @Id
    @SequenceGenerator(name = "TXE_REC_SEQ", sequenceName = "txe_rec_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_REC_SEQ")
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

    @Column(name = "wa_message_id")
    private String whatsappMessageId;

    @Column(name = "wa_body_content", columnDefinition = "LONGTEXT")
    private String whatsappBodyContent;

    @Column(name = "wa_status_sent")
    private Boolean whatsappStatusSent;

    @Column(name = "wa_status_sent_timestamp")
    private OffsetDateTime whatsappStatusSentTimestamp;

    @Column(name = "wa_status_skip")
    private Boolean whatsappStatusSkip;

    @Column(name = "wa_status_skip_reason")
    private String whatsappStatusSkipReason;

    @Column(name = "wa_status_delivered")
    private Boolean whatsappStatusDelivered;

    @Column(name = "wa_status_delivered_timestamp")
    private OffsetDateTime whatsappStatusDeliveredTimestamp;

    @Column(name = "wa_status_read")
    private Boolean whatsappStatusRead;

    @Column(name = "wa_status_read_timestamp")
    private OffsetDateTime whatsappStatusReadTimestamp;

    @Column(name = "wa_status_ack")
    private Boolean whatsappStatusAcknowledge;

    @Column(name = "wa_status_ack_timestamp")
    private OffsetDateTime whatsappStatusAcknowledgeTimestamp;

    @Column(name = "wa_status_failed")
    private Boolean whatsAppStatusFailed;

    @Column(name = "wa_status_failed_timestamp")
    private OffsetDateTime whatsAppStatusFailedTimestamp;

    @Column(name = "wa_status_failed_message", columnDefinition = "LONGTEXT")
    private String whatsAppStatusFailedMessage;


    @OneToOne(mappedBy = "transactionalEmailRecord")
    private TransactionalEmailIndexRow transactionalEmailIndexRow;

    @ManyToOne
    @JoinColumn(name = "txe_activity_id", nullable = false)
    private TransactionalEmailActivity transactionalEmailActivity;

}
