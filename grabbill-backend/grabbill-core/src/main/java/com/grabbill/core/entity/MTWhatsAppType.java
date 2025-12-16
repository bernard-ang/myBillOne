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
@Table(name = "mt_wa_type")
@EntityListeners(AuditingEntityListener.class)
public class MTWhatsAppType extends BaseType {

    @Id
    @SequenceGenerator(name = "MT_WA_TYPE_SEQ", sequenceName = "mt_wa_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_TYPE_SEQ")
    private Long id;

    @Column(name = "password_protected")
    private boolean passwordProtected;

    @Column(name = "has_attachment")
    private boolean hasAttachment;

    @Column(name = "csv_separator")
    private String csvSeparator;

    @Column(name = "last_sent_by")
    private String lastSentBy;

    @Column(name = "last_sent_date")
    private OffsetDateTime lastSentDate;

    @OneToMany(mappedBy = "mtWhatsAppType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppTemplate> mtWhatsappTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "mtWhatsAppType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsAppIndexField> mtWhatsAppIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "mtWhatsAppType")
    private List<MTWhatsAppFile> mtWhatsAppFiles = new ArrayList<>();

}
