package com.grabbill.core.entity;

import com.grabbill.core.model.DataType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseIndexField {

    @Column(name = "seq_order")
    private int seqOrder;

    @Column(name = "label")
    private String label;

    @Column(name = "header")
    private String header;

    @Column(name = "required")
    private boolean required;

    @Column(name = "date_type")
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(name = "applicable", columnDefinition = "boolean default true")
    private boolean applicable;

    @Column(name = "hard_ref")
    private boolean hardRef;            // referenced by COMPLETED activity

    @Column(name = "soft_ref")
    private boolean softRef;            // reference by DRAFT activity

}
