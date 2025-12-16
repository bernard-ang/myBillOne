package com.grabbill.server.security;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.service.AdminUserService;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author michaellow
 */
public class GrabbillAdminUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminUserService adminUserService;


    @Override
    public UserDetails loadUserByUsername(
            final String username
    ) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserService.getByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Admin user [" + username + "] is not found!")
        );

        return new GrabbillAdminUserDetails(adminUser);
    }

}
