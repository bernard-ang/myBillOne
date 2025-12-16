package com.grabbill.core.entity;

import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.InvoiceType;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "invoice")
@EntityListeners(AuditingEntityListener.class)
public class Invoice extends Auditable {

    @Id
    @SequenceGenerator(name = "INVOICE_SEQ", sequenceName = "invoice_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "INVOICE_SEQ")
    private Integer id;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "stripe_invoice_id")
    private String stripeInvoiceId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_description")
    private String planDescription;

    @Column(name = "storage_size", nullable = false)
    private Long storageSize;

    @Column(name = "storage_price", nullable = false)
    private Double storagePrice;

    @Column(name = "transactional_email_size", nullable = false)
    private Long transactionalEmailSize;

    @Column(name = "transactional_email_price", nullable = false)
    private Double transactionalEmailPrice;

    @Column(name = "email_campaign_size", nullable = false)
    private Long emailCampaignSize;

    @Column(name = "email_campaign_price", nullable = false)
    private Double emailCampaignPrice;

    @Column(name = "sms_topup_size", nullable = false)
    private Long smsTopupSize;

    @Column(name = "sms_topup_price", nullable = false)
    private Double smsTopupPrice;

    @Column(name = "cycle_start_date", nullable = false)
    private OffsetDateTime cycleStartDate;

    @Column(name = "cycle_end_date", nullable = false)
    private OffsetDateTime cycleEndDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "overdue_amount", nullable = false)
    private Double overdueAmount;

    @Column(name = "total_tax_pct", nullable = false)
    private Double totalTaxPercentage;

    @Column(name = "total_tax_amount", nullable = false)
    private Double totalTaxAmount;

    @Column(name = "total_amount_wf_tax", nullable = false)
    private Double totalAmountWithTax;

    @Column(name = "offset_amount", nullable = false)
    private Double offsetAmount;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "bill_to_name")
    private String billToName;

    @Column(name = "bill_to_contact_no")
    private String billToContactNo;

    @Column(name = "bill_to_email")
    private String billToEmail;

    @Column(name = "bill_to_addr_line_1")
    private String billToAddrLine1;

    @Column(name = "bill_to_addr_line_2")
    private String billToAddrLine2;

    @Column(name = "bill_to_city")
    private String billToCity;

    @Column(name = "bill_to_state")
    private String billToState;

    @Column(name = "bill_to_postcode")
    private String billToPostcode;

    @Column(name = "bill_to_country")
    private String billToCountry;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private InvoiceType type;

    @ManyToOne
    @JoinColumn(name = "account_subscription_id", nullable = false)
    private AccountSubscription accountSubscription;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
