package com.grabbill.core.entity;

import com.grabbill.core.model.ProtocolEncryption;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "mail_svr")
@EntityListeners(AuditingEntityListener.class)
public class MailServer extends Auditable {

    @Id
    @SequenceGenerator(name = "MAIL_SVR_SEQ", sequenceName = "mail_svr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "MAIL_SVR_SEQ")
    private Integer id;

    @Column(name = "custom_server")
    private boolean customServer;

    private String smtpHost;

    private int smtpPort;

    @Enumerated(EnumType.STRING)
    private ProtocolEncryption smtpEncryption;

    private String smtpUsername;

    private String smtpPassword;

    private String smtpFrom;

    private String smtpFromName;

    private String imapHost;

    private int imapPort;

    @Enumerated(EnumType.STRING)
    private ProtocolEncryption imapEncryption;

    private String imapUsername;

    private String imapPassword;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
