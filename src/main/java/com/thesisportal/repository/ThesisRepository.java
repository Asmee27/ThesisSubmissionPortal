package com.thesisportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thesisportal.model.ThesisSubmission;

public interface ThesisRepository
        extends JpaRepository<ThesisSubmission, Long> {

    List<ThesisSubmission> findByStudentIdOrderBySubmittedAtDesc(String studentId);
}