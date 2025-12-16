package com.grabbill.core.entity;

import com.grabbill.core.model.CreditType;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "credits_plan_option")
public class CreditsPlanOption {

    @Id
    @SequenceGenerator(name = "CREDITS_PO_SEQ", sequenceName = "credits_po_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "CREDITS_PO_SEQ")
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditType type;

    @Column(nullable = false)
    private String name;

    private String description;

    private Integer quantity;

    private Double price;

    @Column(nullable = false)
    private String stripeProductId;

}
