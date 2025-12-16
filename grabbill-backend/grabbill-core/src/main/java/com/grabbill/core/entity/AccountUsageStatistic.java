package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "account_usage_stats")
@EntityListeners(AuditingEntityListener.class)
public class AccountUsageStatistic extends Auditable {

    @Id
    @SequenceGenerator(
            name = "ACCOUNT_USAGE_STATS_SEQ",
            sequenceName = "account_usage_stats_id_seq",
            allocationSize = 1,
            initialValue = 100
    )
    @GeneratedValue(strategy = SEQUENCE, generator = "ACCOUNT_USAGE_STATS_SEQ")
    private Integer id;

    @Column(name = "total_storage_used")
    private Long totalStorageUsed;

    @Column(name = "max_storage_size")
    private Long maxStorageSize;

    @Column(name = "total_txe_sent")
    private Long totalTransactionalEmailSent;

    @Column(name = "max_txe_sent")
    private Long maxTransactionalEmailSent;

    @Column(name = "total_ec_sent")
    private Long totalEmailCampaignSent;

    @Column(name = "max_ec_sent")
    private Long maxEmailCampaignSent;

    @Column(name = "total_wa_sent")
    private Long totalWhatsappMessageSent;

    @Column(name = "total_sms_sent")
    private Long totalSmsSent;

    @Column(name = "sms_credit")
    private Integer smsCredit = 0;                  // sms credit balance

    @Column(name = "sms_credit_used")
    private Integer smsCreditUsed = 0;

    @ManyToOne
    @JoinColumn(
            name = "account_id",
            unique = true,
            nullable = false
    )
    private Account account;

}
