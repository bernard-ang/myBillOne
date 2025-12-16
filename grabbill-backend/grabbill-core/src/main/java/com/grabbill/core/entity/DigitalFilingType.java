package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "df_type")
@EntityListeners(AuditingEntityListener.class)
public class DigitalFilingType extends PurgeableType {

    @Id
    @SequenceGenerator(name = "DF_TYPE_SEQ", sequenceName = "df_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_TYPE_SEQ")
    private Long id;

    @Column(name = "csv_separator")
    private String csvSeparator;

    @Column(name = "last_upload_by")
    private String lastUploadBy;

    @Column(name = "last_upload_date")
    private OffsetDateTime lastUploadDate;

    @OneToMany(mappedBy = "digitalFilingType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DigitalFilingIndexField> digitalFilingIndexFields = new ArrayList<>();

    @OneToMany(mappedBy = "digitalFilingType")
    private List<DigitalFilingFile> digitalFilingFiles = new ArrayList<>();

}
