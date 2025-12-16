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
@Table(name = "wa_type")
@EntityListeners(AuditingEntityListener.class)
public class WhatsAppType extends BaseType {

    @Id
    @SequenceGenerator(name = "WA_TYPE_SEQ", sequenceName = "wa_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_TYPE_SEQ")
    private Long id;

    @Column(name = "password_protected")
    private boolean passwordProtected;

    @Column(name = "has_attachment")
    private boolean hasAttachment;

    @Column(name = "csv_separator")
    private String csvSeparator;

    @Column(name = "wa_template_name")
    private String whatsAppTemplateName;

    @Column(name = "last_sent_by")
    private String lastSentBy;

    @Column(name = "last_sent_date")
    private OffsetDateTime lastSentDate;

    @OneToMany(mappedBy = "whatsAppType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsAppIndexField> whatsAppIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "whatsAppType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsappTemplateParam> whatsappTemplateParams = new ArrayList<>();

    @OneToMany(mappedBy = "whatsAppType")
    private List<WhatsAppFile> whatsAppFiles = new ArrayList<>();

}
