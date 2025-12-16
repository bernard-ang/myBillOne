package com.grabbill.core.entity;

import com.grabbill.core.model.DataType;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "sms_act_index_field")
public class SmsActivityIndexField {

    @Id
    @SequenceGenerator(name = "SMS_ACT_IDXF_SEQ", sequenceName = "sms_act_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_ACT_IDXF_SEQ")
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "sms_activity_id", nullable = false)
    private SmsActivity smsActivity;

}
