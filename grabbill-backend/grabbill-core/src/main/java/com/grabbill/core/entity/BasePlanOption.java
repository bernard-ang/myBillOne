package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author seez
 */
@Data
@MappedSuperclass
public class BasePlanOption {

    private Long size;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
