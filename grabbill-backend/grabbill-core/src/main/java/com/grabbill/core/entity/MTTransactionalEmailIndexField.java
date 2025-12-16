package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_txe_index_field")
public class MTTransactionalEmailIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "MT_TXE_IDXF_SEQ", sequenceName = "mt_txe_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mt_txe_type_id", nullable = false)
    private MTTransactionalEmailType mtTransactionalEmailType;

}
