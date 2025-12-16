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
@Table(name = "mt_txe_type")
@EntityListeners(AuditingEntityListener.class)
public class MTTransactionalEmailType extends PurgeableType {

    @Id
    @SequenceGenerator(name = "MT_TXE_TYPE_SEQ", sequenceName = "mt_txe_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_TYPE_SEQ")
    private Long id;

    @Column(name = "email_from")
    private String emailFrom;

    @Column(name = "email_from_name")
    private String emailFromName;

    @Column(name = "email_attachment_file_name")
    private String emailAttachmentFileName;

    @Column(name = "password_protected")
    private boolean passwordProtected;

    @Column(name = "has_attachment")
    private boolean hasAttachment;

    @Column(name = "csv_separator")
    private String csvSeparator;

    @Column(name = "send_sms")
    private boolean sendSms;

    @Column(name = "sms_content")
    private String smsContent;

    @Column(name = "archive")
    private boolean archive;

    @Column(name = "wa_template_name")
    private String whatsAppTemplateName;

    @Column(name = "last_sent_by")
    private String lastSentBy;

    @Column(name = "last_sent_date")
    private OffsetDateTime lastSentDate;

    @OneToMany(mappedBy = "mtTransactionalEmailType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTTransactionalEmailTemplate> mtTransactionalEmailTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "mtTransactionalEmailType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTTransactionalEmailIndexField> mtTransactionalEmailIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "mtTransactionalEmailType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTTransactionalEmailWhatsappTemplateParam> mtTransactionalEmailWhatsappTemplateParams = new ArrayList<>();

    @OneToMany(mappedBy = "mtTransactionalEmailType")
    private List<MTTransactionalEmailFile> mtTransactionalEmailFiles = new ArrayList<>();

}
