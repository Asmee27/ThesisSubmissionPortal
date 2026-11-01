package com.thesisportal.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThesisSubmissionTest extends BaseSeleniumTest {

    @Test
    void studentShouldLoginSuccessfully() {

        loginAs("STUDENT", "student", "student123", "/student");

        assertTrue(
                driver.getCurrentUrl().contains("/student"),
                "Student should be redirected to the student dashboard."
        );
    }
}