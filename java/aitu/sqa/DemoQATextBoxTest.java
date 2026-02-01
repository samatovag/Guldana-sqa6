package aitu.sqa;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


import java.time.Duration;

public class DemoQATextBoxTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String EXCEL_RESOURCE = "testdata/testdata.xlsx";

    @BeforeMethod
    @Parameters({"runOn", "browser", "browserVersion", "os", "osVersion"})
    public void setup(@Optional("LOCAL") String runOn,
                      @Optional("chrome") String browser,
                      @Optional("latest") String browserVersion,
                      @Optional("Windows") String os,
                      @Optional("11") String osVersion) throws Exception {

        if ("BROWSERSTACK".equalsIgnoreCase(runOn)) {

            String username = System.getenv("BROWSERSTACK_USERNAME");
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

            if (username == null || accessKey == null) {
                throw new RuntimeException("BrowserStack env vars missing. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY and restart IntelliJ.");
            }

            Map<String, Object> bsOptions = new HashMap<>();
            bsOptions.put("os", os);
            bsOptions.put("osVersion", osVersion);
            bsOptions.put("sessionName", "DemoQA TextBox DDT");
            bsOptions.put("buildName", "SQA Assignment 6 - Guldana");

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("browserName", browser);
            caps.setCapability("browserVersion", browserVersion);
            caps.setCapability("bstack:options", bsOptions);

            String hubUrl = "https://" + username + ":" + accessKey + "@hub.browserstack.com/wd/hub";
            driver = new RemoteWebDriver(new URL(hubUrl), caps);

        } else {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }


    @DataProvider(name = "textBoxData")
    public Object[][] textBoxData() {
        return ExcelUtils.readSheetFromResources(EXCEL_RESOURCE, "TextBox");
    }


    @Test(dataProvider = "textBoxData")
    public void testTextBoxForm(String testName,
                                String fullName,
                                String email,
                                String currentAddress,
                                String permanentAddress,
                                String expected) {

        System.out.println("Running dataset: " + testName);

        driver.get("https://demoqa.com/text-box");

        driver.findElement(By.id("userName")).clear();
        driver.findElement(By.id("userName")).sendKeys(fullName);

        driver.findElement(By.id("userEmail")).clear();
        driver.findElement(By.id("userEmail")).sendKeys(email);

        driver.findElement(By.id("currentAddress")).clear();
        driver.findElement(By.id("currentAddress")).sendKeys(currentAddress);

        driver.findElement(By.id("permanentAddress")).clear();
        driver.findElement(By.id("permanentAddress")).sendKeys(permanentAddress);

        WebElement submitBtn = driver.findElement(By.id("submit"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitBtn);

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

        if ("SUCCESS".equalsIgnoreCase(expected)) {
            WebElement output = driver.findElement(By.id("output"));
            Assert.assertTrue(output.isDisplayed(), "Output should be displayed for SUCCESS");

            String nameText = driver.findElement(By.id("name")).getText();
            Assert.assertTrue(nameText.contains(fullName), "Output name should contain fullName");

            String emailText = driver.findElement(By.id("email")).getText();
            Assert.assertTrue(emailText.contains(email), "Output email should contain email");

            System.out.println(testName + " -> PASSED");

        } else if ("INVALID_EMAIL".equalsIgnoreCase(expected)) {

            WebElement emailField = driver.findElement(By.id("userEmail"));

            String classes = emailField.getAttribute("class");

            String ariaInvalid = emailField.getAttribute("aria-invalid");

            Assert.assertTrue(
                    (classes != null && classes.contains("field-error")) ||
                            ("true".equalsIgnoreCase(ariaInvalid)),
                    "Email should be marked invalid. class=" + classes + ", aria-invalid=" + ariaInvalid
            );

            System.out.println(testName + " -> PASSED (invalid email correctly detected)");
        }
        else {
            Assert.fail("Unknown expected value in Excel: " + expected);
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {

        if (!result.isSuccess()) {
            System.out.println("FAILED: " + result.getName() + " Reason: " + result.getThrowable());
        }
        if (driver != null) driver.quit();
    }

}


