package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "txe_index_field")
public class TransactionalEmailIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "TXE_IDXF_SEQ", sequenceName = "txe_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "txe_type_id", nullable = false)
    private TransactionalEmailType transactionalEmailType;

}
