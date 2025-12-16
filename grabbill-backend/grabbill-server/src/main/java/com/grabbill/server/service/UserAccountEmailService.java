package com.grabbill.server.service;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.entity.User;

/**
 * @author michaellow
 */
public interface UserAccountEmailService {

    void sendPasswordResetEmail(User user, String decodedPassword);

    void sendForgetPasswordEmail(User user);

    void sendNewUserWelcomeEmail(User user, String decodedPassword);

    void sendAccountVerificationEmail(User user);

    void sendForgetPasswordEmail(AdminUser adminUser);

    void sendEmailOtpEmail(User user, int otp);

    void sendEmail2faActivationOtpEmail(User user, int otp);

    void sendEmailOtpEmail(AdminUser adminUser, int otp);

}
