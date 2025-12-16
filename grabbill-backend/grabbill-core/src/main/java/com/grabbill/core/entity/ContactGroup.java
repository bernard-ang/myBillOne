package com.grabbill.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@EqualsAndHashCode(exclude = "contacts", callSuper = false)
@ToString(exclude = "contacts")
@Entity
@Table(name = "contact_group")
@EntityListeners(AuditingEntityListener.class)
public class ContactGroup extends Auditable {

    @Id
    @SequenceGenerator(name = "CONTACT_GROUP_SEQ", sequenceName = "contact_group_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "CONTACT_GROUP_SEQ")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "contactGroups")
    private Set<Contact> contacts;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
