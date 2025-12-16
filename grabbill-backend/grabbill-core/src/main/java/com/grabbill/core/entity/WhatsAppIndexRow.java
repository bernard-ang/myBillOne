package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_index_row")
public class WhatsAppIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "WA_IDXR_SEQ", sequenceName = "wa_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_IDXR_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wa_file_id")
    private WhatsAppFile whatsAppFile;

    @OneToOne
    @JoinColumn(name = "wa_record_id")
    private WhatsAppRecord whatsAppRecord;

    @ManyToOne
    @JoinColumn(name = "wa_activity_id", nullable = false)
    private WhatsAppActivity whatsAppActivity;

    @ManyToOne
    @JoinColumn(name = "wa_type_id", nullable = false)
    private WhatsAppType whatsAppType;

}
