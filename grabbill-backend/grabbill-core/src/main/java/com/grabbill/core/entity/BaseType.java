package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseType extends Auditable {

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
