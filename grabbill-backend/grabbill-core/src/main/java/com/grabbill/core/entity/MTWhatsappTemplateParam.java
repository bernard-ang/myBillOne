package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_wa_template_param")
public class MTWhatsappTemplateParam {

    @Id
    @SequenceGenerator(name = "MT_WA_PARAM_SEQ", sequenceName = "mt_wa_param_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_PARAM_SEQ")
    private Long id;

    @Column(name = "idx")
    private String index;

    @Column(name = "field")
    private String field;

    @ManyToOne
    @JoinColumn(name = "mt_wa_template_id", nullable = false)
    private MTWhatsAppTemplate mtWhatsAppTemplate;

}
