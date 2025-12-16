package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "txe_index_row")
public class TransactionalEmailIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "TXE_IDXR_SEQ", sequenceName = "txe_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_IDXR_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "txe_file_id")
    private TransactionalEmailFile transactionalEmailFile;

    @OneToOne
    @JoinColumn(name = "txe_record_id")
    private TransactionalEmailRecord transactionalEmailRecord;

    @ManyToOne
    @JoinColumn(name = "txe_activity_id", nullable = false)
    private TransactionalEmailActivity transactionalEmailActivity;

    @ManyToOne
    @JoinColumn(name = "txe_type_id", nullable = false)
    private TransactionalEmailType transactionalEmailType;

}
