package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;

import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "account_statement")
public class AccountStatement {

    @Id
    @SequenceGenerator(name = "ACCOUNT_STATEMENT_SEQ", sequenceName = "account_statement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "ACCOUNT_STATEMENT_SEQ")
    private Integer id;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

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

    @Column(name = "sms_credit_bal")
    private Integer smsCreditBalance = 0;

    @Column(name = "sms_credit_used")
    private Integer smsCreditUsed = 0;

    @Column(name = "storage_price")
    private Double storagePrice;

    @Column(name = "txe_price")
    private Double transactionalEmailPrice;

    @Column(name = "ec_price")
    private Double emailCampaignPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    @CreatedDate
    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "account_subscription_id", nullable = false)
    private AccountSubscription accountSubscription;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
