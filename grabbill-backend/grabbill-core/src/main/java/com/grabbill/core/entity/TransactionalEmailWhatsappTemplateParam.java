package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "txe_wa_template_param")
public class TransactionalEmailWhatsappTemplateParam {

    @Id
    @SequenceGenerator(name = "TXE_WTPARAM_SEQ", sequenceName = "txe_wtparam_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_WTPARAM_SEQ")
    private Long id;

    @Column(name = "idx")
    private String index;

    @Column(name = "field")
    private String field;

    @ManyToOne
    @JoinColumn(name = "txe_type_id", nullable = false)
    private TransactionalEmailType transactionalEmailType;

}
