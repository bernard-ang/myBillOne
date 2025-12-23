package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.TwoFactorAuthType;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.CustomerPaymentMethodService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AdminUserPayload;
import com.grabbill.server.controller.response.payload.UserAuthorityPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.dto.GrabbillAuthData;
import com.grabbill.server.dto.GrabbillToken;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.security.TokenCookieServices;
import com.grabbill.server.security.TokenServices;
import com.stripe.model.Customer;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class AuthServiceImpl implements AuthService {

    @Value("${2fa.auth-session.ttl-seconds}")
    private long authSessionTtlInSeconds;

    @Autowired
    private CustomerPaymentMethodService customerPaymentMethodService;

    @Autowired
    @Qualifier("userGoogleAuthenticator")
    private GoogleAuthenticator userGoogleAuthenticator;

    @Autowired
    @Qualifier("adminUserGoogleAuthenticator")
    private GoogleAuthenticator adminUserGoogleAuthenticator;

    @Autowired
    @Qualifier("userEmailAuthenticator")
    private EmailAuthenticator userEmailAuthenticator;

    @Autowired
    @Qualifier("adminUserEmailAuthenticator")
    private EmailAuthenticator adminUserEmailAuthenticator;

    @Autowired
    private TokenCookieServices tokenCookieServices;

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AccountPaymentCheckService accountPaymentCheckService;

    @Autowired
    private CustomerService customerService;

    @Transactional
    @Override
    public GrabbillAuthData loginForUser(final Authentication authentication) {
        if (authentication.isAuthenticated()) {
            String email = ((GrabbillUserDetails) authentication.getPrincipal()).getUser().getEmail();
            User user = userService.getByEmail(email).orElseThrow(
                    () -> new UsernameNotFoundException("User [" + email + "] is not found!"));

            HttpHeaders responseHeaders = new HttpHeaders();
            if (!user.isEmail2FAEnabled() && !user.isGoogle2FAEnabled()) {
                addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
                addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));

            } else if (user.isEmail2FAEnabled()) {
                int otp = userEmailAuthenticator.generateOTP(email);
                user.setEmail2FAOtp(otp);
                user.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
                user = userService.save(user);
            }

            GrabbillApiResponse responseBody = new GrabbillApiResponse(
                    GrabbillServerApiVersion.V1.getVersion(),
                    getUserAuthorityPayload(user));

            user.setLastLoggedIn(OffsetDateTime.now(ZoneOffset.UTC));
            userService.save(user);

            return new GrabbillAuthData(responseHeaders, responseBody);
        }

        throw new BadCredentialsException("Authentication failed for user [" + authentication.getName() + "]");
    }

    @Override
    public GrabbillAuthData loginForAdmin(final Authentication authentication) {
        if (authentication.isAuthenticated()) {
            String email = ((GrabbillAdminUserDetails) authentication.getPrincipal()).getAdminUser().getEmail();
            AdminUser adminUser = adminUserService.getByEmail(email).orElseThrow(
                    () -> new UsernameNotFoundException("Admin user [" + email + "] is not found!"));

            HttpHeaders responseHeaders = new HttpHeaders();
            if (!adminUser.isEmail2FAEnabled() && !adminUser.isGoogle2FAEnabled()) {
                addAdminAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(adminUser));
                addAdminRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(adminUser));

            } else if (adminUser.isEmail2FAEnabled()) {
                int otp = adminUserEmailAuthenticator.generateOTP(adminUser.getEmail());
                adminUser.setEmail2FAOtp(otp);
                adminUser.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
                adminUser = adminUserService.save(adminUser);
            }

            GrabbillApiResponse responseBody = new GrabbillApiResponse(
                    GrabbillServerApiVersion.V1.getVersion(),
                    AdminUserPayload.from(adminUser));

            adminUser.setLastLoggedIn(OffsetDateTime.now(ZoneOffset.UTC));
            adminUserService.save(adminUser);

            return new GrabbillAuthData(responseHeaders, responseBody);
        }

        throw new BadCredentialsException("Authentication failed for admin user [" + authentication.getName() + "]");
    }

    @Transactional
    @Override
    public GrabbillAuthData authorize(final String email) {
        User user = userService.getByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User [" + email + "] is not found!"));

        HttpHeaders responseHeaders = new HttpHeaders();
        if (!user.isEmail2FAEnabled() && !user.isGoogle2FAEnabled()) {
            addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
            addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));
        }

        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                getUserAuthorityPayload(user));

        return new GrabbillAuthData(responseHeaders, responseBody);
    }

    @Override
    public GrabbillAuthData twoFactorAuthorizeForUser(
            final TwoFactorAuthType authType,
            final String email,
            final int otp) {
        User user = userService.getByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User [" + email + "] is not found!"));

        long authSessionInSeconds = ChronoUnit.SECONDS.between(user.getLastLoggedIn(),
                OffsetDateTime.now(ZoneOffset.UTC));
        if (authSessionInSeconds > authSessionTtlInSeconds) {
            throw new PreAuthenticatedCredentialsNotFoundException("User [" + email + "] is not pre-authenticated!");
        }

        boolean authorized = false;
        HttpHeaders responseHeaders = new HttpHeaders();
        if (TwoFactorAuthType.EMAIL.equals(authType) && userEmailAuthenticator.authorize(email, otp)) {
            addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
            addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));
            authorized = true;

        } else if (TwoFactorAuthType.GOOGLE.equals(authType) && userGoogleAuthenticator.authorizeUser(email, otp)) {
            addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
            addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));
            authorized = true;

        } else if (TwoFactorAuthType.BOTH.equals(authType)) {
            if (userEmailAuthenticator.authorize(email, otp) || userGoogleAuthenticator.authorizeUser(email, otp)) {
                addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
                addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));
                authorized = true;
            }
        }

        if (!authorized) {
            throw new BadCredentialsException("Authorization failed for user - [" + email + "].");
        }

        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                getUserAuthorityPayload(user));

        return new GrabbillAuthData(responseHeaders, responseBody);
    }

    @Override
    public GrabbillAuthData twoFactorAuthorizeForAdmin(
            final TwoFactorAuthType authType,
            final String email,
            final int otp) {
        AdminUser adminUser = adminUserService.getByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Admin user [" + email + "] is not found!"));

        long authSessionInSeconds = ChronoUnit.SECONDS.between(adminUser.getLastLoggedIn(),
                OffsetDateTime.now(ZoneOffset.UTC));
        if (authSessionInSeconds > authSessionTtlInSeconds) {
            throw new PreAuthenticatedCredentialsNotFoundException(
                    "Admin user [" + email + "] is not pre-authenticated!");
        }

        boolean authorized = false;
        HttpHeaders responseHeaders = new HttpHeaders();
        if (TwoFactorAuthType.EMAIL.equals(authType) && adminUserEmailAuthenticator.authorize(email, otp)) {
            addAdminAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(adminUser));
            addAdminRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(adminUser));
            authorized = true;

        } else if (TwoFactorAuthType.GOOGLE.equals(authType)
                && adminUserGoogleAuthenticator.authorizeUser(email, otp)) {
            addAdminAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(adminUser));
            addAdminRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(adminUser));
            authorized = true;

        } else if (TwoFactorAuthType.BOTH.equals(authType)) {
            if (adminUserEmailAuthenticator.authorize(email, otp)
                    || adminUserGoogleAuthenticator.authorizeUser(email, otp)) {
                addAdminAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(adminUser));
                addAdminRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(adminUser));
                authorized = true;
            }
        }

        if (!authorized) {
            throw new BadCredentialsException("Authorization failed for admin user - [" + email + "].");
        }

        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                AdminUserPayload.from(adminUser));

        return new GrabbillAuthData(responseHeaders, responseBody);
    }

    @Transactional
    @Override
    public GrabbillAuthData refresh(
            final UserDetails userDetails,
            final String accessToken,
            final String refreshToken) {
        boolean refreshTokenValid = tokenServices.validateToken(refreshToken);
        if (!refreshTokenValid) {
            throw new BadCredentialsException("Invalid Refresh Token from user [" + userDetails.getUsername() + "]");
        }

        boolean isAdmin = tokenServices.isAdmin(refreshToken);

        HttpHeaders responseHeaders;
        GrabbillApiResponse responseBody;
        if (!isAdmin) {
            String email = tokenServices.getEmailFromToken(refreshToken);
            User user = userService.getByEmail(email).orElseThrow(
                    () -> new UsernameNotFoundException("User [" + email + "] is not found!"));

            responseHeaders = new HttpHeaders();
            addAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(user));
            addRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(user));
            responseBody = new GrabbillApiResponse(
                    GrabbillServerApiVersion.V1.getVersion(),
                    getUserAuthorityPayload(user));

        } else {
            String email = tokenServices.getEmailFromToken(refreshToken);
            AdminUser adminUser = adminUserService.getByEmail(email).orElseThrow(
                    () -> new UsernameNotFoundException("Admin user [" + email + "] is not found!"));

            responseHeaders = new HttpHeaders();
            addAdminAccessTokenCookie(responseHeaders, tokenServices.createAccessToken(adminUser));
            addAdminRefreshTokenCookie(responseHeaders, tokenServices.createRefreshToken(adminUser));
            responseBody = new GrabbillApiResponse(
                    GrabbillServerApiVersion.V1.getVersion(),
                    AdminUserPayload.from(adminUser));
        }

        return new GrabbillAuthData(responseHeaders, responseBody);
    }

    @Override
    public GrabbillAuthData logout(
            final UserDetails userDetails) {
        HttpHeaders responseHeaders = new HttpHeaders();
        if (userDetails instanceof GrabbillUserDetails) {
            deleteAccessTokenCookie(responseHeaders);
            deleteRefreshTokenCookie(responseHeaders);

        } else if (userDetails instanceof GrabbillAdminUserDetails) {
            deleteAdminAccessTokenCookie(responseHeaders);
            deleteAdminRefreshTokenCookie(responseHeaders);
        }

        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("User [" + userDetails.getUsername() + "] is logout successfully."));

        return new GrabbillAuthData(responseHeaders, responseBody);
    }

    @NotNull
    private UserAuthorityPayload getUserAuthorityPayload(final User user) {
        boolean paymentMethodRequired = false;
        boolean paymentGracePeriodExceeded = false;
        double outstandingAmount = 0;

        Optional<AccountSubscription> activeSubscription = Optional.empty();
        Customer customer = null;
        Account account = user.getAccount();
        if (account != null) {
            // Skip Stripe customer lookup/creation for payment exempted accounts
            if (!account.isPaymentExempted()) {
                customer = customerService.getOrCreate(account);
            }
            activeSubscription = accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId());

            if (activeSubscription.isPresent()) {
                AccountSubscription accountSubscription = activeSubscription.get();
                if (!accountSubscription.getPlanName().equals("Free")) {
                    List<Invoice> paidInvoices = invoiceService.getPaidInvoices(account);

                    // Only check Stripe payment method if customer exists (non-payment-exempted
                    // accounts)
                    com.stripe.model.PaymentMethod defaultPaymentMethod = null;
                    if (customer != null) {
                        defaultPaymentMethod = customerPaymentMethodService
                                .getDefaultPaymentMethodByCustomerId(customer.getId());
                    }
                    paymentMethodRequired = (defaultPaymentMethod == null) && paidInvoices.isEmpty()
                            && !account.isPaymentExempted();

                    // paid subscription, check if any unpaid invoice that is more than 14 days!
                    paymentGracePeriodExceeded = accountPaymentCheckService.isPaymentGracePeriodOver(account);
                }
            }

            for (Invoice unpaidInvoice : invoiceService.getUnpaidInvoices(account)) {
                outstandingAmount += unpaidInvoice.getTotalAmountWithTax();
            }
        }

        return UserAuthorityPayload.from(
                user,
                customer,
                activeSubscription.orElse(null),
                paymentMethodRequired,
                paymentGracePeriodExceeded,
                outstandingAmount);
    }

    @Transactional
    @Override
    public UserAuthorityPayload getUserAuthority(
            final GrabbillUserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User [" + userDetails.getUsername() + "] is not found!"));

        return getUserAuthorityPayload(user);
    }

    @Transactional
    @Override
    public UserAuthorityPayload getAdminUserAuthority(
            final GrabbillAdminUserDetails adminUserDetails) {
        AdminUser adminUser = adminUserService.getByEmail(adminUserDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User [" + adminUserDetails.getUsername() + "] is not found!"));

        return UserAuthorityPayload.from(adminUser);
    }

    private void addAccessTokenCookie(final HttpHeaders httpHeaders, final GrabbillToken grabbillToken) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.createAccessTokenCookie(
                        grabbillToken.getJwtTokenValue(),
                        grabbillToken.getDuration()).toString());
    }

    private void addRefreshTokenCookie(final HttpHeaders httpHeaders, final GrabbillToken grabbillToken) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.createRefreshTokenCookie(
                        grabbillToken.getJwtTokenValue(),
                        grabbillToken.getDuration()).toString());
    }

    private void deleteAccessTokenCookie(final HttpHeaders httpHeaders) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.deleteAccessTokenCookie().toString());
    }

    private void deleteRefreshTokenCookie(final HttpHeaders httpHeaders) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.deleteRefreshTokenCookie().toString());
    }

    private void addAdminAccessTokenCookie(final HttpHeaders httpHeaders, final GrabbillToken grabbillToken) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.createAdminAccessTokenCookie(
                        grabbillToken.getJwtTokenValue(),
                        grabbillToken.getDuration()).toString());
    }

    private void addAdminRefreshTokenCookie(final HttpHeaders httpHeaders, final GrabbillToken grabbillToken) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.createAdminRefreshTokenCookie(
                        grabbillToken.getJwtTokenValue(),
                        grabbillToken.getDuration()).toString());
    }

    private void deleteAdminAccessTokenCookie(final HttpHeaders httpHeaders) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.deleteAdminAccessTokenCookie().toString());
    }

    private void deleteAdminRefreshTokenCookie(final HttpHeaders httpHeaders) {
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                tokenCookieServices.deleteAdminRefreshTokenCookie().toString());
    }

}
