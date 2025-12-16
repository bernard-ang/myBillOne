package com.grabbill.server.service;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * @author michaellow
 */
public class AuditorService implements AuditorAware<String> {

    private static final String SYSTEM = "system";


    @Override
    public Optional<String> getCurrentAuditor() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            return Optional.empty();
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return Optional.ofNullable(((UserDetails) principal).getUsername());

        } else {
            if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
                return Optional.of(SYSTEM);
            }

            return Optional.ofNullable(principal.toString());
        }

    }

}
