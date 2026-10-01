package com.thesisportal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "signup", required = false) String signup,
            @RequestParam(value = "roleError", required = false) String roleError,
            Model model) {

        // If already logged in, redirect according to role
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            for (GrantedAuthority authority : auth.getAuthorities()) {

                if ("ROLE_STUDENT".equals(authority.getAuthority())) {
                    return "redirect:/student";
                }

                if ("ROLE_REVIEWER".equals(authority.getAuthority())) {
                    return "redirect:/reviewer";
                }
            }
        }

        // Wrong username/password
        if (error != null) {
            model.addAttribute(
                    "error",
                    "Invalid username or password. Please try again."
            );
        }

        // Selected role doesn't match database role
        if (roleError != null) {
            model.addAttribute(
                    "error",
                    "Selected role does not match this account."
            );
        }

        // Successful logout
        if (logout != null) {
            model.addAttribute(
                    "message",
                    "You have been logged out successfully."
            );
        }

        if ("success".equals(signup)) {
            model.addAttribute("message", "Account created successfully. You can now sign in.");
        }

        return "login";
    }


    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}