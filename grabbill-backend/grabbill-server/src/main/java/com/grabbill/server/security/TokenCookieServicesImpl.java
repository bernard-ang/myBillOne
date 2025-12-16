package com.grabbill.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;

/**
 * @author michaellow
 **/
public class TokenCookieServicesImpl implements TokenCookieServices {

    @Value("${security.access-token.cookie.name:act}")
    private String accessTokenCookieName;

    @Value("${security.refresh-token.cookie.name:rft}")
    private String refreshTokenCookieName;

    @Value("${security.admin-access-token.cookie.name:act}")
    private String adminAccessTokenCookieName;

    @Value("${security.admin-refresh-token.cookie.name:rft}")
    private String adminRefreshTokenCookieName;

    @Autowired
    private TokenCryptoServices tokenCryptoServices;


    @Override
    public HttpCookie createAccessTokenCookie(
            final String token,
            final Long duration
    ) {
        String encryptedToken = tokenCryptoServices.encrypt(token);
        return ResponseCookie.from(accessTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie createRefreshTokenCookie(
            final String token,
            final Long duration
    ) {
        String encryptedToken = tokenCryptoServices.encrypt(token);
        return ResponseCookie.from(refreshTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie createAdminAccessTokenCookie(
            final String token,
            final Long duration
    ) {
        String encryptedToken = tokenCryptoServices.encrypt(token);
        return ResponseCookie.from(adminAccessTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie createAdminRefreshTokenCookie(
            final String token,
            final Long duration
    ) {
        String encryptedToken = tokenCryptoServices.encrypt(token);
        return ResponseCookie.from(adminRefreshTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie deleteAdminAccessTokenCookie() {
        return ResponseCookie.from(adminAccessTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @Override
    public HttpCookie deleteAdminRefreshTokenCookie() {
        return ResponseCookie.from(adminRefreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build();
    }

}