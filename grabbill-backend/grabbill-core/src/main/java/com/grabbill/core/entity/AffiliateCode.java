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
@Table(name = "affiliate_code")
@EntityListeners(AuditingEntityListener.class)
public class AffiliateCode extends Auditable {

    @Id
    @SequenceGenerator(name = "AFFILIATE_CODE_SEQ", sequenceName = "affiliate_code_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "AFFILIATE_CODE_SEQ")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

}
