package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.AdminActionType;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AdminAuditLogService;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.AuthorizationRequest;
import com.grabbill.server.controller.request.LoginRequest;
import com.grabbill.server.controller.request.TwoFactorAuthorizationRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.UserAuthorityPayload;
import com.grabbill.server.dto.FcmIdTokenPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.dto.GrabbillAuthData;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.exception.InactiveAccountException;
import com.grabbill.server.exception.UnverifiedAccountException;
import com.grabbill.server.security.AdminUsernamePasswordAuthenticationToken;
import com.grabbill.server.security.ClientUsernamePasswordAuthenticationToken;
import com.grabbill.server.security.TokenCryptoServices;
import com.grabbill.server.service.AuthService;
import com.grabbill.server.service.FcmAuthService;
import com.grabbill.server.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;


/**
 * @author michaellow
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private FcmAuthService fcmAuthService;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenCryptoServices tokenCryptoServices;


    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrabbillApiResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new ClientUsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (DisabledException e) {
            User user = userService.getByEmail(loginRequest.getEmail()).get();
            if (!user.isVerified()) {
                throw new UnverifiedAccountException("User account has not been verified", e);
            } else {
                throw new InactiveAccountException("User account is disabled", e);
            }
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        GrabbillAuthData authData = authService.loginForUser(authentication);

        User user = ((GrabbillUserDetails) authentication.getPrincipal()).getUser();
        Account account = user.getAccount();
        auditLogService.log(
                account.getId(),
                Optional.empty(),
                user.getId().longValue(),
                DomainType.AUTH,
                ActionType.LOGIN,
                "User with email [" + user.getEmail() + "] has login.",
                "system"
        );

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping(
            value = "/admin/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrabbillApiResponse> adminLogin(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new AdminUsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        GrabbillAuthData authData = authService.loginForAdmin(authentication);

        AdminUser user = ((GrabbillAdminUserDetails) authentication.getPrincipal()).getAdminUser();
        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.AUTH);
        adminAuditLog.setTargetId(user.getId());
        adminAuditLog.setActionType(AdminActionType.LOGIN.name());
        adminAuditLog.setDescription("Admin user with email [" + loginRequest.getEmail() + "] has login.");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping(
            value = "/authorize",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrabbillApiResponse> authorize(
            @Valid @RequestBody AuthorizationRequest request
    ) {
        if (!"google".equalsIgnoreCase(request.getAuthProvider())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0101,
                    "Unsupported external authentication provider " + request.getAuthProvider() + "."
            );
        }
        FcmIdTokenPayload payload = fcmAuthService.authenticate(request.getToken());
        GrabbillAuthData authData;
        try {
            authData = authService.authorize(payload.getEmail());

        } catch (UsernameNotFoundException ex) {
            // new user... create new user account
            userRegistrationService.registerByFcm(
                    payload.getUid(),
                    payload.getName(),
                    payload.getEmail(),
                    null
            );
            authData = authService.authorize(payload.getEmail());
        }

        fcmAuthService.revoke(payload);

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @Transactional
    @PostMapping(
            value = "/2-factor-auth",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrabbillApiResponse> twoFactorAuthorize(
            @Valid @RequestBody TwoFactorAuthorizationRequest request
    ) {
        GrabbillAuthData authData = authService.twoFactorAuthorizeForUser(
                request.getAuthType(),
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @Transactional
    @PostMapping(
            value = "/admin/2-factor-auth",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrabbillApiResponse> adminTwoFactorAuthorize(
            @Valid @RequestBody TwoFactorAuthorizationRequest request
    ) {
        GrabbillAuthData authData = authService.twoFactorAuthorizeForAdmin(
                request.getAuthType(),
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GrabbillApiResponse> refreshToken(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @CookieValue(name = "act", required = false) String accessToken,
            @CookieValue(name = "rft", required = false) String refreshToken
    ) {
        String decryptedAccessToken = accessToken != null ? tokenCryptoServices.decrypt(accessToken) : null;
        String decryptedRefreshToken = tokenCryptoServices.decrypt(refreshToken);
        GrabbillAuthData authData = authService.refresh(userDetails, decryptedAccessToken, decryptedRefreshToken);

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping(value = "/admin/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GrabbillApiResponse> adminRefreshToken(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @CookieValue(name = "aact", required = false) String accessToken,
            @CookieValue(name = "arft", required = false) String refreshToken
    ) {
        String decryptedAccessToken = accessToken != null ? tokenCryptoServices.decrypt(accessToken) : null;
        String decryptedRefreshToken = tokenCryptoServices.decrypt(refreshToken);
        GrabbillAuthData authData = authService.refresh(userDetails, decryptedAccessToken, decryptedRefreshToken);

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping("/logout")
    public ResponseEntity<GrabbillApiResponse> logout(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        GrabbillAuthData authData = authService.logout(userDetails);
        User user = userDetails.getUser();
        Account account = user.getAccount();
        auditLogService.log(
                account.getId(),
                Optional.empty(),
                user.getId().longValue(),
                DomainType.AUTH,
                ActionType.LOGOUT,
                "User with email [" + userDetails.getUsername() + "] has logout.",
                "system"
        );

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<GrabbillApiResponse> adminLogout(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails
    ) {
        GrabbillAuthData authData = authService.logout(userDetails);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.AUTH);
        adminAuditLog.setTargetId(userDetails.getAdminUser().getId());
        adminAuditLog.setActionType(AdminActionType.LOGOUT.name());
        adminAuditLog.setDescription("Admin user with email [" + userDetails.getUsername() + "] has logout.");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().headers(authData.getHeaders()).body(authData.getBody());
    }

    @GetMapping("/authority")
    public ResponseEntity<GrabbillApiResponse> getUserAuthority(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        UserAuthorityPayload userAuthorityPayload = authService.getUserAuthority(userDetails);
        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userAuthorityPayload
        );

        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/admin/authority")
    public ResponseEntity<GrabbillApiResponse> getAdminUserAuthority(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails
    ) {
        UserAuthorityPayload userAuthorityPayload = authService.getAdminUserAuthority(userDetails);
        GrabbillApiResponse responseBody = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userAuthorityPayload
        );

        return ResponseEntity.ok().body(responseBody);
    }

}
