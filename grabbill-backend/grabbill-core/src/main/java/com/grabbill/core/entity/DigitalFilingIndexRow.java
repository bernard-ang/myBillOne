package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "df_index_row")
public class DigitalFilingIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "DF_IDXR_SEQ", sequenceName = "df_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_IDXR_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "df_file_id")
    private DigitalFilingFile digitalFilingFile;

    @OneToOne
    @JoinColumn(name = "df_record_id")
    private DigitalFilingRecord digitalFilingRecord;

    @ManyToOne
    @JoinColumn(name = "df_activity_id", nullable = false)
    private DigitalFilingActivity digitalFilingActivity;

    @ManyToOne
    @JoinColumn(name = "df_type_id", nullable = false)
    private DigitalFilingType digitalFilingType;

}
