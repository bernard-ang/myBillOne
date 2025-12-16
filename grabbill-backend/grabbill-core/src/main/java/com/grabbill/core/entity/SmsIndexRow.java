package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "sms_index_row")
public class SmsIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "SMS_IDXR_SEQ", sequenceName = "sms_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_IDXR_SEQ")
    private Long id;

    @OneToOne
    @JoinColumn(name = "sms_record_id")
    private SmsRecord smsRecord;

    @ManyToOne
    @JoinColumn(name = "sms_activity_id", nullable = false)
    private SmsActivity smsActivity;

    @ManyToOne
    @JoinColumn(name = "sms_type_id", nullable = false)
    private SmsType smsType;

}
