package com.grabbill.core.entity;

import com.grabbill.core.model.DomainType;
import lombok.Data;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;


/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "embedded_link")
public class EmbeddedLink {

    @Id
    @SequenceGenerator(name = "EMBEDDED_LINK_SEQ", sequenceName = "embedded_link_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EMBEDDED_LINK_SEQ")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type")
    private DomainType domainType;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "url")
    private String url;

    @OneToMany(mappedBy = "embeddedLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmbeddedLinkClick> embeddedLinkClicks = new ArrayList<>();

}
