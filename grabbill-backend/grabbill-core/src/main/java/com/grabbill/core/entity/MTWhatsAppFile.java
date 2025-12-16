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
@Table(name = "mt_wa_file")
@EntityListeners(AuditingEntityListener.class)
public class MTWhatsAppFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "MT_WA_FILE_SEQ", sequenceName = "mt_wa_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_WA_FILE_SEQ")
    private Long id;

    @OneToMany(mappedBy = "mtWhatsAppFile")
    private List<MTWhatsAppIndexRow> mtWhatsAppIndexRows = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "mt_wa_activity_id", nullable = false)
    private MTWhatsAppActivity mtWhatsAppActivity;

    @ManyToOne
    @JoinColumn(name = "mt_wa_type_id", nullable = false)
    private MTWhatsAppType mtWhatsAppType;

}
