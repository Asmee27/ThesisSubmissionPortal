package com.thesisportal.selenium;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

class StatusTrackingTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void studentShouldSeeStatusTrackingDashboard() {

        driver.get("http://localhost:8083/login");

        WebElement studentRole = driver.findElement(
                By.cssSelector(
                        "input[name='role'][value='STUDENT'] + .role-card"
                )
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", studentRole);

        driver.findElement(By.id("username")).sendKeys("student");
        driver.findElement(By.id("password")).sendKeys("student123");

        WebElement signInButton = driver.findElement(
                By.cssSelector("button[type='submit']")
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", signInButton);

        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains("/student")
        );

        assertTrue(
                driver.getPageSource().contains("My Thesis Submissions"),
                "Student dashboard should be displayed."
        );

        assertTrue(
                driver.getPageSource().contains("Track your thesis submission and review status."),
                "Status tracking information should be displayed."
        );

        assertTrue(
                driver.getPageSource().contains("Submitted"),
                "Submitted stage should be visible."
        );

        assertTrue(
                driver.getPageSource().contains("Review"),
                "Review stage should be visible."
        );

        assertTrue(
                driver.getPageSource().contains("Decision"),
                "Decision stage should be visible."
        );
    }
}
