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
@Table(name = "txe_activity")
@EntityListeners(AuditingEntityListener.class)
public class TransactionalEmailActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "TXE_ACT_SEQ", sequenceName = "txe_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_ACT_SEQ")
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

    @Column(name = "sms_content")
    private String smsContent;

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

    @Column(name = "storage_size")
    private Long storageSize;

    @Column(name = "send_wa_message")
    private Boolean sendWhatsAppMessage;

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
    private Integer whatsAppStatusSent;

    @Column(name = "wa_status_skip")
    private Integer whatsAppStatusSkip;

    @Column(name = "wa_status_read")
    private Integer whatsAppStatusRead;

    @Column(name = "wa_status_received")
    private Integer whatsAppStatusDelivered;

    @Column(name = "wa_status_acknowledge")
    private Integer whatsAppStatusAcknowledge;

    @Column(name = "wa_status_failed")
    private Integer whatsAppStatusFailed;


    @OneToMany(mappedBy = "transactionalEmailActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailFile> transactionalEmailFiles = new ArrayList<>();

    @OneToMany(mappedBy = "transactionalEmailActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailIndexRow> transactionalEmailIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "transactionalEmailActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailRecord> transactionalEmailRecords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "txe_type_id", nullable = false)
    private TransactionalEmailType transactionalEmailType;

}
