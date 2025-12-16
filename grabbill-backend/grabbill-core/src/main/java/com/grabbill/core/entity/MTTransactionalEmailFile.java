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
@Table(name = "mt_txe_file")
@EntityListeners(AuditingEntityListener.class)
public class MTTransactionalEmailFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "MT_TXE_FILE_SEQ", sequenceName = "mt_txe_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_FILE_SEQ")
    private Long id;

    @OneToMany(mappedBy = "mtTransactionalEmailFile")
    private List<MTTransactionalEmailIndexRow> mtTransactionalEmailIndexRows = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "mt_txe_activity_id", nullable = false)
    private MTTransactionalEmailActivity mtTransactionalEmailActivity;

    @ManyToOne
    @JoinColumn(name = "mt_txe_type_id", nullable = false)
    private MTTransactionalEmailType mtTransactionalEmailType;

}
