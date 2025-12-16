package com.grabbill.core.entity;

import com.grabbill.core.model.StripeEventType;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "stripe_event")
@EntityListeners(AuditingEntityListener.class)
public class StripeEvent extends Auditable {

    @Id
    @SequenceGenerator(name = "STRIPE_EVENT_SUB_SEQ", sequenceName = "stripe_event_sub_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "STRIPE_EVENT_SUB_SEQ")
    private Long id;


    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private StripeEventType type;

    @Column(name = "ref_id")
    private String refId;

    @Column(name = "json_object", columnDefinition = "LONGTEXT")
    private String jsonObject;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
