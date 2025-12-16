package com.grabbill.server.security;

import org.springframework.http.HttpCookie;

/**
 * @author michaellow
 */
public interface TokenCookieServices {

    HttpCookie createAccessTokenCookie(String token, Long duration);

    HttpCookie createRefreshTokenCookie(String token, Long duration);

    HttpCookie deleteAccessTokenCookie();

    HttpCookie deleteRefreshTokenCookie();

    HttpCookie createAdminAccessTokenCookie(String token, Long duration);

    HttpCookie createAdminRefreshTokenCookie(String token, Long duration);

    HttpCookie deleteAdminAccessTokenCookie();

    HttpCookie deleteAdminRefreshTokenCookie();

}
