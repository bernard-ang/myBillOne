package com.grabbill.server.service;

import com.grabbill.core.model.TwoFactorAuthType;
import com.grabbill.server.controller.response.payload.UserAuthorityPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.dto.GrabbillAuthData;
import com.grabbill.server.dto.GrabbillUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provides API related to authentication services.
 *
 * @author michaellow
 */
public interface AuthService {

    GrabbillAuthData loginForUser(
            Authentication authentication
    );

    GrabbillAuthData loginForAdmin(
            Authentication authentication
    );

    GrabbillAuthData authorize(
            String email
    );

    GrabbillAuthData twoFactorAuthorizeForUser(
        TwoFactorAuthType authType,
        String email,
        int otp
    );

    GrabbillAuthData twoFactorAuthorizeForAdmin(
            TwoFactorAuthType authType,
            String email,
            int otp
    );

    GrabbillAuthData refresh(
            UserDetails userDetails,
            String accessToken,
            String refreshToken
    );

    GrabbillAuthData logout(
            UserDetails userDetails
    );

    UserAuthorityPayload getUserAuthority(
            GrabbillUserDetails userDetails
    );

    UserAuthorityPayload getAdminUserAuthority(
            GrabbillAdminUserDetails adminUserDetails
    );

}

