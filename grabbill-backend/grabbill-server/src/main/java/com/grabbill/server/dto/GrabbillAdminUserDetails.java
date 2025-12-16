package com.grabbill.server.dto;

import com.grabbill.core.entity.AdminUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * @author michaellow
 **/
@Data
public class GrabbillAdminUserDetails implements UserDetails {

    private final AdminUser adminUser;


    public GrabbillAdminUserDetails(final AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String getPassword() {
        return adminUser.getPassword();
    }

    @Override
    public String getUsername() {
        return adminUser.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return adminUser.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return adminUser.isActive();
    }

    @Override
    public boolean isAccountNonExpired() {
        return adminUser.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return adminUser.isActive();
    }

}
