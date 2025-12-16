package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_wa_activity_template")
@EntityListeners(AuditingEntityListener.class)
public class MTWhatsAppActivityTemplate {

    @Id
    @SequenceGenerator(name = "MT_WA_ACT_TPL_SEQ", sequenceName = "mt_wa_act_tpl_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_ACT_TPL_SEQ")
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "mt_wa_activity_id", nullable = false)
    private MTWhatsAppActivity mtWhatsAppActivity;

}
