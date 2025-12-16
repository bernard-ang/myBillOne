package com.grabbill.core.entity;

import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.SubscriptionMode;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "account_subscription")
@EntityListeners(AuditingEntityListener.class)
public class AccountSubscription extends Auditable {

    @Id
    @SequenceGenerator(
            name = "ACCOUNT_SUB_SEQ",
            sequenceName = "account_sub_id_seq",
            allocationSize = 1,
            initialValue = 100
    )
    @GeneratedValue(strategy = SEQUENCE, generator = "ACCOUNT_SUB_SEQ")
    private Integer id;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "plan_description")
    private String planDescription;

    @Column(name = "storage_size")
    private Long storageSize;

    @Column(name = "storage_price")
    private Double storagePrice;

    @Column(name = "transactional_email_size")
    private Long transactionalEmailSize;

    @Column(name = "transactional_email_price")
    private Double transactionalEmailPrice;

    @Column(name = "email_campaign_size")
    private Long emailCampaignSize;

    @Column(name = "email_campaign_price")
    private Double emailCampaignPrice;

    @Column(name = "max_attachment_size")
    private Long maxAttachmentSize;

    @Column(name = "grabbill_logo")
    private Boolean grabbillLogo;

    @Column(name = "custom_smtp")
    private Boolean customSmtp;

    @Column(name = "max_user")
    private Integer maxUser;

    @Column(name = "support_days")
    private Integer supportDays;

    @Column(name = "reporting")
    private Boolean reporting;

    @Column(name = "schedule_email")
    private Boolean scheduleEmail;

    @Column(name = "export_file")
    private Boolean exportFile;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "cycle_start_date")
    private OffsetDateTime cycleStartDate;

    @Column(name = "cycle_end_date")
    private OffsetDateTime cycleEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private SubscriptionMode mode;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "disc_occurrence")
    @Enumerated(EnumType.STRING)
    private DiscountOccurrence discountOccurrence;

    @Column(name = "disc_occurrence_count")
    private Integer discountOccurrenceCount;

    @OneToMany(mappedBy = "accountSubscription")
    private List<Invoice> invoices = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
