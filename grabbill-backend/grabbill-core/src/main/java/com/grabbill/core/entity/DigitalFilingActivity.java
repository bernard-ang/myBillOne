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
@Table(name = "df_activity")
@EntityListeners(AuditingEntityListener.class)
public class DigitalFilingActivity extends BaseActivity {

    @Id
    @SequenceGenerator(name = "DF_ACT_SEQ", sequenceName = "df_act_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "DF_ACT_SEQ")
    private Long id;

    @Column(name = "storage_size")
    private Long storageSize;

    @Column(name = "sftp")
    private boolean sftp = false;

    @Column(name = "sftp_path")
    private String sftpPath;

    @OneToMany(mappedBy = "digitalFilingActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DigitalFilingFile> digitalFilingFiles = new ArrayList<>();

    @OneToMany(mappedBy = "digitalFilingActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DigitalFilingIndexRow> digitalFilingIndexRows = new ArrayList<>();

    @OneToMany(mappedBy = "digitalFilingActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DigitalFilingRecord> digitalFilingRecords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "df_type_id", nullable = false)
    private DigitalFilingType digitalFilingType;

}
