package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_record")
public class WhatsAppRecord extends BaseRecord {

    @Id
    @SequenceGenerator(name = "WA_REC_SEQ", sequenceName = "wa_rec_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_REC_SEQ")
    private Long id;

    @Column(name = "processed_timestamp")
    private OffsetDateTime processedTimestamp;

    @Column(name = "wa_message_id")
    private String whatsAppMessageId;

    @Column(name = "wa_body_content", columnDefinition = "LONGTEXT")
    private String whatsAppBodyContent;

    @Column(name = "wa_status_sent")
    private Boolean whatsAppStatusSent;

    @Column(name = "wa_status_sent_timestamp")
    private OffsetDateTime whatsAppStatusSentTimestamp;

    @Column(name = "wa_status_skip")
    private Boolean whatsAppStatusSkip;

    @Column(name = "wa_status_skip_reason")
    private String whatsAppStatusSkipReason;

    @Column(name = "wa_status_delivered")
    private Boolean whatsAppStatusDelivered;

    @Column(name = "wa_status_delivered_timestamp")
    private OffsetDateTime whatsAppStatusDeliveredTimestamp;

    @Column(name = "wa_status_read")
    private Boolean whatsAppStatusRead;

    @Column(name = "wa_status_read_timestamp")
    private OffsetDateTime whatsAppStatusReadTimestamp;

    @Column(name = "wa_status_ack")
    private Boolean whatsAppStatusAcknowledge;

    @Column(name = "wa_status_ack_timestamp")
    private OffsetDateTime whatsAppStatusAcknowledgeTimestamp;

    @Column(name = "wa_status_failed")
    private Boolean whatsAppStatusFailed;

    @Column(name = "wa_status_failed_timestamp")
    private OffsetDateTime whatsAppStatusFailedTimestamp;

    @Column(name = "wa_status_failed_message", columnDefinition = "LONGTEXT")
    private String whatsAppStatusFailedMessage;


    @OneToOne(mappedBy = "whatsAppRecord")
    private WhatsAppIndexRow whatsAppIndexRow;

    @ManyToOne
    @JoinColumn(name = "wa_activity_id", nullable = false)
    private WhatsAppActivity whatsAppActivity;

}
