package com.rev.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class CustomWebAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    @Override
    public CustomWebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new CustomWebAuthenticationDetails(context);
    }
}
