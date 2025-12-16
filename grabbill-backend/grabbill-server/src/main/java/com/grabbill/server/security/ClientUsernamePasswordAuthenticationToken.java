package com.grabbill.server.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author michaellow
 */
public class ClientUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public ClientUsernamePasswordAuthenticationToken(
            final Object principal,
            final Object credentials
    ) {
        super(principal, credentials);
    }

    public ClientUsernamePasswordAuthenticationToken(
            final Object principal,
            final Object credentials,
            final Collection<? extends GrantedAuthority> authorities
    ) {
        super(principal, credentials, authorities);
    }

}
