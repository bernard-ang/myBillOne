package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_txe_wa_template_param")
public class MTTransactionalEmailWhatsappTemplateParam {

    @Id
    @SequenceGenerator(name = "MT_TXE_WTPARAM_SEQ", sequenceName = "mt_txe_wtparam_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_WTPARAM_SEQ")
    private Long id;

    @Column(name = "idx")
    private String index;

    @Column(name = "field")
    private String field;

    @ManyToOne
    @JoinColumn(name = "mt_txe_type_id", nullable = false)
    private MTTransactionalEmailType mtTransactionalEmailType;

}
