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
@Table(name = "txe_type")
@EntityListeners(AuditingEntityListener.class)
public class TransactionalEmailType extends PurgeableType {

    @Id
    @SequenceGenerator(name = "TXE_TYPE_SEQ", sequenceName = "txe_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_TYPE_SEQ")
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

    @OneToMany(mappedBy = "transactionalEmailType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailIndexField> transactionalEmailIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "transactionalEmailType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailWhatsappTemplateParam> transactionalEmailWhatsappTemplateParams = new ArrayList<>();

    @OneToMany(mappedBy = "transactionalEmailType")
    private List<TransactionalEmailFile> transactionalEmailFiles = new ArrayList<>();

}
