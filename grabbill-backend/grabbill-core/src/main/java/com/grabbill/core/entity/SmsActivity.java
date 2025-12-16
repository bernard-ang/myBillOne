package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "sms_activity")
@EntityListeners(AuditingEntityListener.class)
public class SmsActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "SMS_ACT_SEQ", sequenceName = "sms_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_ACT_SEQ")
    private Long id;

    @Column(name = "sms_from")
    private String smsFrom;

    @Column(name = "sms_content")
    private String smsContent;

    @Column(name = "total_sms")
    private int totalSms;

    @Column(name = "sms_status_sent")
    private int smsStatusSent;

    @Column(name = "sms_status_error")
    private int smsStatusError;

    @Column(name = "sms_credit_used")
    private int smsCreditUsed;

    @Column(name = "scheduled_timestamp")
    private OffsetDateTime scheduledTimestamp;

    @ManyToOne
    @JoinColumn(name = "sms_type_id", nullable = false)
    private SmsType smsType;

    @OneToOne
    @JoinColumn(name = "contact_group_id", referencedColumnName = "id")
    private ContactGroup contactGroup;

    @OneToMany(mappedBy = "smsActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SmsActivityIndexField> smsActivityIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "smsActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SmsIndexRow> smsIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "smsActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SmsRecord> smsRecords = new ArrayList<>();

}
