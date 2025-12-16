package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
// TODO: REMOVE THIS!
@Data
@Entity
@Table(name = "billing_info")
@EntityListeners(AuditingEntityListener.class)
public class BillingInformation extends Auditable {

    @Id
    @SequenceGenerator(name = "BILLING_INFO_SEQ", sequenceName = "billing_info_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "BILLING_INFO_SEQ")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_no", nullable = false)
    private String contactNo;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "addr_line_1", nullable = false)
    private String addrLine1;

    @Column(name = "addr_line_2")
    private String addrLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "postcode", nullable = false)
    private String postcode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "country_iso_code", nullable = false)
    private String countryIsoCode;

    @Column(name = "default_bill_info")
    private boolean defaultBillingInfo;


    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
