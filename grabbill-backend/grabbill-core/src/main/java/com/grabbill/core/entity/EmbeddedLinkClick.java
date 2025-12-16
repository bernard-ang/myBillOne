package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;

import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;


/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "embedded_link_click")
public class EmbeddedLinkClick {

    @Id
    @SequenceGenerator(name = "EMBEDDED_LINK_CLICK_SEQ", sequenceName = "embedded_link_click_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EMBEDDED_LINK_CLICK_SEQ")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "clicked_date")
    private OffsetDateTime clickedDate;

    @ManyToOne
    @JoinColumn(name = "embedded_link_id", nullable = false)
    private EmbeddedLink embeddedLink;

}
