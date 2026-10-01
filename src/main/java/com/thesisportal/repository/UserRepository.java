package com.thesisportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thesisportal.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByStudentId(String studentId);
}
