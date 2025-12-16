package com.grabbill.core.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * @author michaellow
 */
@Data
@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User extends Auditable {

    @Id
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "user_id_seq", allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = SEQUENCE, generator = "USER_SEQ")
    private Integer id;

    private String fcmUid;

    private String name;

    private String email;

    private String password;

    private boolean active;

    private boolean verified;

    private boolean accountActive;

    private String verificationCode;

    private OffsetDateTime lastLoggedIn;

    private boolean guidedStepsViewed = false;   // default to false

    private boolean google2FAEnabled = false;

    private String google2FASecretKey;

    private int google2FAValidationCode;

    private boolean email2FAEnabled = false;

    private int email2FAOtp;

    private OffsetDateTime email2FAOtpRequestedTime;


    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCode> userCodes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
