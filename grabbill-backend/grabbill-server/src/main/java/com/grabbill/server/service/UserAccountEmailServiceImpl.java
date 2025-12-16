package com.grabbill.server.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.entity.User;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.service.SmtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author michaellow
 */
@Slf4j
public class UserAccountEmailServiceImpl implements UserAccountEmailService {

    @Value("${email-template.new-account-verification.url}")
    private String emailVerificationUrl;

    @Value("${email-template.new-account-reset-password.url}")
    private String resetPasswordUrl;

    @Value("${email-template.admin-account-reset-password.url}")
    private String adminResetPasswordUrl;

    @Value("${email-template.new-account-verification.subject}")
    private String emailSubject;

    @Value("${email-template.password-reset.subject}")
    private String passwordResetEmailSubject;

    @Value("${email-template.forget-password.subject}")
    private String forgetPasswordEmailSubject;

    @Value("${email-template.admin-account-forget-password.subject}")
    private String adminForgetPasswordEmailSubject;

    @Value("${email-template.new-user-welcome.subject}")
    private String newUserAccountEmailSubject;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private SmtpService smtpService;

    @Autowired
    private DeploymentProperties deploymentProperties;

    @Value("${deployment.mode.on-premise.email.logo}")
    private String companyLogo;

    @Value("${deployment.mode.on-premise.email.contact}")
    private String companyContactEmail;


    @Override
    public void sendPasswordResetEmail(final User user, String decodedPassword) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", user.getName());
        parameterMap.put("password", decodedPassword);
        appendOnPremiseParametersIfApplicable(user, parameterMap);

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = !deploymentProperties.isOnPremise() ?
                    handlebars.compile("password-reset-template") :
                    handlebars.compile("password-reset-template-op");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, user.getEmail());
            message.setSubject(passwordResetEmailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send password reset notification email to user - " + user.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendForgetPasswordEmail(User user) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", user.getName());
        parameterMap.put("resetPasswordLink", resetPasswordUrl + "?"
                + "email=" + urlEncode(user.getEmail())
                + "&code=" + urlEncode(user.getVerificationCode())
        );
        appendOnPremiseParametersIfApplicable(user, parameterMap);

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = !deploymentProperties.isOnPremise() ?
                    handlebars.compile("forget-password-template") :
                    handlebars.compile("forget-password-template-op");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, user.getEmail());
            message.setSubject(forgetPasswordEmailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send forget password email to new user - " + user.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendNewUserWelcomeEmail(
            final User user,
            final String decodedPassword
    ) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", user.getName());
        parameterMap.put("username", user.getEmail());
        parameterMap.put("password", decodedPassword);
        appendOnPremiseParametersIfApplicable(user, parameterMap);

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = !deploymentProperties.isOnPremise() ?
                    handlebars.compile("new-user-welcome-template") :
                    handlebars.compile("new-user-welcome-template-op");



            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, user.getEmail());
            message.setSubject(newUserAccountEmailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send new user account created notification email to user - " + user.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendAccountVerificationEmail(
            final User registeredUser
    ) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", registeredUser.getName());
        parameterMap.put("verificationLink", emailVerificationUrl + "?"
                + "email=" + urlEncode(registeredUser.getEmail())
                + "&verificationCode=" + urlEncode(registeredUser.getVerificationCode())
        );

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("email-verification-template");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, registeredUser.getEmail());
            message.setSubject(emailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send verification email to new user - " + registeredUser.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendForgetPasswordEmail(final AdminUser adminUser) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", adminUser.getName());
        parameterMap.put("resetPasswordLink", adminResetPasswordUrl + "?"
                + "email=" + urlEncode(adminUser.getEmail())
                + "&code=" + urlEncode(adminUser.getVerificationCode())
        );

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("admin-forget-password-template");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, adminUser.getEmail());
            message.setSubject(adminForgetPasswordEmailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send forget password email to admin user - " + adminUser.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendEmailOtpEmail(final User user, final int otp) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", user.getName());
        parameterMap.put("otp", String.valueOf(otp));
        appendOnPremiseParametersIfApplicable(user, parameterMap);

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = !deploymentProperties.isOnPremise() ?
                    handlebars.compile("otp-email-template") :
                    handlebars.compile("otp-email-template-op");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, user.getEmail());
            message.setSubject("Your myBillOne MFA Login OTP");
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send OTP email to user - " + user.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendEmail2faActivationOtpEmail(final User user, final int otp) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", user.getName());
        parameterMap.put("otp", String.valueOf(otp));
        appendOnPremiseParametersIfApplicable(user, parameterMap);

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = !deploymentProperties.isOnPremise() ?
                    handlebars.compile("otp-email-2fa-activation-template") :
                    handlebars.compile("otp-email-2fa-activation-template-op");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, user.getEmail());
            message.setSubject("Activate Multi-Factor Authentication for your myBillOne account");
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send OTP for email 2fa activation to user - " + user.getEmail(),
                    e
            );
        }
    }

    @Override
    public void sendEmailOtpEmail(final AdminUser adminUser, final int otp) {
        MimeMessage message = smtpService.createMimeMessage();
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("name", adminUser.getName());
        parameterMap.put("otp", String.valueOf(otp));

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("otp-email-template");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, adminUser.getEmail()); // "michael@evos.tech");
            message.setSubject("Your myBillOne MFA Login OTP");
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send OTP email to admin user - " + adminUser.getEmail(),
                    e
            );
        }
    }

    private String urlEncode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void appendOnPremiseParametersIfApplicable(
            final User user,
            final Map<String, String> parameterMap
    ) {
        if (deploymentProperties.isOnPremise()) {
            Account account = user.getAccount();
            parameterMap.put("companyName", account.getCompanyName());
            parameterMap.put("companyLogo", companyLogo);
            parameterMap.put("contactEmail", companyContactEmail);
        }
    }

}
