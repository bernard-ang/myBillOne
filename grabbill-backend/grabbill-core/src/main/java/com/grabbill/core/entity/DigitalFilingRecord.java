package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "df_record")
public class DigitalFilingRecord extends BaseRecord {

    @Id
    @SequenceGenerator(name = "DF_REC_SEQ", sequenceName = "df_rec_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_REC_SEQ")
    private Long id;

    @OneToOne(mappedBy = "digitalFilingRecord")
    private DigitalFilingIndexRow digitalFilingIndexRow;

    @ManyToOne
    @JoinColumn(name = "df_activity_id", nullable = false)
    private DigitalFilingActivity digitalFilingActivity;

}
