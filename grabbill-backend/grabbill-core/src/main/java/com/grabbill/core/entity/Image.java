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
@Table(name = "image")
@EntityListeners(AuditingEntityListener.class)
public class Image extends Auditable {

    @Id
    @SequenceGenerator(name = "IMAGE_SEQ", sequenceName = "image_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "IMAGE_SEQ")
    private Long id;

    @Column(name = "link_id", unique = true, nullable = false)
    private String linkId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "fileSize", nullable = false)
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "image_folder_id")
    private ImageFolder imageFolder;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
