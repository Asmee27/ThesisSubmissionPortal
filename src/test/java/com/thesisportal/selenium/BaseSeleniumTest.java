package com.thesisportal.selenium;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.thesisportal.model.Role;
import com.thesisportal.model.User;
import com.thesisportal.model.ThesisSubmission;
import com.thesisportal.repository.UserRepository;
import com.thesisportal.repository.ThesisRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("selenium")
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThesisRepository thesisRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void setUpTestEnvironment() {

        thesisRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User(
                "student",
                passwordEncoder.encode("student123"),
                "Alex Johnson",
                "S001",
                Role.ROLE_STUDENT
        ));

        userRepository.save(new User(
                "reviewer",
                passwordEncoder.encode("reviewer123"),
                "Dr. Robert Smith",
                null,
                Role.ROLE_REVIEWER
        ));

        ThesisSubmission submission = new ThesisSubmission();
        submission.setStudentName("Alex Johnson");
        submission.setStudentId("S001");
        submission.setTitle("Selenium Test Thesis");
        submission.setDepartment("Computer Engineering");
        submission.setGuideName("Dr. Test Guide");
        submission.setAbstractText("Sample thesis created for automated testing.");
        submission.setFileName("selenium-test.pdf");
        submission.setStatus("SUBMITTED");

        thesisRepository.save(submission);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected void loginAs(String role, String username, String password,
                           String dashboardPath) {

        driver.get(baseUrl() + "/login");

        WebElement roleCard = driver.findElement(
                By.cssSelector(
                        "input[name='role'][value='" + role + "'] + .role-card"
                )
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", roleCard);

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);

        WebElement signInButton = driver.findElement(
                By.cssSelector("button[type='submit']")
        );

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", signInButton);

        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains(dashboardPath)
        );
    }

    @AfterEach
    void tearDownTestEnvironment() {
        if (driver != null) {
            driver.quit();
        }
    }
}