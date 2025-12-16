package com.grabbill.server.security;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.entity.User;
import com.grabbill.server.dto.GrabbillToken;

import java.time.LocalDateTime;

/**
 * Provides API related to authorization token.
 *
 * @author michaellow
 */
public interface TokenServices {

    GrabbillToken createAccessToken(User user);

    GrabbillToken createRefreshToken(User user);

    GrabbillToken createAccessToken(AdminUser adminUser);

    GrabbillToken createRefreshToken(AdminUser adminUser);

    boolean validateToken(String token);

    String getEmailFromToken(String token);

    boolean isAdmin(String token);

    LocalDateTime getExpiryDateFromToken(String token);

}
