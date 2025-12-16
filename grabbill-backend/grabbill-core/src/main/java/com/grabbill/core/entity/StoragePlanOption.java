package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "storage_plan_option")
public class StoragePlanOption extends BasePlanOption {
    @Id
    @SequenceGenerator(name = "STORAGE_PO_SEQ", sequenceName = "storage_po_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "STORAGE_PO_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
