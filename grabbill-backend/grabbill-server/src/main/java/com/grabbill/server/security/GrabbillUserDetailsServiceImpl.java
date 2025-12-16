package com.grabbill.server.security;

import com.grabbill.core.entity.User;
import com.grabbill.core.service.UserService;
import com.grabbill.server.dto.GrabbillUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author michaellow
 */
public class GrabbillUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;


    @Override
    public UserDetails loadUserByUsername(
            final String username
    ) throws UsernameNotFoundException {
        User user = userService.getByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User [" + username + "] is not found!")
        );

        return new GrabbillUserDetails(user);
    }

}
