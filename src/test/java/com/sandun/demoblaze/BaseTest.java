package com.sandun.demoblaze;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL = "https://demoblaze.com";
    protected static final String PRODUCT_SAMSUNG = "Samsung galaxy s6";
    protected static final String PRODUCT_NOKIA = "Nokia lumia 1520";

    protected static final String CHECKOUT_NAME = "Test Student";
    protected static final String CHECKOUT_COUNTRY = "Sri Lanka";
    protected static final String CHECKOUT_CITY = "Colombo";
    protected static final String CHECKOUT_CARD = "4111111111111111";
    protected static final String CHECKOUT_MONTH = "12";
    protected static final String CHECKOUT_YEAR = "2027";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(BASE_URL);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null) {
            String status = result.getStatus() == ITestResult.SUCCESS ? "PASS" : "FAIL";
            takeScreenshot(result.getName(), status);

            if (result.getStatus() == ITestResult.FAILURE) {
                dumpPageSource(result.getName());
            }
            driver.quit();
        }
    }

    private void takeScreenshot(String testName, String status) {
        try {
            Files.createDirectories(Paths.get("screenshots"));
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            String fileName = "screenshots/" + testName + "-" + status + "-" + System.currentTimeMillis() + ".png";
            Files.copy(src.toPath(), Paths.get(fileName));
            System.out.println("Saved screenshot: " + fileName);
        } catch (IOException | ClassCastException e) {
            System.out.println("Could not capture screenshot: " + e.getMessage());
        }
    }

    private void dumpPageSource(String testName) {
        try {
            Files.createDirectories(Paths.get("target/failure-pagesource"));
            String fileName = "target/failure-pagesource/" + testName + "-" + System.currentTimeMillis() + ".html";
            Files.write(Paths.get(fileName), driver.getPageSource().getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved failure page source: " + fileName);
            System.out.println("Current URL at failure: " + driver.getCurrentUrl());
        } catch (IOException e) {
            System.out.println("Could not capture failure page source: " + e.getMessage());
        }
    }
}
