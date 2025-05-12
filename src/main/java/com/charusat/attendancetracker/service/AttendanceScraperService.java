package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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

    @Value("${selenium.timeout:45}")
    private int timeoutSeconds;

    public void scrapeAttendanceForUser(User user) {
        WebDriver driver = null;
        try {
            log.info("Starting attendance scraping for user: {}", user.getEgovId());

            // Setup WebDriver
            driver = setupWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

            // Login to CHARUSAT e-Governance
            login(driver, wait, user);

            // Navigate to attendance page
            navigateToAttendancePage(driver, wait);

            // Extract attendance data
            Map<String, Subject> subjects = extractAttendanceData(driver, user);

            // Check if any subjects were found
            if (subjects.isEmpty()) {
                log.warn("No subjects found during scraping for user {}", user.getEgovId());
            } else {
                log.info("Found {} subjects during scraping for user {}", subjects.size(), user.getEgovId());

                // Save subjects and attendance records
                saveAttendanceData(subjects, user);
            }

            log.info("Finished attendance scraping for user: {}", user.getEgovId());
        } catch (Exception e) {
            log.error("Error during attendance scraping for user {}: {}", user.getEgovId(), e.getMessage(), e);

            // Take screenshot on error if possible
            if (driver != null) {
                try {
                    File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    log.info("Screenshot saved at: {}", screenshotFile.getAbsolutePath());
                } catch (Exception se) {
                    log.warn("Failed to take error screenshot: {}", se.getMessage());
                }
            }
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    log.info("WebDriver successfully closed");
                } catch (Exception e) {
                    log.warn("Error closing WebDriver: {}", e.getMessage());
                }
            }
        }
    }

    @Value("${logging.level.org.openqa.selenium:INFO}")
    private String seleniumLogLevel;

    @PostConstruct
    public void init() {
        log.info("AttendanceScraperService initialized");
        try {
            // Check Chrome installation
            ProcessBuilder pb = new ProcessBuilder("which", "google-chrome");
            Process p = pb.start();
            int exitCode = p.waitFor();
            log.info("Chrome check exit code: {}", exitCode);

            // Check ChromeDriver
            pb = new ProcessBuilder("which", "chromedriver");
            p = pb.start();
            exitCode = p.waitFor();
            log.info("ChromeDriver check exit code: {}", exitCode);
        } catch (Exception e) {
            log.warn("Environment check failed: {}", e.getMessage());
        }
    }

    private WebDriver setupWebDriver() {
        log.info("Setting up WebDriver with headless={}", headless);

        try {
            log.info("Chrome paths to check:");
            String[] chromePaths = {
                    "/opt/google/chrome/chrome",
                    "/usr/bin/google-chrome",
                    "/usr/bin/google-chrome-stable"
            };

            String chromePath = null;
            for (String path : chromePaths) {
                if (new File(path).exists()) {
                    chromePath = path;
                    log.info("Found Chrome at: {}", path);
                    break;
                }
            }

            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920,1080");  // Set a proper window size
            options.addArguments("--start-maximized");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");

            // Set binary path if found
            if (chromePath != null) {
                options.setBinary(chromePath);
            }

            // Let WebDriverManager handle finding ChromeDriver
            WebDriverManager.chromedriver().setup();

            log.info("Creating ChromeDriver with options: {}", options);
            return new ChromeDriver(options);
        } catch (Exception e) {
            log.error("Error setting up WebDriver: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    private void login(WebDriver driver, WebDriverWait wait, User user) {
        log.info("Logging in to e-Governance portal");
        driver.get("https://charusat.edu.in:912/eGovernance/");

        try {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"txtUserName\"]")));
            typeSlowly(usernameField, user.getEgovId());

            WebElement passwordField = driver.findElement(By.xpath("//*[@id=\"txtPassword\"]"));
            typeSlowly(passwordField, user.getEgovPassword());

            WebElement loginButton = driver.findElement(By.id("btnLogin"));
            loginButton.click();

            // Wait for the page to load after login
            wait.until(ExpectedConditions.urlContains("frmAppSelection.aspx"));
            log.info("Login successful");
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new RuntimeException("Failed to login to e-Governance portal", e);
        }
    }

    private void navigateToAttendancePage(WebDriver driver, WebDriverWait wait) {
        log.info("Navigating to attendance page");

        try {
            // Wait for page to be fully loaded after login
            slowDown(3000);

            // First try using the original XPath
            try {
                log.info("Attempting to find element with original XPath");
                WebElement lectureGrossAttendance = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id=\"grdGrossAtt_ctl01_lnkRequestViewTT\"]")));
                lectureGrossAttendance.click();
                log.info("Successfully clicked original XPath element");
            } catch (Exception e) {
                log.warn("Original XPath not found, trying alternative methods: {}", e.getMessage());

                // Try alternative 1: Look for elements by partial ID
                try {
                    log.info("Trying to find element containing 'lnkRequestViewTT' in ID");
                    WebElement attendanceLink = driver.findElement(
                            By.cssSelector("[id$='lnkRequestViewTT']"));
                    attendanceLink.click();
                    log.info("Successfully clicked element with partial ID match");
                } catch (Exception e2) {
                    log.warn("Partial ID search failed: {}", e2.getMessage());

                    // Try alternative 2: Look for link by text content
                    try {
                        log.info("Trying to find element by link text/content");
                        List<WebElement> links = driver.findElements(By.tagName("a"));
                        for (WebElement link : links) {
                            String text = link.getText().toLowerCase();
                            if (text.contains("attendance") || text.contains("view") || text.contains("detail")) {
                                log.info("Found potential link with text: {}", text);
                                link.click();
                                log.info("Clicked link with text: {}", text);
                                break;
                            }
                        }
                    } catch (Exception e3) {
                        // Last resort: Try to navigate directly to the attendance page URL if you know it
                        log.warn("All element finding methods failed, attempting direct navigation");
                        try {
                            // Replace with actual attendance page URL if known
                            driver.get("https://charusat.edu.in:912/eGovernance/frmStudentAttendanceStatus.aspx");
                            log.info("Attempted direct navigation to attendance page");
                        } catch (Exception e4) {
                            log.error("Direct navigation failed: {}", e4.getMessage());
                            throw new RuntimeException("Failed to navigate to attendance page: " + e4.getMessage());
                        }
                    }
                }
            }

            // Wait for the attendance table to load
            log.info("Waiting for attendance table to load");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("gvGrossAttPop")));
                log.info("Attendance table found");
            } catch (Exception e) {
                log.warn("Could not find attendance table with ID 'gvGrossAttPop': {}", e.getMessage());

                // Try alternative table locators
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("table[id*='GrossAtt']")));
                    log.info("Found alternative attendance table");
                } catch (Exception e2) {
                    log.warn("Alternative table search failed: {}", e2.getMessage());
                }
            }

            // Click the more button to see detailed attendance (if needed)
            try {
                log.info("Attempting to click 'more' button");
                WebElement moreButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id=\"footAnnouncement\"]")));
                moreButton.click();
                log.info("Successfully clicked 'more' button");

                // Wait for detailed view to load
                slowDown(3000);
            } catch (Exception e) {
                log.warn("More button not found or not clickable: {}", e.getMessage());
                // Try alternative more button locators
                try {
                    List<WebElement> buttons = driver.findElements(By.tagName("button"));
                    for (WebElement button : buttons) {
                        String text = button.getText().toLowerCase();
                        if (text.contains("more") || text.contains("detail") || text.contains("view")) {
                            button.click();
                            log.info("Clicked alternative 'more' button with text: {}", text);
                            slowDown(3000);
                            break;
                        }
                    }
                } catch (Exception e2) {
                    log.warn("Alternative more button not found: {}", e2.getMessage());
                    // Continue execution, maybe detailed view is already showing
                }
            }
        } catch (Exception e) {
            log.error("Navigation to attendance page failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to navigate to attendance page", e);
        }
    }

    private Map<String, Subject> extractAttendanceData(WebDriver driver, User user) {
        log.info("Extracting attendance data");
        Map<String, Subject> subjects = new HashMap<>();

        try {
            // Try multiple ways to find the attendance table
            WebElement attendanceTable = null;

            try {
                attendanceTable = driver.findElement(By.id("gvGrossAttPop"));
                log.info("Found attendance table with ID 'gvGrossAttPop'");
            } catch (Exception e) {
                log.warn("Could not find table with ID 'gvGrossAttPop', trying alternatives");

                try {
                    // Try finding by CSS selector for tables containing "GrossAtt" in ID
                    attendanceTable = driver.findElement(By.cssSelector("table[id*='GrossAtt']"));
                    log.info("Found attendance table by partial ID match");
                } catch (Exception e2) {
                    // Last resort: try to find any table with attendance-like data
                    List<WebElement> tables = driver.findElements(By.tagName("table"));
                    log.info("Found {} tables on page", tables.size());

                    for (WebElement table : tables) {
                        try {
                            String tableText = table.getText().toLowerCase();
                            if (tableText.contains("attendance") ||
                                    tableText.contains("present") ||
                                    tableText.contains("lecture") ||
                                    tableText.contains("lab")) {
                                attendanceTable = table;
                                log.info("Selected table by content match");
                                break;
                            }
                        } catch (Exception e3) {
                            // Skip this table if we can't read it
                        }
                    }
                }
            }

            if (attendanceTable == null) {
                log.error("Could not find attendance table by any method");
                return subjects;
            }

            // Find all rows in the table
            List<WebElement> rows = attendanceTable.findElements(By.tagName("tr"));
            log.info("Found {} rows in the attendance table", rows.size());

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                try {
                    WebElement row = rows.get(i);
                    List<WebElement> cells = row.findElements(By.tagName("td"));

                    if (cells.size() >= 4) {
                        log.info("Processing row {} with {} cells", i, cells.size());
                        String courseWithCode = cells.get(0).getText().trim(); // Format: "CE262 / DCN"
                        String classType = cells.get(1).getText().trim();      // Format: "LECT" or "LAB"
                        String presentTotal = cells.get(2).getText().trim();   // Format: "41/52"
                        String percentageText = cells.get(3).getText().trim(); // Format: "78%"

                        log.info("Raw data - Course: {}, Type: {}, Present/Total: {}, Percentage: {}",
                                courseWithCode, classType, presentTotal, percentageText);

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
                    } else {
                        log.warn("Row {} has insufficient cells: {}", i, cells.size());
                    }
                } catch (Exception e) {
                    log.warn("Error processing row {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error extracting attendance data: {}", e.getMessage(), e);
        }

        log.info("Successfully extracted {} subjects", subjects.size());
        return subjects;
    }

    private void saveAttendanceData(Map<String, Subject> subjects, User user) {
        log.info("Saving attendance data for {} subjects", subjects.size());

        for (Map.Entry<String, Subject> entry : subjects.entrySet()) {
            Subject subject = entry.getValue();
            log.info("Processing subject for saving: {} ({})", subject.getName(), subject.getClassType());

            try {
                // Save or update the subject
                Subject savedSubject = attendanceService.saveOrUpdateSubject(subject);
                log.info("Subject saved/updated with ID: {}", savedSubject.getId());

                // Create attendance record
                AttendanceRecord record = new AttendanceRecord();
                record.setUser(user);
                record.setSubject(savedSubject);
                record.setAttendancePercentage(savedSubject.getCurrentAttendance());
                record.setRecordedAt(LocalDateTime.now());
                record.setAttendedClasses(subject.getAttendedClasses());
                record.setTotalClasses(subject.getTotalClasses());

                // Save the attendance record
                AttendanceRecord savedRecord = attendanceService.saveAttendanceRecord(record);
                log.info("Saved attendance record with ID: {} for subject: {} ({})",
                        savedRecord.getId(), savedSubject.getName(), savedSubject.getClassType());
            } catch (Exception e) {
                log.error("Error saving data for subject {}: {}", subject.getName(), e.getMessage(), e);
                // Continue with other subjects even if one fails
            }
        }

        log.info("Completed saving all attendance data");
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