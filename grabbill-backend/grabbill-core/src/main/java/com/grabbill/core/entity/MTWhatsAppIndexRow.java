package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_wa_index_row")
public class MTWhatsAppIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "MT_WA_IDXR_SEQ", sequenceName = "mt_wa_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_IDXR_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mt_wa_file_id")
    private MTWhatsAppFile mtWhatsAppFile;

    @OneToOne
    @JoinColumn(name = "mt_wa_record_id")
    private MTWhatsAppRecord mtWhatsAppRecord;

    @ManyToOne
    @JoinColumn(name = "mt_wa_activity_id", nullable = false)
    private MTWhatsAppActivity mtWhatsAppActivity;

    @ManyToOne
    @JoinColumn(name = "mt_wa_type_id", nullable = false)
    private MTWhatsAppType mtWhatsAppType;

}
