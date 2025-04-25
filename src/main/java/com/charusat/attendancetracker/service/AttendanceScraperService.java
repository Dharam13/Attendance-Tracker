package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceScraperService {

    private final AttendanceService attendanceService;

    @Value("${selenium.headless:true}")
    private boolean headless;

    public void scrapeAttendanceForUser(User user) {
        WebDriver driver = null;
        try {
            log.info("Starting attendance scraping for user: {}", user.getEgovId());

            // Setup WebDriver
            driver = setupWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Login to CHARUSAT e-Governance
            login(driver, wait, user);

            // Navigate to attendance page
            navigateToAttendancePage(driver, wait);

            // Extract attendance data
            Map<String, Subject> subjects = extractAttendanceData(driver, user);

            // Save subjects and attendance records
            saveAttendanceData(subjects, user);

            log.info("Finished attendance scraping for user: {}", user.getEgovId());
        } catch (Exception e) {
            log.error("Error during attendance scraping for user {}: {}", user.getEgovId(), e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private WebDriver setupWebDriver() {
        log.info("Setting up WebDriver for containerized environment");

        // Configure Chrome options for containerized environment
        ChromeOptions options = new ChromeOptions();

        // Essential options for running Chrome in a container
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        // For the selenium/standalone-chrome image
        options.addArguments("--disable-extensions");

        // Use RemoteWebDriver to connect to the Chrome instance
        try {
            return new ChromeDriver(options);
        } catch (Exception e) {
            log.error("Error creating ChromeDriver: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void login(WebDriver driver, WebDriverWait wait, User user) {
        log.info("Logging in to e-Governance portal");
        driver.get("https://charusat.edu.in:912/eGovernance/");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"txtUserName\"]")));
        typeSlowly(usernameField, user.getEgovId());

        WebElement passwordField = driver.findElement(By.xpath("//*[@id=\"txtPassword\"]"));
        typeSlowly(passwordField, user.getEgovPassword());

        WebElement loginButton = driver.findElement(By.id("btnLogin"));
        loginButton.click();

        // Wait for the page to load after login
        wait.until(ExpectedConditions.urlContains("frmAppSelection.aspx"));
    }

    private void navigateToAttendancePage(WebDriver driver, WebDriverWait wait) {
        log.info("Navigating to attendance page");
        WebElement lectureGrossAttendance = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"grdGrossAtt_ctl01_lnkRequestViewTT\"]")));
        lectureGrossAttendance.click();

        // Wait for the attendance table to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("gvGrossAttPop")));

        // Click the more button to see detailed attendance (if needed)
        try {
            WebElement moreButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"footAnnouncement\"]")));
            moreButton.click();

            // Wait for detailed view to load
            slowDown(3000);
        } catch (Exception e) {
            log.warn("More button not found or not clickable: {}", e.getMessage());
        }
    }

    private Map<String, Subject> extractAttendanceData(WebDriver driver, User user) {
        log.info("Extracting attendance data");
        Map<String, Subject> subjects = new HashMap<>();

        try {
            // Find the table that contains attendance data
            WebElement attendanceTable = driver.findElement(By.id("gvGrossAttPop"));
            List<WebElement> rows = attendanceTable.findElements(By.tagName("tr"));

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                try {
                    WebElement row = rows.get(i);
                    List<WebElement> cells = row.findElements(By.tagName("td"));

                    if (cells.size() >= 4) {
                        String courseWithCode = cells.get(0).getText().trim(); // Format: "CE262 / DCN"
                        String classType = cells.get(1).getText().trim();      // Format: "LECT" or "LAB"
                        String presentTotal = cells.get(2).getText().trim();   // Format: "41/52"
                        String percentageText = cells.get(3).getText().trim(); // Format: "78%"

                        // Parse course code and name
                        String[] courseInfo = parseSubjectInfo(courseWithCode);
                        String subjectCode = courseInfo[0]; // "CE262"
                        String subjectName = courseInfo[1]; // "DCN"

                        // Parse attendance percentage directly from the percentage column
                        double attendancePercentage = parseAttendancePercentage(percentageText);

                        // Parse total and attended classes from the Present/Total column
                        int[] classCounts = parseClassCountsFromFraction(presentTotal);
                        int attendedClasses = classCounts[0];
                        int totalClasses = classCounts[1];

                        // Create subject key combining code and type (e.g., "CE262_LECT")
                        String subjectKey = subjectCode + "_" + classType;

                        // Create or update subject
                        Subject subject = new Subject();
                        subject.setCode(subjectCode);
                        subject.setName(subjectName);
                        subject.setCurrentAttendance(attendancePercentage);
                        subject.setUser(user);
                        subject.setClassType(classType);  // Set the class type
                        subject.setAttendedClasses(attendedClasses);
                        subject.setTotalClasses(totalClasses);

                        subjects.put(subjectKey, subject);

                        log.info("Extracted subject: {} ({}) with attendance: {}% ({}/{})",
                                subjectName, classType, attendancePercentage, attendedClasses, totalClasses);
                    }
                } catch (Exception e) {
                    log.warn("Error processing row {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error extracting attendance data: {}", e.getMessage(), e);
        }

        return subjects;
    }

    private void saveAttendanceData(Map<String, Subject> subjects, User user) {
        log.info("Saving attendance data for {} subjects", subjects.size());

        for (Map.Entry<String, Subject> entry : subjects.entrySet()) {
            Subject subject = entry.getValue();

            // Save or update the subject
            Subject savedSubject = attendanceService.saveOrUpdateSubject(subject);

            // Create attendance record
            AttendanceRecord record = new AttendanceRecord();
            record.setUser(user);
            record.setSubject(savedSubject);
            record.setAttendancePercentage(savedSubject.getCurrentAttendance());
            record.setRecordedAt(LocalDateTime.now());
            record.setAttendedClasses(subject.getAttendedClasses());
            record.setTotalClasses(subject.getTotalClasses());

            // Save the attendance record
            attendanceService.saveAttendanceRecord(record);
            log.info("Saved attendance record for subject: {} ({})",
                    savedSubject.getName(), savedSubject.getClassType());
        }
    }

    /**
     * Parse the subject code and name from the combined text
     * @param combinedText format like "CE262 / DCN"
     * @return String array where [0] is code and [1] is name
     */
    private String[] parseSubjectInfo(String combinedText) {
        String[] result = new String[2];
        result[0] = ""; // Default code
        result[1] = ""; // Default name

        try {
            if (combinedText.contains("/")) {
                String[] parts = combinedText.split("/", 2);
                result[0] = parts[0].trim(); // "CE262"
                result[1] = parts[1].trim(); // "DCN"
            } else {
                // If there's no slash, use the whole text as both code and name
                result[0] = combinedText.trim();
                result[1] = combinedText.trim();
            }
        } catch (Exception e) {
            log.warn("Failed to parse subject info from text: {}", combinedText);
        }

        return result;
    }

    /**
     * Parse attendance percentage from text
     * @param percentageText format like "78%"
     * @return the percentage as a double value
     */
    private double parseAttendancePercentage(String percentageText) {
        try {
            // Extract the percentage value from text like "78%"
            return Double.parseDouble(percentageText.replace("%", "").trim());
        } catch (Exception e) {
            log.warn("Failed to parse attendance percentage from text: {}", percentageText);
            return 0.0;
        }
    }

    /**
     * Parse the class counts from a fraction text
     * @param fractionText format like "41/52"
     * @return int array where [0] is attended classes and [1] is total classes
     */
    private int[] parseClassCountsFromFraction(String fractionText) {
        int[] result = new int[2]; // [attendedClasses, totalClasses]

        try {
            // Parse from format like "41/52"
            String[] parts = fractionText.split("/");
            if (parts.length == 2) {
                result[0] = Integer.parseInt(parts[0].trim()); // Attended classes
                result[1] = Integer.parseInt(parts[1].trim()); // Total classes
            }
        } catch (Exception e) {
            log.warn("Failed to parse class counts from text: {}", fractionText);
        }

        return result;
    }

    private void typeSlowly(WebElement element, String text) {
        for (char c : text.toCharArray()) {
            element.sendKeys(Character.toString(c));
            slowDown(150); // Delay between each character
        }
    }

    private void slowDown(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}