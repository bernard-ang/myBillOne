package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_index_row")
public class EmailCampaignIndexRow extends BaseIndexRow {

    @Id
    @SequenceGenerator(name = "EC_IDXR_SEQ", sequenceName = "ee_idxr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_IDXR_SEQ")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ec_record_id")
    private EmailCampaignRecord emailCampaignRecord;

    @ManyToOne
    @JoinColumn(name = "ec_activity_id", nullable = false)
    private EmailCampaignActivity emailCampaignActivity;

    @ManyToOne
    @JoinColumn(name = "ec_type_id", nullable = false)
    private EmailCampaignType emailCampaignType;

}
