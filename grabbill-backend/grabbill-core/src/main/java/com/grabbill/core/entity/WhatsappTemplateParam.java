package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_template_param")
public class WhatsappTemplateParam {

    @Id
    @SequenceGenerator(name = "WA_PARAM_SEQ", sequenceName = "wa_param_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_PARAM_SEQ")
    private Long id;

    @Column(name = "idx")
    private String index;

    @Column(name = "field")
    private String field;

    @ManyToOne
    @JoinColumn(name = "wa_type_id", nullable = false)
    private WhatsAppType whatsAppType;

}
