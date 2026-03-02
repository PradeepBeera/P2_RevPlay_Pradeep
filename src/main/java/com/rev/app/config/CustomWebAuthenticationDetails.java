package com.rev.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String intendedRole;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.intendedRole = request.getParameter("intendedRole");
    }

    public String getIntendedRole() {
        return intendedRole;
    }
}
