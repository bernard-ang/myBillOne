package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseFile extends Auditable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "fileSize", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false)
    private String fileType;

}
