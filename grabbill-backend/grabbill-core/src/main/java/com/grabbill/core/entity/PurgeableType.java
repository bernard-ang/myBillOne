package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class PurgeableType extends BaseType {

    @Column(name = "auto_purge")
    private boolean autoPurge;

    @Column(name = "auto_purge_by_days")
    private Integer autoPurgeByDays;

}
