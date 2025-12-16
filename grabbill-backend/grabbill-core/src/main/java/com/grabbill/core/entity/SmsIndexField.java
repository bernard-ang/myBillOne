package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "sms_index_field")
public class SmsIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "SMS_IDXF_SEQ", sequenceName = "sms_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sms_type_id", nullable = false)
    private SmsType smsType;

}
