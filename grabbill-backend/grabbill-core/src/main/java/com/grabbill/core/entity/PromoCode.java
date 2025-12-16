package com.grabbill.core.entity;

import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.DiscountType;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "promo_code")
@EntityListeners(AuditingEntityListener.class)
public class PromoCode extends Auditable {

    @Id
    @SequenceGenerator(name = "PROMO_CODE_SEQ", sequenceName = "promo_code_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "PROMO_CODE_SEQ")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "start")
    private OffsetDateTime start;

    @Column(name = "end")
    private OffsetDateTime end;

    @Column(name = "disc", nullable = false)
    private Integer discount;

    @Column(name = "disc_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "disc_occurrence", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountOccurrence discountOccurrence;

    @Column(name = "disc_occurrence_count", nullable = false)
    private Integer discountOccurrenceCount;

    @Column(name = "active")
    private boolean active;

    @Column(name = "remarks")
    private String remarks;

}
