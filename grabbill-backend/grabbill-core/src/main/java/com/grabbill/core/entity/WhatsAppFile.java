package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author seez
 */
@Data
@Entity
@Table(name = "wa_file")
@EntityListeners(AuditingEntityListener.class)
public class WhatsAppFile extends BaseFile {

    @Id
    @SequenceGenerator(name = "WA_FILE_SEQ", sequenceName = "wa_file_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "WA_FILE_SEQ")
    private Long id;

    @OneToMany(mappedBy = "whatsAppFile")
    private List<WhatsAppIndexRow> whatsAppIndexRows = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "wa_activity_id", nullable = false)
    private WhatsAppActivity whatsAppActivity;

    @ManyToOne
    @JoinColumn(name = "wa_type_id", nullable = false)
    private WhatsAppType whatsAppType;

}
