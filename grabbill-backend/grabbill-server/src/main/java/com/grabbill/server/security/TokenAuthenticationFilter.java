package com.grabbill.server.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author michaellow
 **/
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Value("${security.access-token.cookie.name:act}")
    private String accessTokenCookieName;

    @Value("${security.refresh-token.cookie.name:rft}")
    private String refreshTokenCookieName;

    @Value("${security.admin-access-token.cookie.name:act}")
    private String adminAccessTokenCookieName;

    @Value("${security.admin-refresh-token.cookie.name:rft}")
    private String adminRefreshTokenCookieName;

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    private TokenCryptoServices tokenCryptoServices;

    @Autowired
    @Qualifier("clientUserDetailsService")
    private UserDetailsService clientUserDetailsService;

    @Autowired
    @Qualifier("adminUserDetailsService")
    private UserDetailsService adminUserDetailsService;


    @Override
    protected void doFilterInternal(
            @NotNull final HttpServletRequest httpServletRequest,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {

            String jwt;
            if (httpServletRequest.getRequestURL().toString().contains("/mgmt/") || httpServletRequest.getRequestURL().toString().contains("/admin/")) {
                jwt = getAdminJwtFromCookie(httpServletRequest);

            } else {
                jwt = getJwtFromCookie(httpServletRequest);
            }

            // NOTE: this is to handle where API is called after access token cookie is expired and removed
            //       we should still allow user to refresh the token cookies based on valid refresh token cookie
            if (httpServletRequest.getRequestURL().toString().endsWith("/auth/admin/refresh")) {
                jwt = getAdminRefreshJwtFromCookie(httpServletRequest);

            } else if (httpServletRequest.getRequestURL().toString().endsWith("/auth/refresh")) {
                jwt = getRefreshJwtFromCookie(httpServletRequest);
            }

            // skip auth validation if condition matches
            if (    // auth related
                    !httpServletRequest.getRequestURL().toString().endsWith("/auth/login")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/auth/admin/login")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/auth/authorize")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/auth/2-factor-auth")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/auth/admin/2-factor-auth")

                    // account registration related
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/register")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/verify-email")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/forget-password")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/reset-password")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/resend-verify-email")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/generate-email-otp")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/account/generate-email-2fa-activation-otp")

                    // email tracking related
                    && !httpServletRequest.getRequestURL().toString().endsWith("/unsubscribe")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/plans")
                    && !httpServletRequest.getRequestURL().toString().endsWith("/build-info")
                    && !httpServletRequest.getRequestURL().toString().contains("/track/")
                    && !httpServletRequest.getRequestURL().toString().contains("/clicks/")
                    && !httpServletRequest.getRequestURL().toString().contains("/ext/")

                    // admin account management related (forgot / reset password etc.)
                    && !httpServletRequest.getRequestURL().toString().contains("/mgmt/account/")

                    // payment callback related
                    && !httpServletRequest.getRequestURL().toString().contains("/callback/stripe/sessions")
                    && !httpServletRequest.getRequestURL().toString().contains("/webhook/stripe/events")
                    && StringUtils.hasText(jwt)
                    && tokenServices.validateToken(jwt)
            ) {
                String username = tokenServices.getEmailFromToken(jwt);
                boolean isAdmin = tokenServices.isAdmin(jwt);

                UsernamePasswordAuthenticationToken authentication;
                if (isAdmin) {
                    UserDetails userDetails = adminUserDetailsService.loadUserByUsername(username);
                    authentication = new AdminUsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                } else {
                    UserDetails userDetails = clientUserDetailsService.loadUserByUsername(username);
                    authentication = new ClientUsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                }

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception ex) {
//            PrintWriter pw = new PrintWriter(new StringWriter());
//            ex.printStackTrace(pw);
//            LOGGER.warn(pw.toString());
            LOGGER.error("Failed to set user authentication in security context", ex);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private String getJwtFromCookie(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (accessTokenCookieName.equals(cookie.getName())) {
                    String accessToken = cookie.getValue();
                    if (!StringUtils.hasLength(accessToken)) {
                        return null;
                    }

                    return tokenCryptoServices.decrypt(accessToken);
                }
            }
        }
        return null;
    }

    private String getAdminJwtFromCookie(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (adminAccessTokenCookieName.equals(cookie.getName())) {
                    String accessToken = cookie.getValue();
                    if (!StringUtils.hasLength(accessToken)) {
                        return null;
                    }

                    return tokenCryptoServices.decrypt(accessToken);
                }
            }
        }
        return null;
    }

    private String getRefreshJwtFromCookie(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    if (!StringUtils.hasLength(refreshToken)) {
                        return null;
                    }

                    return tokenCryptoServices.decrypt(refreshToken);
                }
            }
        }
        return null;
    }

    private String getAdminRefreshJwtFromCookie(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (adminRefreshTokenCookieName.equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    if (!StringUtils.hasLength(refreshToken)) {
                        return null;
                    }

                    return tokenCryptoServices.decrypt(refreshToken);
                }
            }
        }
        return null;
    }

}
