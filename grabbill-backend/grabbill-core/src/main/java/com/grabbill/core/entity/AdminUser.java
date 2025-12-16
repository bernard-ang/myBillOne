package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "admin_user")
@EntityListeners(AuditingEntityListener.class)
public class AdminUser extends Auditable {

    @Id
    @SequenceGenerator(name = "ADMIN_USER_SEQ", sequenceName = "admin_user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "ADMIN_USER_SEQ")
    private Integer id;

    private String name;

    private String email;

    private String password;

    private String verificationCode;

    private boolean active;

    private OffsetDateTime lastLoggedIn;

    private boolean google2FAEnabled = false;

    private String google2FASecretKey;

    private int google2FAValidationCode;

    // default to true for admin user!
    private boolean email2FAEnabled = true;

    private int email2FAOtp;

    private OffsetDateTime email2FAOtpRequestedTime;

}
