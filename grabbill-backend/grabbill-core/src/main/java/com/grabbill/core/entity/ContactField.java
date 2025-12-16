package com.grabbill.core.entity;

import com.grabbill.core.model.DataType;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "contact_field")
@EntityListeners(AuditingEntityListener.class)
public class ContactField extends Auditable {

    @Id
    @SequenceGenerator(name = "CONTACT_FIELD_SEQ", sequenceName = "contact_field_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "CONTACT_FIELD_SEQ")
    private Integer id;

    @Column(name = "seq_order")
    private int seqOrder;

    @Column(name = "name")
    private String name;

    @Column(name = "label")
    private String label;

    @Column(name = "required")
    private boolean required;

    @Column(name = "date_type")
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(name = "referenced")
    private boolean referenced;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
