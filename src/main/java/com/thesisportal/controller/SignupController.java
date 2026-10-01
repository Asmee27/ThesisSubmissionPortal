package com.thesisportal.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thesisportal.model.Role;
import com.thesisportal.model.User;
import com.thesisportal.repository.UserRepository;

@Controller
public class SignupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String reviewerRegistrationCode;

    public SignupController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${REVIEWER_REGISTRATION_CODE:}") String reviewerRegistrationCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reviewerRegistrationCode = reviewerRegistrationCode;
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String role,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String registrationCode,
            Model model) {

        String normalizedUsername = username.trim();
        String normalizedFullName = fullName.trim();
        String normalizedStudentId = studentId == null ? null : studentId.trim();
        String normalizedRole = role.trim().toUpperCase();

        if (normalizedFullName.isBlank() || normalizedUsername.isBlank()
                || password.isBlank() || confirmPassword.isBlank()) {
            return error(model, "Please complete all required fields.");
        }

        if (!password.equals(confirmPassword)) {
            return error(model, "Passwords do not match.");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            return error(model, "Username is already registered.");
        }

        Role accountRole;
        if ("STUDENT".equals(normalizedRole)) {
            if (normalizedStudentId == null || normalizedStudentId.isBlank()) {
                return error(model, "Student ID is required for student accounts.");
            }
            if (userRepository.existsByStudentId(normalizedStudentId)) {
                return error(model, "Student ID is already registered.");
            }
            accountRole = Role.ROLE_STUDENT;
        } else if ("REVIEWER".equals(normalizedRole)) {
            if (reviewerRegistrationCode.isBlank()
                    || registrationCode == null
                    || !sameSecret(registrationCode, reviewerRegistrationCode)) {
                return error(model, "Invalid reviewer registration code.");
            }
            normalizedStudentId = null;
            accountRole = Role.ROLE_REVIEWER;
        } else {
            return error(model, "Please select a valid account role.");
        }

        try {
            User user = new User(
                    normalizedUsername,
                    passwordEncoder.encode(password),
                    normalizedFullName,
                    normalizedStudentId,
                    accountRole);
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            return error(model, "Username or student ID is already registered.");
        }

        return "redirect:/login?signup=success";
    }

    private String error(Model model, String message) {
        model.addAttribute("error", message);
        return "signup";
    }

    private boolean sameSecret(String submitted, String configured) {
        return MessageDigest.isEqual(
                submitted.getBytes(StandardCharsets.UTF_8),
                configured.getBytes(StandardCharsets.UTF_8));
    }
}