package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "df_file")
@EntityListeners(AuditingEntityListener.class)
public class DigitalFilingFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "DF_FILE_SEQ", sequenceName = "df_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_FILE_SEQ")
    private Long id;

    @OneToMany(mappedBy = "digitalFilingFile")
    private List<DigitalFilingIndexRow> digitalFilingIndexRows = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "df_activity_id", nullable = false)
    private DigitalFilingActivity digitalFilingActivity;

    @ManyToOne
    @JoinColumn(name = "df_type_id", nullable = false)
    private DigitalFilingType digitalFilingType;

}
