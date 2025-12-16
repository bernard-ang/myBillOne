package com.grabbill.server.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.model.TwoFactorAuthType;
import com.grabbill.core.service.AdminUserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.TwoFactorAuthUpdateRequest;
import com.grabbill.server.controller.request.TwoFactorAuthorizationRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AdminUserPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mgmt/profile")
public class AdminUserProfileManagementController extends BaseManagementController {

    @Value("${2fa.google.app-name.admin}")
    private String appName;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private QRCodeWriter qrCodeWriter;

    @Autowired
    @Qualifier("adminUserGoogleAuthenticator")
    private GoogleAuthenticator adminUserGoogleAuthenticator;

    @Autowired
    @Qualifier("adminUserGoogleAuthenticatorConfig")
    private GoogleAuthenticatorConfig adminUserGoogleAuthenticatorConfig;


    @Transactional
    @PutMapping(value = "/2fa")
    public ResponseEntity<GrabbillApiResponse> updateAdminUser2FA(
            @AuthenticationPrincipal GrabbillAdminUserDetails adminUserDetails,
            @Valid @RequestBody TwoFactorAuthUpdateRequest request
    ) {
        if (request.isGoogle2FAEnabled() && !getAdminUser(adminUserDetails).isGoogle2FAEnabled()) {
            adminUserGoogleAuthenticator.createCredentials(adminUserDetails.getUsername());
        }

        AdminUser adminUser = getAdminUser(adminUserDetails);
        if (!request.isGoogle2FAEnabled()) {
            adminUser.setGoogle2FAEnabled(false);
            adminUser.setGoogle2FASecretKey(null);
            adminUser.setGoogle2FAValidationCode(-999999);
        }

        if (!request.isEmail2FAEnabled()) {
            adminUser.setEmail2FAEnabled(false);
            adminUser.setEmail2FAOtp(-999999);
            adminUser.setEmail2FAOtpRequestedTime(null);
        }

        AdminUserPayload adminUserPayload = AdminUserPayload.from(adminUserService.save(adminUser));

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                adminUserPayload
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/2fa/generate-qr")
    public ResponseEntity<ByteArrayResource> generate2FaQrCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails adminUserDetails
    ) throws Exception {
        AdminUser adminUser = getAdminUser(adminUserDetails);
        if (adminUser.getGoogle2FASecretKey() == null || adminUser.getGoogle2FAValidationCode() == -999999) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1015,
                    "Google 2fa not enabled for admin user [" + adminUserDetails.getUsername() + "]!"
            );
        }

        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(adminUser.getGoogle2FASecretKey())
                .setConfig(adminUserGoogleAuthenticatorConfig)
                .setVerificationCode(adminUser.getGoogle2FAValidationCode())
                .build();

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                appName,
                adminUserDetails.getUsername(),
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
            @AuthenticationPrincipal GrabbillAdminUserDetails adminUserDetails,
            @Valid @RequestBody TwoFactorAuthorizationRequest request
    ) {
        TwoFactorAuthType authType = request.getAuthType();
        String email = request.getEmail();
        int otp = request.getOtp();

        AdminUser adminUser = getAdminUser(adminUserDetails);
        boolean authorized = false;
        if (TwoFactorAuthType.BOTH.equals(authType)) {
            return ResponseEntity.badRequest().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiMessage("Two factor authentication type [BOTH] is not supported.")
                    )
            );

        } else if (TwoFactorAuthType.EMAIL.equals(authType) && adminUserGoogleAuthenticator.authorize(email, otp)) {
            adminUser.setEmail2FAEnabled(true);
            authorized = true;

        } else if (TwoFactorAuthType.GOOGLE.equals(authType) && adminUserGoogleAuthenticator.authorizeUser(email, otp)) {
            adminUser.setGoogle2FAEnabled(true);
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

        adminUserService.save(adminUser);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Two factor authentication activated successfully.")
                )
        );
    }

    private AdminUser getAdminUser(final GrabbillAdminUserDetails adminUserDetails) {
        return adminUserService.getByEmail(adminUserDetails.getUsername()).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin user [" + adminUserDetails.getUsername() + "] is not found!"
                )
        );
    }

    private HttpHeaders createReportDownloadHttpHeaders(final int dataLength) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"admin-2fa-qr.png\"");
        headers.setContentLength(dataLength);

        return headers;
    }

}
