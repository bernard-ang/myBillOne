package com.grabbill.server.security;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.UserType;
import com.grabbill.server.dto.GrabbillToken;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author michaellow
 */
public class TokenServicesImpl implements TokenServices {

    @Value("${security.access-token.expiration.duration:900}")
    private Integer accessTokenExpirationDuration;

    @Value("${security.refresh-token.expiration.duration:3600}")
    private Integer refreshTokenExpirationDuration;

    @Value("${security.token.signing-key}")
    private String tokenSecret;
    

    @Override
    public GrabbillToken createAccessToken(final User user) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiry = now.plusSeconds(accessTokenExpirationDuration);
        Instant instant = expiry.toInstant();

        return new GrabbillToken(
                GrabbillToken.Type.ACCESS,
                UserType.CLIENT,
                createJwtToken(user.getEmail(), false, now, expiry),
                // KLUDGE: browser will remove expired cookie immediately
                // extending access token cookie duration to refresh token expiry duration to keep it
                ChronoUnit.SECONDS.between(now, now.plusSeconds(refreshTokenExpirationDuration)),
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        );
    }

    @Override
    public GrabbillToken createRefreshToken(final User user) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiry = now.plusSeconds(refreshTokenExpirationDuration);
        Instant instant = now.plusSeconds(refreshTokenExpirationDuration).toInstant();

        return new GrabbillToken(
                GrabbillToken.Type.REFRESH,
                UserType.CLIENT,
                createJwtToken(user.getEmail(), false, now, expiry),
                ChronoUnit.SECONDS.between(now, expiry),
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        );
    }

    @Override
    public GrabbillToken createAccessToken(final AdminUser adminUser) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiry = now.plusSeconds(accessTokenExpirationDuration);
        Instant instant = expiry.toInstant();

        return new GrabbillToken(
                GrabbillToken.Type.ACCESS,
                UserType.ADMIN,
                createJwtToken(adminUser.getEmail(), true, now, expiry),
                // KLUDGE: browser will remove expired cookie immediately
                // extending access token cookie duration to refresh token expiry duration to keep it
                ChronoUnit.SECONDS.between(now, now.plusSeconds(refreshTokenExpirationDuration)),
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        );
    }

    @Override
    public GrabbillToken createRefreshToken(final AdminUser adminUser) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiry = now.plusSeconds(refreshTokenExpirationDuration);
        Instant instant = now.plusSeconds(refreshTokenExpirationDuration).toInstant();

        return new GrabbillToken(
                GrabbillToken.Type.REFRESH,
                UserType.ADMIN,
                createJwtToken(adminUser.getEmail(), true, now, expiry),
                ChronoUnit.SECONDS.between(now, expiry),
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        );
    }

    private String createJwtToken(
            final String subject,
            final boolean isAdmin,
            final OffsetDateTime now,
            final OffsetDateTime expiry
    ) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiry.toInstant()))
                .claim("admin", isAdmin)
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
    }

    @Override
    public boolean validateToken(final String token) {
        try {
            Jwts.parser().setSigningKey(tokenSecret).parse(token);
            return true;

        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String getEmailFromToken(final String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @Override
    public boolean isAdmin(final String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
        return (boolean) claims.get("admin");
    }

    @Override
    public LocalDateTime getExpiryDateFromToken(final String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

}

