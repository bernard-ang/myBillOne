package com.grabbill.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@EqualsAndHashCode(exclude = "contactGroups", callSuper = false)
@ToString(exclude = "contactGroups")
@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener.class)
public class Contact extends Auditable {

    @Id
    @SequenceGenerator(name = "CONTACT_SEQ", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "CONTACT_SEQ")
    private Integer id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "text1")
    private String text1;

    @Column(name = "number1")
    private Integer number1;

    @Column(name = "date1")
    private LocalDate date1;

    @Column(name = "text2")
    private String text2;

    @Column(name = "number2")
    private Integer number2;

    @Column(name = "date2")
    private LocalDate date2;

    @Column(name = "text3")
    private String text3;

    @Column(name = "number3")
    private Integer number3;

    @Column(name = "date3")
    private LocalDate date3;

    @Column(name = "text4")
    private String text4;

    @Column(name = "number4")
    private Integer number4;

    @Column(name = "date4")
    private LocalDate date4;

    @Column(name = "text5")
    private String text5;

    @Column(name = "number5")
    private Integer number5;

    @Column(name = "date5")
    private LocalDate date5;

    @Column(name = "text6")
    private String text6;

    @Column(name = "number6")
    private Integer number6;

    @Column(name = "date6")
    private LocalDate date6;

    @Column(name = "text7")
    private String text7;

    @Column(name = "number7")
    private Integer number7;

    @Column(name = "date7")
    private LocalDate date7;

    @Column(name = "text8")
    private String text8;

    @Column(name = "number8")
    private Integer number8;

    @Column(name = "date8")
    private LocalDate date8;

    @Column(name = "text9")
    private String text9;

    @Column(name = "number9")
    private Integer number9;

    @Column(name = "date9")
    private LocalDate date9;

    @Column(name = "text10")
    private String text10;

    @Column(name = "number10")
    private Integer number10;

    @Column(name = "date10")
    private LocalDate date10;

    @ManyToMany
    private Set<ContactGroup> contactGroups;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
