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
@Table(name = "txe_file")
@EntityListeners(AuditingEntityListener.class)
public class TransactionalEmailFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "TXE_FILE_SEQ", sequenceName = "txe_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_FILE_SEQ")
    private Long id;

    @OneToMany(mappedBy = "transactionalEmailFile")
    private List<TransactionalEmailIndexRow> transactionalEmailIndexRows = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "txe_activity_id", nullable = false)
    private TransactionalEmailActivity transactionalEmailActivity;

    @ManyToOne
    @JoinColumn(name = "txe_type_id", nullable = false)
    private TransactionalEmailType transactionalEmailType;

}
