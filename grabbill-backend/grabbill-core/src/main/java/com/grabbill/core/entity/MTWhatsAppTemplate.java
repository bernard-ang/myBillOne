package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_wa_template")
@EntityListeners(AuditingEntityListener.class)
public class MTWhatsAppTemplate {

    @Id
    @SequenceGenerator(name = "MT_WA_TPL_SEQ", sequenceName = "mt_wa_tpl_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_TPL_SEQ")
    private Long id;

    @Column(name = "wa_template_name")
    private String whatsAppTemplateName;

    @OneToMany(mappedBy = "mtWhatsAppTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MTWhatsappTemplateParam> mtWhatsappTemplateParams = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "mt_wa_type_id", nullable = false)
    private MTWhatsAppType mtWhatsAppType;

}
