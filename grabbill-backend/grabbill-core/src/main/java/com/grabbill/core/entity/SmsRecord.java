package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "sms_record")
public class SmsRecord extends BaseRecord {

    @Id
    @SequenceGenerator(name = "SMS_REC_SEQ", sequenceName = "sms_rec_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_REC_SEQ")
    private Long id;

    @Column(name = "sms_content")
    private String smsContent;

    @Column(name = "sms_status_sent")
    private boolean smsStatusSent;

    @Column(name = "credit_used")
    private int creditUsed;

    @Column(name = "sms_status_code")
    private int smsStatusCode;

    @Column(name = "sms_error_msg")
    private String smsErrorMessage;


    @Column(name = "processed_timestamp")
    private OffsetDateTime processedTimestamp;

    @OneToOne(mappedBy = "smsRecord")
    private SmsIndexRow smsIndexRow;

    @ManyToOne
    @JoinColumn(name = "sms_activity_id", nullable = false)
    private SmsActivity smsActivity;

}
