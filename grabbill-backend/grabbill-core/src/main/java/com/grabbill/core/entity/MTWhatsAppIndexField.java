package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mt_wa_index_field")
public class MTWhatsAppIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "MT_WA_IDXF_SEQ", sequenceName = "mt_wa_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mt_wa_type_id", nullable = false)
    private MTWhatsAppType mtWhatsAppType;

}
