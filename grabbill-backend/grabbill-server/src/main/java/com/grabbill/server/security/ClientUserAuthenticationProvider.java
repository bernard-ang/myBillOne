package com.grabbill.server.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

/**
 * @author michaellow
 */
public class ClientUserAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(ClientUsernamePasswordAuthenticationToken.class);
    }

}
