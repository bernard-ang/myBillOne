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
@Table(name = "user_code")
@EntityListeners(AuditingEntityListener.class)
public class UserCode extends Auditable {

    @Id
    @SequenceGenerator(name = "USER_CODE_SEQ", sequenceName = "user_code_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "USER_CODE_SEQ")
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
