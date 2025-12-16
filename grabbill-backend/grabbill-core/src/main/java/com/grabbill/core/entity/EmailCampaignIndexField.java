package com.grabbill.core.entity;

import com.grabbill.core.model.DataType;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_index_field")
public class EmailCampaignIndexField {

    @Id
    @SequenceGenerator(name = "EC_IDXF_SEQ", sequenceName = "ec_idxf_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_IDXF_SEQ")
    private Long id;

    @Column(name = "seq_order")
    private int seqOrder;

    @Column(name = "name")
    private String name;

    @Column(name = "label")
    private String label;

    @Column(name = "required")
    private boolean required;

    @Column(name = "date_type")
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @ManyToOne
    @JoinColumn(name = "ec_activity_id", nullable = false)
    private EmailCampaignActivity emailCampaignActivity;

}
