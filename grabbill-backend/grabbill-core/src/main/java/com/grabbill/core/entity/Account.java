package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
public class Account extends Auditable {

    @Id
    @SequenceGenerator(name = "ACCOUNT_SEQ", sequenceName = "account_id_seq", allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = SEQUENCE, generator = "ACCOUNT_SEQ")
    private Integer id;

    private String stripeCustomerId;

    private String companyName;

    private String companyContactNo;

    private String addrLine1;

    private String addrLine2;

    private String city;

    private String state;

    private String postcode;

    private String country;

    private String countryIsoCode;

    private boolean paymentExempted = false;

    private String affiliateMasterCode;

    private String affiliateSubCode;

    private String wabaEmail;
    private String wabaPassword;
    private String wabaId;
    private String wabaGuid;
    private String wabaName;
    private String wabaPhone;
    private String wabaPhoneId;
    private String wabaWebhookId;
    private String wabaWebhookUrl;
    private String wabaAutoReplyMessage;

    private String sftpHost;
    private int sftpPort;
    private String sftpUsername;
    private String sftpPassword;

    @Lob
    private String logo;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountSubscription> subscriptions = new ArrayList<>();

}
