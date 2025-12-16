package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "ec_plan_option")
public class EmailCampaignPlanOption extends BasePlanOption {
    @Id
    @SequenceGenerator(name = "EC_PO_SEQ", sequenceName = "ec_po_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_PO_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
