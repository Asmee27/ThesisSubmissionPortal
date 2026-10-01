package com.thesisportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.thesisportal.model.ThesisSubmission;
import com.thesisportal.model.User;
import com.thesisportal.repository.ThesisRepository;
import com.thesisportal.repository.UserRepository;

@Controller
public class ThesisController {

    private static final String UPLOAD_DIR = "uploads";
    private final ThesisRepository thesisRepository;
    private final UserRepository userRepository;

    public ThesisController(ThesisRepository thesisRepository, UserRepository userRepository) {
        this.thesisRepository = thesisRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/submit")
    public String showSubmissionForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> {
                model.addAttribute("defaultStudentName", user.getFullName());
                model.addAttribute("defaultStudentId", user.getStudentId() != null ? user.getStudentId() : username);
            });
        }
        return "submit-thesis";
    }

    @GetMapping("/student")
    public String studentDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        String studentId = user.getStudentId();

        model.addAttribute(
                "submissions",
                thesisRepository.findByStudentIdOrderBySubmittedAtDesc(studentId)
        );

        model.addAttribute("studentId", studentId);

        return "student-dashboard";
    }

    @GetMapping("/documents/{submissionId}")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long submissionId,
            Authentication authentication) {

        Optional<ThesisSubmission> submissionOpt = thesisRepository.findById(submissionId);
        if (submissionOpt.isEmpty()
                || !canAccessDocument(submissionOpt.get(), authentication)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Path uploadRoot = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Path documentPath = uploadRoot.resolve(submissionOpt.get().getFileName()).normalize();
            if (!documentPath.startsWith(uploadRoot) || !Files.isRegularFile(documentPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource document = new UrlResource(documentPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline()
                                    .filename(submissionOpt.get().getFileName())
                                    .build()
                                    .toString())
                    .body(document);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean canAccessDocument(
            ThesisSubmission submission,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isReviewer = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_REVIEWER".equals(authority.getAuthority()));
        if (isReviewer) {
            return true;
        }

        return userRepository.findByUsername(authentication.getName())
                .map(user -> submission.getStudentId().equals(
                        user.getStudentId() != null ? user.getStudentId() : user.getUsername()))
                .orElse(false);
    }

    @PostMapping("/submit")
    public String submitThesis(
            @RequestParam String studentName,
            @RequestParam String studentId,
            @RequestParam String title,
            @RequestParam String department,
            @RequestParam String guideName,
            @RequestParam String abstractText,
            @RequestParam("thesisFile") MultipartFile thesisFile,
            Authentication authentication,
            Model model) {

        try {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            studentName = user.getFullName();
            studentId = user.getStudentId();

            // Basic validation
            if (studentName.isBlank()
                    || studentId.isBlank()
                    || title.isBlank()
                    || department.isBlank()
                    || guideName.isBlank()
                    || abstractText.isBlank()) {

                model.addAttribute(
                        "error",
                        "Please complete all required fields."
                );

                return "submit-thesis";
            }

            // File validation
            if (thesisFile.isEmpty()) {

                model.addAttribute(
                        "error",
                        "Please select a thesis PDF."
                );

                return "submit-thesis";
            }

            String originalFileName = thesisFile.getOriginalFilename();

            if (originalFileName == null
                || !originalFileName.toLowerCase().endsWith(".pdf")
                || !"application/pdf".equalsIgnoreCase(thesisFile.getContentType())) {

            model.addAttribute(
                    "error",
                    "Only valid PDF files are allowed."
            );

            return "submit-thesis";
}

            // 10 MB maximum
            long maxFileSize = 10 * 1024 * 1024;

            if (thesisFile.getSize() > maxFileSize) {

                model.addAttribute(
                        "error",
                        "PDF size must not exceed 10 MB."
                );

                return "submit-thesis";
            }

            // Create upload directory
            Path uploadPath = Paths.get(UPLOAD_DIR);

            Files.createDirectories(uploadPath);

            // Generate unique stored filename
            String storedFileName =
                    UUID.randomUUID() + "_" + originalFileName;

            Path destination =
                    uploadPath.resolve(storedFileName);

            Files.copy(
                    thesisFile.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Create submission object
            ThesisSubmission submission = new ThesisSubmission();

            submission.setStudentName(studentName);
            submission.setStudentId(studentId);
            submission.setTitle(title);
            submission.setDepartment(department);
            submission.setGuideName(guideName);
            submission.setAbstractText(abstractText);
            submission.setFileName(storedFileName);
            submission.setStatus("SUBMITTED");

            // Save submission metadata in MySQL
            thesisRepository.save(submission);

            model.addAttribute("submission", submission);
            return "submission-success";

        } catch (IOException e) {

            model.addAttribute(
                    "error",
                    "Unable to upload the thesis document."
                );

            return "submit-thesis";
        }
    }
}