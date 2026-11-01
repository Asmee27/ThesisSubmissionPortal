package com.thesisportal.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatusTrackingTest extends BaseSeleniumTest {

    @Test
    void studentShouldSeeStatusTrackingDashboard() {

        loginAs("STUDENT", "student", "student123", "/student");

        String page = driver.getPageSource();

        assertTrue(
                page.contains("My Thesis Submissions"),
                "Student dashboard should be displayed."
        );

       assertTrue(
        page.contains("Track your research submissions, faculty"),
        "Status tracking information should be displayed."
       );

        assertTrue(
                page.contains("Submitted"),
                "Submitted stage should be visible."
        );

        assertTrue(
                page.contains("Review"),
                "Review stage should be visible."
        );

        assertTrue(
                page.contains("Decision"),
                "Decision stage should be visible."
        );
    }
}