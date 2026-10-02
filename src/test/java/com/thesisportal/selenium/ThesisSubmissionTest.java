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

class ThesisSubmissionTest {

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
    void studentShouldLoginSuccessfully() {

        driver.get("http://localhost:8083/login");

        // Select visible Student role card
        WebElement studentRole = driver.findElement(
                By.cssSelector(
                        "input[name='role'][value='STUDENT'] + .role-card"
                )
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", studentRole);

        // Enter credentials
        driver.findElement(By.id("username"))
                .sendKeys("student");

        driver.findElement(By.id("password"))
                .sendKeys("student123");

        // Submit the form using JavaScript to avoid UI overlay issues
        WebElement signInButton = driver.findElement(
                By.cssSelector("button[type='submit']")
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", signInButton);

        // Wait for successful redirect
        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains("/student")
        );

        assertTrue(
                driver.getCurrentUrl().contains("/wrong-dashboard"),
                "Student should be redirected to the student dashboard."
        );
    }
}