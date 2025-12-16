package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_txe_index_row")
public class MTTransactionalEmailIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "MT_TXE_IDXR_SEQ", sequenceName = "mt_txe_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_IDXR_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mt_txe_file_id")
    private MTTransactionalEmailFile mtTransactionalEmailFile;

    @OneToOne
    @JoinColumn(name = "mt_txe_record_id")
    private MTTransactionalEmailRecord mtTransactionalEmailRecord;

    @ManyToOne
    @JoinColumn(name = "mt_txe_activity_id", nullable = false)
    private MTTransactionalEmailActivity mtTransactionalEmailActivity;

    @ManyToOne
    @JoinColumn(name = "mt_txe_type_id", nullable = false)
    private MTTransactionalEmailType mtTransactionalEmailType;

}
