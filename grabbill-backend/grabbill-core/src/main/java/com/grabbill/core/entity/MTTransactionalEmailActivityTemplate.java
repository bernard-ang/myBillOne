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
@Table(name = "mt_txe_activity_template")
@EntityListeners(AuditingEntityListener.class)
public class MTTransactionalEmailActivityTemplate {

    @Id
    @SequenceGenerator(name = "MT_TXE_ACT_TPL_SEQ", sequenceName = "mt_txe_act_tpl_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MT_TXE_ACT_TPL_SEQ")
    private Long id;

    @Column(name = "email_template_name")
    private String emailTemplateName;

    @Column(name = "email_subject")
    private String emailSubject;

    @Column(name = "email_content", columnDefinition = "LONGTEXT")
    private String emailContent;

    @Column(name = "email_mjml_content", columnDefinition = "LONGTEXT")
    private String emailMjmlContent;

    @ManyToOne
    @JoinColumn(name = "mt_txe_activity_id", nullable = false)
    private MTTransactionalEmailActivity mtTransactionalEmailActivity;

}
