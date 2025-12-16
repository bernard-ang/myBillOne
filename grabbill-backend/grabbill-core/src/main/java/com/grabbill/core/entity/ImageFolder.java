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
@Table(name = "image_folder")
@EntityListeners(AuditingEntityListener.class)
public class ImageFolder extends Auditable {

    @Id
    @SequenceGenerator(name = "IMAGE_FOLDER_SEQ", sequenceName = "image_folder_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "IMAGE_FOLDER_SEQ")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "imageFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
