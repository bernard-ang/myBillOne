package com.grabbill.server.controller;

import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.model.AdminActionType;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.core.service.AdminAuditLogService;
import com.grabbill.core.service.AdminUserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.EmailOtpRequest;
import com.grabbill.server.controller.request.ForgetPasswordRequest;
import com.grabbill.server.controller.request.ResetPasswordRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.EmailAuthenticator;
import com.grabbill.server.service.UserAccountEmailService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mgmt/account")
public class AdminAccountController {

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserAccountEmailService userAccountEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("adminUserEmailAuthenticator")
    private EmailAuthenticator adminUserEmailAuthenticator;


    @PostMapping("/forget-password")
    public ResponseEntity<GrabbillApiResponse> forgetPassword(
            @Valid @RequestBody ForgetPasswordRequest request
    ) {
        AdminUser adminUser = getAdminUser(request.getEmail());
        adminUser.setVerificationCode(RandomStringUtils.randomAlphanumeric(15));
        adminUser = adminUserService.save(adminUser);
        userAccountEmailService.sendForgetPasswordEmail(adminUser);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(adminUser.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_FORGET_PASSWORD.name());
        adminAuditLog.setDescription("Forgot password URL for admin account with email [" + request.getEmail() + "] was triggered!");
        adminAuditLogService.save(adminAuditLog);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Forget password email " + adminUser.getEmail() + " sent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GrabbillApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        AdminUser adminUser = getAdminUser(request.getEmail());
        if (!adminUser.getVerificationCode().equals(request.getCode())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB7005,
                    "Failed to reset admin user [" + request.getEmail()
                            + "] password with invalid code [" + request.getCode() + "]!"
            );
        }

        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUserService.save(adminUser);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(adminUser.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_RESET_PASSWORD.name());
        adminAuditLog.setDescription("Forgot password URL for admin account with email [" + request.getEmail() + "] was triggered!");
        adminAuditLogService.save(adminAuditLog);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Password for " + request.getEmail() + " is reset successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PostMapping("/generate-email-otp")
    public ResponseEntity<GrabbillApiResponse> generateEmailOtp(
            @Valid @RequestBody EmailOtpRequest request
    ) {
        AdminUser adminUser = getAdminUser(request.getEmail());
        int otp = adminUserEmailAuthenticator.generateOTP(adminUser.getEmail());
        adminUser.setEmail2FAOtp(otp);
        adminUser.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
        adminUser = adminUserService.save(adminUser);

        userAccountEmailService.sendEmailOtpEmail(adminUser, otp);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(adminUser.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_GENERATE_EMAIL_OTP.name());
        adminAuditLog.setDescription("OTP email for [" + request.getEmail() + "] was generated!");
        adminAuditLogService.save(adminAuditLog);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("OTP email for " + adminUser.getEmail() + " sent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    private AdminUser getAdminUser(final String email) {
        return adminUserService.getByEmail(email).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7004,
                        "Admin user [" + email + "] does not exist!"
                )
        );
    }

}
