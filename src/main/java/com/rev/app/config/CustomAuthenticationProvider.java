package com.rev.app.config;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super();
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);

        Object details = authentication.getDetails();
        if (details instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails customDetails = (CustomWebAuthenticationDetails) details;
            String intendedRole = customDetails.getIntendedRole();

            if (intendedRole != null) {
                boolean hasRole = userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + intendedRole.toUpperCase()));

                if (!hasRole) {
                    if ("ARTIST".equalsIgnoreCase(intendedRole)) {
                        throw new BadCredentialsException("mismatch_artist");
                    } else if ("LISTENER".equalsIgnoreCase(intendedRole)) {
                        throw new BadCredentialsException("mismatch_listener");
                    }
                }
            }
        }
    }
}
