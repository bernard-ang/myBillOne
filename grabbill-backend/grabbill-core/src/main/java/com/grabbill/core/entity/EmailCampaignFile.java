package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "ec_file")
@EntityListeners(AuditingEntityListener.class)
public class EmailCampaignFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "EC_FILE_SEQ", sequenceName = "ec_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EC_FILE_SEQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ec_activity_id", nullable = false)
    private EmailCampaignActivity emailCampaignActivity;

}
