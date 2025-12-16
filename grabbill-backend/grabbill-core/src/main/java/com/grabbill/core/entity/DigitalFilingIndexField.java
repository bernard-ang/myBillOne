package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "df_index_field")
public class DigitalFilingIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "DF_IDXF_SEQ", sequenceName = "df_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "df_type_id", nullable = false)
    private DigitalFilingType digitalFilingType;

}
