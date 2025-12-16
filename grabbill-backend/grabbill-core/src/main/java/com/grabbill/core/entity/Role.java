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
@Table(name = "role")
public class Role {

    public static String OWNER_NAME = "OWNER";

    @Id
    @SequenceGenerator(name = "ROLE_SEQ", sequenceName = "role_id_seq", allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = SEQUENCE, generator = "ROLE_SEQ")
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "role")
    private Collection<User> users;

    @ManyToMany
    @JoinTable(
            name = "roles_privileges",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "privilege_id",
                    referencedColumnName = "id"
            )
    )
    private Collection<Privilege> privileges;

    public boolean isOwner() {
        return name.equals(OWNER_NAME);
    }

}
