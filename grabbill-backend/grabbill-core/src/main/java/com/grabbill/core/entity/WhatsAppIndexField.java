package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_index_field")
public class WhatsAppIndexField extends BaseIndexField {

    @Id
    @SequenceGenerator(name = "WA_IDXF_SEQ", sequenceName = "wa_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_IDXF_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wa_type_id", nullable = false)
    private WhatsAppType whatsAppType;

}
