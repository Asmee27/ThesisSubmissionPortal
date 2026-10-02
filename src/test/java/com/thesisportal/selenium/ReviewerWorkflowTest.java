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

class ReviewerWorkflowTest {

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
    void reviewerShouldLoginAndOpenDashboard() {

        driver.get("http://localhost:8083/login");

        WebElement reviewerRole = driver.findElement(
                By.cssSelector(
                        "input[name='role'][value='REVIEWER'] + .role-card"
                )
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", reviewerRole);

        driver.findElement(By.id("username"))
                .sendKeys("reviewer");

        driver.findElement(By.id("password"))
                .sendKeys("reviewer123");

        WebElement signInButton = driver.findElement(
                By.cssSelector("button[type='submit']")
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", signInButton);

        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains("/reviewer")
        );

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