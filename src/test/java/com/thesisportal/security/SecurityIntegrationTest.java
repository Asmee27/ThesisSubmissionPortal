package com.thesisportal.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.thesisportal.model.Role;
import com.thesisportal.model.ThesisSubmission;
import com.thesisportal.model.User;
import com.thesisportal.repository.ThesisRepository;
import com.thesisportal.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThesisRepository thesisRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        thesisRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User("student", passwordEncoder.encode("student123"),
                "Alex Johnson", "S001", Role.ROLE_STUDENT));
        userRepository.save(new User("reviewer", passwordEncoder.encode("reviewer123"),
                "Dr. Robert Smith", null, Role.ROLE_REVIEWER));
    }


    // =====================================================
    // UNAUTHENTICATED ACCESS
    // =====================================================

    @Test
    public void testUnauthenticatedUserRedirectedToLoginForProtectedRoutes()
            throws Exception {

        mockMvc.perform(get("/student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(get("/reviewer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    // =====================================================
    // PUBLIC ROUTES
    // =====================================================

    @Test
    public void testPublicRoutesAccessibleWithoutAuth()
            throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());
    }


    // =====================================================
    // STUDENT LOGIN
    // =====================================================

    @Test
    public void testStudentLoginRedirectsToStudentDashboard()
            throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "student")
                        .param("password", "student123")
                        .param("role", "STUDENT")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }


    // =====================================================
    // REVIEWER LOGIN
    // =====================================================

    @Test
    public void testReviewerLoginRedirectsToReviewerDashboard()
            throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "reviewer")
                        .param("password", "reviewer123")
                        .param("role", "REVIEWER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviewer"));
    }


    // =====================================================
    // ROLE MISMATCH TESTS
    // =====================================================

    @Test
    public void testStudentCannotLoginAsReviewer()
            throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "student")
                        .param("password", "student123")
                        .param("role", "REVIEWER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?roleError=true"));
    }


    @Test
    public void testReviewerCannotLoginAsStudent()
            throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "reviewer")
                        .param("password", "reviewer123")
                        .param("role", "STUDENT")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?roleError=true"));
    }

    @Test
    public void testStudentSignupStoresBcryptPasswordAndRedirects()
            throws Exception {

        mockMvc.perform(post("/signup")
                        .param("fullName", "New Student")
                        .param("username", "newstudent")
                        .param("password", "new-password")
                        .param("confirmPassword", "new-password")
                        .param("role", "STUDENT")
                        .param("studentId", "S002")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?signup=success"));

        User user = userRepository.findByUsername("newstudent").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(Role.ROLE_STUDENT, user.getRole());
        org.junit.jupiter.api.Assertions.assertTrue(user.getPassword().startsWith("$2"));
        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches("new-password", user.getPassword()));
    }

    @Test
    public void testReviewerSignupRequiresRegistrationCode()
            throws Exception {

        mockMvc.perform(post("/signup")
                        .param("fullName", "New Reviewer")
                        .param("username", "newreviewer")
                        .param("password", "reviewer-password")
                        .param("confirmPassword", "reviewer-password")
                        .param("role", "REVIEWER")
                        .param("registrationCode", "wrong-code")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(content().string(containsString("Invalid reviewer registration code")));

        mockMvc.perform(post("/signup")
                        .param("fullName", "New Reviewer")
                        .param("username", "newreviewer")
                        .param("password", "reviewer-password")
                        .param("confirmPassword", "reviewer-password")
                        .param("role", "REVIEWER")
                        .param("registrationCode", "test-reviewer-code")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        org.junit.jupiter.api.Assertions.assertEquals(
                Role.ROLE_REVIEWER,
                userRepository.findByUsername("newreviewer").orElseThrow().getRole());
    }

    @Test
    public void testDuplicateUsernameAndStudentIdAreRejected()
            throws Exception {

        mockMvc.perform(post("/signup")
                        .param("fullName", "Duplicate Username")
                        .param("username", "student")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("role", "STUDENT")
                        .param("studentId", "S002")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));

        mockMvc.perform(post("/signup")
                        .param("fullName", "Duplicate ID")
                        .param("username", "another-student")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("role", "STUDENT")
                        .param("studentId", "S001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }


    // =====================================================
    // STUDENT AUTHORIZATION
    // =====================================================

    @Test
    @WithMockUser(
            username = "student",
            authorities = {"ROLE_STUDENT"}
    )
    public void testStudentCanAccessStudentDashboard()
            throws Exception {

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(
            username = "student",
            authorities = {"ROLE_STUDENT"}
    )
    public void testStudentCannotAccessReviewerDashboard()
            throws Exception {

        mockMvc.perform(get("/reviewer"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }


    // =====================================================
    // REVIEWER AUTHORIZATION
    // =====================================================

    @Test
    @WithMockUser(
            username = "reviewer",
            authorities = {"ROLE_REVIEWER"}
    )
    public void testReviewerCanAccessReviewerDashboard()
            throws Exception {

        mockMvc.perform(get("/reviewer"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(
            username = "reviewer",
            authorities = {"ROLE_REVIEWER"}
    )
    public void testReviewerCannotAccessStudentDashboard()
            throws Exception {

        mockMvc.perform(get("/student"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

        @Test
        @WithMockUser(username = "student", authorities = {"ROLE_STUDENT"})
        public void testStudentSeesOnlyOwnSubmissions()
                        throws Exception {

                ThesisSubmission own = submission("S001", "Own Thesis");
                ThesisSubmission other = submission("S999", "Other Thesis");
                thesisRepository.save(own);
                thesisRepository.save(other);

                mockMvc.perform(get("/student"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Own Thesis")))
                                .andExpect(content().string(not(containsString("Other Thesis"))));
        }

                    @Test
                    @WithMockUser(username = "student", authorities = {"ROLE_STUDENT"})
                    public void testStudentCanViewOnlyOwnPdf()
                            throws Exception {

                        Path uploadDirectory = Paths.get("uploads");
                        Files.createDirectories(uploadDirectory);
                        Files.write(uploadDirectory.resolve("test.pdf"), "PDF".getBytes());

                        ThesisSubmission own = submission("S001", "Own Thesis");
                        ThesisSubmission other = submission("S999", "Other Thesis");
                        thesisRepository.save(own);
                        thesisRepository.save(other);

                        mockMvc.perform(get("/documents/" + own.getId()))
                                .andExpect(status().isOk());

                        mockMvc.perform(get("/documents/" + other.getId()))
                                .andExpect(status().isNotFound());

                        Files.deleteIfExists(uploadDirectory.resolve("test.pdf"));
                    }

        private ThesisSubmission submission(String studentId, String title) {
                ThesisSubmission submission = new ThesisSubmission();
                submission.setStudentId(studentId);
                submission.setStudentName("Student");
                submission.setTitle(title);
                submission.setDepartment("Computer Engineering");
                submission.setGuideName("Guide");
                submission.setAbstractText("Abstract");
                submission.setFileName("test.pdf");
                return submission;
        }
}