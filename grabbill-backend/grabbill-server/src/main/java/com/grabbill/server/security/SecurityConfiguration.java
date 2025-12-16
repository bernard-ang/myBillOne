package com.grabbill.server.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * @author michaellow
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Qualifier("clientUserDetailsService")
    public UserDetailsService clientUserDetailsService() {
        return new GrabbillUserDetailsServiceImpl();
    }

    @Bean
    @Qualifier("adminUserDetailsService")
    public UserDetailsService adminUserDetailsService() {
        return new GrabbillAdminUserDetailsServiceImpl();
    }

    @Bean
    public TokenCookieServices cookieServices() {
        return new TokenCookieServicesImpl();
    }

    @Bean
    public TokenServices tokenServices() {
        return new TokenServicesImpl();
    }

    @Bean
    public TokenCryptoServices tokenCryptoServices() {
        return new TokenCryptoServicesImpl();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    @Bean
    public TokenExceptionHandlerFilter tokenExceptionHandlerFilter() {
        return new TokenExceptionHandlerFilter();
    }


    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        ClientUserAuthenticationProvider clientUserAuthenticationProvider = new ClientUserAuthenticationProvider();
        clientUserAuthenticationProvider.setUserDetailsService(clientUserDetailsService());
        clientUserAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(clientUserAuthenticationProvider);

        AdminUserAuthenticationProvider adminUserAuthenticationProvider = new AdminUserAuthenticationProvider();
        adminUserAuthenticationProvider.setUserDetailsService(adminUserDetailsService());
        adminUserAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(adminUserAuthenticationProvider);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .headers(header -> {
                    header.httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000));

                    header.contentSecurityPolicy(csp -> csp
                            .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'"));
                })
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint()).and()
                .authorizeRequests()

                .antMatchers("/", "/error")
                .permitAll()

                .antMatchers(
                        // auth related
                        "/auth/login",
                        "/auth/admin/login",
                        "/auth/authorize",
                        "/auth/2-factor-auth",
                        "/auth/admin/2-factor-auth",

                        // account registration related
                        "/account/register",
                        "/account/verify-email",
                        "/account/forget-password",
                        "/account/reset-password",
                        "/account/resend-verify-email",
                        "/account/generate-email-otp",
                        "/account/generate-email-2fa-activation-otp",

                        // email tracking related
                        "/unsubscribe",
                        "/track/trxemail/**/*.png",
                        "/track/mt-trxemail/**/*.png",
                        "/track/ecemail/**/*.png",
                        "/clicks/**/*",
                        "/ext/images/**/*",

                        // whatsapp related
                        "/ext/files/**/*",
                        "/ack/whatsapp",
                        "/webhook/whatsapp",

                        // account management related
                        "/mgmt/account/forget-password",
                        "/mgmt/account/reset-password",
                        "/mgmt/account/generate-email-otp",

                        // payment related
                        "/callback/stripe/sessions",
                        "/webhook/stripe/events",

                        // misc
                        "/plans",
                        "/build-info",

                        // WhatsApp sandbox
                        "/wa-sandbox/**/*"
                ).permitAll()

                .anyRequest()
                .authenticated();

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenExceptionHandlerFilter(), CorsFilter.class);
    }

}
