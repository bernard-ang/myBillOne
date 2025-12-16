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
@Table(name = "mt_wa_activity")
@EntityListeners(AuditingEntityListener.class)
public class MTWhatsAppActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "MT_WA_ACT_SEQ", sequenceName = "mt_wa_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_ACT_SEQ")
    private Long id;

    @Column(name = "scheduled_timestamp")
    private OffsetDateTime scheduledTimestamp;

    @Column(name = "storage_size")
    private Long storageSize;

    @Column(name = "sftp")
    private boolean sftp = false;

    @Column(name = "sftp_path")
    private String sftpPath;

    @OneToMany(mappedBy = "mtWhatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppActivityTemplate> mtWhatsAppActivityTemplates = new ArrayList<>();

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


    @OneToMany(mappedBy = "mtWhatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppFile> mtWhatsAppFiles = new ArrayList<>();

    @OneToMany(mappedBy = "mtWhatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppIndexRow> mtWhatsAppIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "mtWhatsAppActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppRecord> mtWhatsAppRecords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "mt_wa_type_id", nullable = false)
    private MTWhatsAppType mtWhatsAppType;
}
