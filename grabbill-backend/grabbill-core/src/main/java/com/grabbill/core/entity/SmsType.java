package com.grabbill.core.entity;

import com.grabbill.core.model.SmsFieldType;
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
@Table(name = "sms_type")
@EntityListeners(AuditingEntityListener.class)
public class SmsType extends BaseType {

    @Id
    @SequenceGenerator(name = "SMS_TYPE_SEQ", sequenceName = "sms_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SMS_TYPE_SEQ")
    private Long id;

    @Column(name = "sms_from")
    private String smsFrom;

    @Column(name = "sms_content")
    private String smsContent;

    @Column(name = "sms_field_type")
    @Enumerated(EnumType.STRING)
    private SmsFieldType smsFieldType;

    @Column(name = "last_sent_by")
    private String lastSentBy;

    @Column(name = "last_sent_date")
    private OffsetDateTime lastSentDate;

    @OneToMany(mappedBy = "smsType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SmsIndexField> smsIndexFields = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "contact_group_id", referencedColumnName = "id")
    private ContactGroup contactGroup;

}
