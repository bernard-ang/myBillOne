package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_activity")
@EntityListeners(AuditingEntityListener.class)
public class WhatsAppActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "WA_ACT_SEQ", sequenceName = "wa_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_ACT_SEQ")
    private Long id;

    @Column(name = "scheduled_timestamp")
    private OffsetDateTime scheduledTimestamp;

    @Column(name = "storage_size")
    private Long storageSize;

    @Column(name = "wa_message_template_name")
    private String whatsAppTemplateName;

    @Column(name = "wa_message_body", columnDefinition = "LONGTEXT")
    private String whatsAppBodyContent;

    @Column(name = "wa_message_document")
    private Boolean whatsAppDocument;

    @Column(name = "wa_message_footer")
    private String whatsAppFooterContent;

    @Column(name = "wa_message_button")
    private String whatsAppButton;

    @Column(name = "wa_status_sent")
    private int whatsAppStatusSent;

    @Column(name = "wa_status_skip")
    private int whatsAppStatusSkip;

    @Column(name = "wa_status_read")
    private int whatsAppStatusRead;

    @Column(name = "wa_status_received")
    private int whatsAppStatusDelivered;

    @Column(name = "wa_status_acknowledge")
    private int whatsAppStatusAcknowledge;

    @Column(name = "wa_status_failed")
    private int whatsAppStatusFailed;


    @OneToMany(mappedBy = "whatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsAppFile> whatsAppFiles = new ArrayList<>();

    @OneToMany(mappedBy = "whatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsAppIndexRow> whatsAppIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "whatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsAppRecord> whatsAppRecords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "wa_type_id", nullable = false)
    private WhatsAppType whatsAppType;

}
