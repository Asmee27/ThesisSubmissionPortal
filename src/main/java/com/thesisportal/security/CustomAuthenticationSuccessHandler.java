package com.thesisportal.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Role selected on login page
        String selectedRole = request.getParameter("role");

        if (selectedRole == null || selectedRole.isBlank()) {
            SecurityContextHolder.clearContext();

            request.getSession().invalidate();

            response.sendRedirect("/login?roleError=true");
            return;
        }

        // Convert UI value to Spring Security authority
        String requiredAuthority = "ROLE_" + selectedRole.toUpperCase();

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        boolean roleMatches = authorities.stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(requiredAuthority));

        // Selected role does not match role stored in MySQL
        if (!roleMatches) {

            SecurityContextHolder.clearContext();

            request.getSession().invalidate();

            response.sendRedirect("/login?roleError=true");
            return;
        }

        // Correct role → redirect to correct dashboard
        if ("ROLE_STUDENT".equals(requiredAuthority)) {

            response.sendRedirect("/student");
            return;
        }

        if ("ROLE_REVIEWER".equals(requiredAuthority)) {

            response.sendRedirect("/reviewer");
            return;
        }

        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        response.sendRedirect("/login?roleError=true");
    }
}