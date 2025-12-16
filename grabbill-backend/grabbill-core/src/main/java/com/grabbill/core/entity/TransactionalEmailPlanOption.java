package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "txe_plan_option")
public class TransactionalEmailPlanOption extends BasePlanOption {
    @Id
    @SequenceGenerator(name = "TXE_PO_SEQ", sequenceName = "txe_po_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "TXE_PO_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
