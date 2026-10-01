package com.thesisportal.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thesisportal.model.ThesisSubmission;
import com.thesisportal.repository.ThesisRepository;

@Controller
@RequestMapping("/reviewer")
public class ReviewerController {

    private static final Set<String> REVIEW_STATUSES = Set.of(
            "APPROVED", "CHANGES_REQUESTED", "REJECTED");

    private final ThesisRepository thesisRepository;

    public ReviewerController(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @GetMapping
    public String reviewerDashboard(
            @RequestParam(required = false) String error,
            Model model) {
        List<ThesisSubmission> submissions = thesisRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        model.addAttribute("submissions", submissions);
        model.addAttribute("error", error);
        return "reviewer-dashboard";
    }

    @PostMapping("/update-status")
    public String updateStatus(
            @RequestParam Long submissionId,
            @RequestParam String status,
            @RequestParam(required = false) String feedback) {

        if (!REVIEW_STATUSES.contains(status)) {
            return "redirect:/reviewer?error=Invalid+review+status.";
        }

        if ("CHANGES_REQUESTED".equals(status)
                && (feedback == null || feedback.isBlank())) {
            return "redirect:/reviewer?error=Feedback+is+required+when+requesting+changes.";
        }

        Optional<ThesisSubmission> optionalSubmission = thesisRepository.findById(submissionId);
        if (optionalSubmission.isPresent()) {
            ThesisSubmission submission = optionalSubmission.get();
            submission.setStatus(status);
            submission.setReviewerFeedback(feedback == null ? null : feedback.trim());
            thesisRepository.save(submission);
        }

        return "redirect:/reviewer";
    }
}
