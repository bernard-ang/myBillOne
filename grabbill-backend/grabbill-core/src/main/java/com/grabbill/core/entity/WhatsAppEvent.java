package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "whatsapp_event")
@EntityListeners(AuditingEntityListener.class)
public class WhatsAppEvent extends Auditable {

    @Id
    @SequenceGenerator(name = "WHATSAPP_EVENT_SUB_SEQ", sequenceName = "whatsapp_event_sub_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WHATSAPP_EVENT_SUB_SEQ")
    private Long id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "wa_message_id")
    private String whatsappMessageId;

    @Column(name = "type")
    private String type;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "status")
    private String status;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "processed")
    private boolean processed;

    @Column(name = "processed_count")
    private Integer processedCount = 0;

    @Column(name = "event_timestamp")
    private OffsetDateTime eventTimestamp;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "status_message", columnDefinition = "LONGTEXT")
    private String statusMessage;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
