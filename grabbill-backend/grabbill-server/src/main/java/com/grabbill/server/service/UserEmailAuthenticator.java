package com.grabbill.server.service;

import com.grabbill.core.entity.User;
import com.grabbill.core.service.UserService;
import com.grabbill.core.utils.OTPUtils;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * @author michaellow
 */
public class UserEmailAuthenticator implements EmailAuthenticator {

    @Value("${2fa.email.otp.ttl-seconds}")
    private long otpTtlInSeconds;

    @Autowired
    private UserService userService;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int generateOTP(final String email) {
        User target = getUser(email);
        target.setEmail2FAOtp(OTPUtils.generateOTP(6));
        target.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
        target = userService.save(target);

        return target.getEmail2FAOtp();
    }

    @Override
    public boolean authorize(final String email, final int otp) {
        User target = getUser(email);
        if (target.getEmail2FAOtpRequestedTime() == null) {
            return false;
        }

        long otpAgeInSeconds = ChronoUnit.SECONDS.between(
                target.getEmail2FAOtpRequestedTime(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        return ((target.getEmail2FAOtp() == otp) && (otpAgeInSeconds <= otpTtlInSeconds));
    }

    private User getUser(final String email) {
        return userService.getByEmail(email).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + email + "] is not found!"
                )
        );
    }

}
