package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "plan")
public class Plan {

    @Id
    @SequenceGenerator(name = "PLAN_SEQ", sequenceName = "plan_id_seq", allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = SEQUENCE, generator = "PLAN_SEQ")
    private Integer id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoragePlanOption> storageOptions;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionalEmailPlanOption> transactionalEmailOptions;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCampaignPlanOption> emailCampaignOptions;

    @Column(name = "max_attachment_size")
    private Long maxAttachmentSize;

    @Column(name = "grabbill_logo")
    private Boolean grabbillLogo;

    @Column(name = "custom_smtp")
    private Boolean customSmtp;

    @Column(name = "max_user")
    private Integer maxUser;

    @Column(name = "support_days")
    private Integer supportDays;

    private Boolean reporting;

    @Column(name = "schedule_email")
    private Boolean scheduleEmail;

    @Column(name = "export_file")
    private Boolean exportFile;
}
