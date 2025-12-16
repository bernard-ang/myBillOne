package com.grabbill.server.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.TwoFactorAuthType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.TwoFactorAuthUpdateRequest;
import com.grabbill.server.controller.request.TwoFactorAuthorizationRequest;
import com.grabbill.server.controller.request.UserPasswordChangeRequest;
import com.grabbill.server.controller.request.UserProfileUpdateRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.UserPayload;
import com.grabbill.server.controller.response.payload.UserProfilePayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.EmailAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * @author michaellow
 */
@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Value("${2fa.google.app-name.user}")
    private String appName;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QRCodeWriter qrCodeWriter;

    @Autowired
    @Qualifier("userGoogleAuthenticator")
    private GoogleAuthenticator userGoogleAuthenticator;

    @Autowired
    @Qualifier("userEmailAuthenticator")
    private EmailAuthenticator userEmailAuthenticator;

    @Autowired
    @Qualifier("userGoogleAuthenticatorConfig")
    private GoogleAuthenticatorConfig userGoogleAuthenticatorConfig;


    @PostMapping(value = "/change-password")
    public ResponseEntity<GrabbillApiResponse> changePassword(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserPasswordChangeRequest request
    ) {
        User user = getUser(userDetails);
        if (user.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1003,
                    "User password for [" + userDetails.getUsername() + "] does not match!"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.CHANGE_PASSWORD,
                user.getEmail(),
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Password for user account " + user.getEmail() + " updated successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PutMapping(value = "/mark-guided-steps-viewed")
    public ResponseEntity<GrabbillApiResponse> markGuidedStepsViewed(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        Optional<User> userOptional = userService.getByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + userDetails.getUsername() + "] is not found!"
            );
        }

        User currentUser = userOptional.get();
        currentUser.setGuidedStepsViewed(true);
        User savedInstance = userService.save(currentUser);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(savedInstance.getId()),
                getDomainType(),
                ActionType.MARK_GUIDED_STEPS_VIEWED,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getUserProfile(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        User user = getUser(userDetails);
        UserProfilePayload userProfilePayload = UserProfilePayload.from(user);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userProfilePayload
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> updateUserProfile(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        User user = getUser(userDetails);
        user.setName(request.getName());
        UserProfilePayload userProfilePayload = UserProfilePayload.from(userService.save(user));

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.UPDATE,
                user.getEmail(),
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userProfilePayload
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PutMapping(value = "/2fa")
    public ResponseEntity<GrabbillApiResponse> updateUser2FA(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody TwoFactorAuthUpdateRequest request
    ) {
        if (request.isGoogle2FAEnabled() && !getUser(userDetails).isGoogle2FAEnabled()) {
            userGoogleAuthenticator.createCredentials(userDetails.getUsername());
        }

        User user = getUser(userDetails);
        if (!request.isGoogle2FAEnabled()) {
            user.setGoogle2FAEnabled(false);
            user.setGoogle2FASecretKey(null);
            user.setGoogle2FAValidationCode(-999999);
        }

        if (!request.isEmail2FAEnabled()) {
            user.setEmail2FAEnabled(false);
            user.setEmail2FAOtp(-999999);
            user.setEmail2FAOtpRequestedTime(null);
        }

        UserProfilePayload userProfilePayload = UserProfilePayload.from(userService.save(user));
        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.UPDATE,
                user.getEmail(),
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userProfilePayload
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/2fa/generate-qr")
    public ResponseEntity<ByteArrayResource> generate2FaQrCode(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) throws Exception {
        User user = getUser(userDetails);
        if (user.getGoogle2FASecretKey() == null || user.getGoogle2FAValidationCode() == -999999) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1015,
                    "Google 2fa not enabled for user [" + userDetails.getUsername() + "]!"
            );
        }

        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(user.getGoogle2FASecretKey().replace("=", ""))
                .setConfig(userGoogleAuthenticatorConfig)
                .setVerificationCode(user.getGoogle2FAValidationCode())
                .build();

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                appName,
                userDetails.getUsername(),
                key
        );

        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

        return new ResponseEntity(
                new ByteArrayResource(baos.toByteArray()),
                createReportDownloadHttpHeaders(baos.toByteArray().length),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/2fa/activate")
    public ResponseEntity<GrabbillApiResponse> activate2FA(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody TwoFactorAuthorizationRequest request
    ) {
        TwoFactorAuthType authType = request.getAuthType();
        String email = request.getEmail();
        int otp = request.getOtp();

        User user = getUser(userDetails);
        boolean authorized = false;
        if (TwoFactorAuthType.BOTH.equals(authType)) {
            return ResponseEntity.badRequest().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiMessage("Two factor authentication type [BOTH] is not supported.")
                    )
            );

        } else if (TwoFactorAuthType.EMAIL.equals(authType) && userEmailAuthenticator.authorize(email, otp)) {
            user.setEmail2FAEnabled(true);
            authorized = true;

        } else if (TwoFactorAuthType.GOOGLE.equals(authType) && userGoogleAuthenticator.authorizeUser(email, otp)) {
            user.setGoogle2FAEnabled(true);
            authorized = true;
        }

        if (!authorized) {
            return ResponseEntity.badRequest().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiMessage("Two factor authentication activation failed.")
                    )
            );
        }

        userService.save(user);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Two factor authentication activated successfully.")
                )
        );
    }

    private User getUser(final GrabbillUserDetails userDetails) {
        return userService.getByEmail(userDetails.getUsername()).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + userDetails.getUsername() + "] is not found!"
                )
        );
    }

    private HttpHeaders createReportDownloadHttpHeaders(final int dataLength) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"2fa-qr.png\"");
        headers.setContentLength(dataLength);

        return headers;
    }

    public DomainType getDomainType() {
        return DomainType.USER_PROFILE;
    }
}
