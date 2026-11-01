package com.thesisportal.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReviewerWorkflowTest extends BaseSeleniumTest {

    @Test
    void reviewerShouldLoginAndOpenDashboard() {

        loginAs("REVIEWER", "reviewer", "reviewer123", "/reviewer");

        assertTrue(
                driver.getCurrentUrl().contains("/reviewer"),
                "Reviewer should be redirected to reviewer dashboard."
        );

        assertTrue(
                driver.getPageSource().contains("Thesis Review Dashboard"),
                "Reviewer dashboard should be displayed."
        );
    }
}