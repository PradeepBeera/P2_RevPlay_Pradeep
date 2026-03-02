package com.rev.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = exception.getMessage();
        String targetUrl = "/login?error=true"; // Default

        if ("mismatch_artist".equalsIgnoreCase(errorMessage)) {
            targetUrl = "/login?error=mismatch_artist";
        } else if ("mismatch_listener".equalsIgnoreCase(errorMessage)) {
            targetUrl = "/login?error=mismatch_listener";
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
