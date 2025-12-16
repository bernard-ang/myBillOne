package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "privilege")
public class Privilege {

    @Id
    @SequenceGenerator(name = "PRIVILEGE_SEQ", sequenceName = "privilege_id_seq", allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = SEQUENCE, generator = "PRIVILEGE_SEQ")
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;

}
