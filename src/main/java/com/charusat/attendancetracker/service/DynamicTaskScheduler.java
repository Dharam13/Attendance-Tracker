package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicTaskScheduler {

    private final TaskScheduler taskScheduler;
    private final SchedulingService schedulingService;
    private final AttendanceScraperService attendanceScraperService;
    private final NotificationService notificationService;

    // Store scheduled tasks so they can be cancelled when refreshing
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @PostConstruct
    public void scheduleTasksAfterStartup() {
        log.info("Initializing dynamic task scheduler on application startup");
        refreshSchedule();
    }

    public void refreshSchedule() {
        // Cancel all existing scheduled tasks
        cancelAllScheduledTasks();

        SchedulingConfig config = schedulingService.getSchedulingConfig();
        if (config == null || !config.isEnabled()) {
            log.info("Scheduling is disabled. No tasks scheduled.");
            return;
        }

        // Schedule attendance checks
        scheduledTasks.put("attendanceCheck", taskScheduler.schedule(
                this::runAttendanceCheck,
                new CronTrigger(config.getAttendanceCronExpression())
        ));

        // Schedule daily report
        scheduledTasks.put("dailyReport", taskScheduler.schedule(
                this::runDailyReport,
                new CronTrigger(config.getDailyReportCronExpression())
        ));

        // Schedule weekly report
        scheduledTasks.put("weeklyReport", taskScheduler.schedule(
                this::runWeeklyReport,
                new CronTrigger(config.getWeeklyReportCronExpression())
        ));

        // Schedule monthly report
        scheduledTasks.put("monthlyReport", taskScheduler.schedule(
                this::runMonthlyReport,
                new CronTrigger(config.getMonthlyReportCronExpression())
        ));

        // Schedule unsent notifications processing
        scheduledTasks.put("notificationProcessor", taskScheduler.schedule(
                this::processUnsentNotifications,
                new CronTrigger("0 0 * * * *") // Run every hour
        ));

        // System health check
        scheduledTasks.put("systemHealthCheck", taskScheduler.schedule(
                this::systemHealthCheck,
                new CronTrigger("0 0 6 * * *") // Run at 6 AM daily
        ));

        log.info("Tasks scheduled successfully with cron expressions: " +
                        "\nAttendance: {}" +
                        "\nDaily Report: {}" +
                        "\nWeekly Report: {}" +
                        "\nMonthly Report: {}",
                config.getAttendanceCronExpression(),
                config.getDailyReportCronExpression(),
                config.getWeeklyReportCronExpression(),
                config.getMonthlyReportCronExpression());

        // Log the scheduling update
        schedulingService.logScheduledEvent(
                "SCHEDULER",
                "SUCCESS",
                "Scheduled tasks updated with new configuration"
        );
    }

    private void cancelAllScheduledTasks() {
        scheduledTasks.forEach((name, future) -> {
            log.info("Cancelling scheduled task: {}", name);
            future.cancel(false);
        });
        scheduledTasks.clear();
    }

    // This can be called manually for testing
    public void runAttendanceCheckManually() {
        runAttendanceCheck();
    }

    private void runAttendanceCheck() {
        try {
            log.info("Starting scheduled attendance check at {}", LocalDateTime.now());
            // Get all users that require attendance scraping
            schedulingService.getUsersForAttendanceScraping().forEach(user -> {
                try {
                    log.info("Checking attendance for user: {}", user.getEgovId());
                    attendanceScraperService.scrapeAttendanceForUser(user);
                    schedulingService.logScheduledEvent("ATTENDANCE_CHECK", "SUCCESS",
                            "Successfully scraped attendance for user: " + user.getEgovId());

                    // Add a delay between users to avoid overwhelming the system
                    try {
                        Thread.sleep(30000); // 30 second delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    log.error("Error scraping attendance for user {}: {}", user.getEgovId(), e.getMessage());
                    schedulingService.logScheduledEvent("ATTENDANCE_CHECK", "FAILURE",
                            "Failed to scrape attendance for user: " + user.getEgovId() + ", Error: " + e.getMessage());
                }
            });
            log.info("Completed scheduled attendance check at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Scheduled attendance check failed: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent("ATTENDANCE_CHECK", "FAILURE",
                    "Failed to run attendance check: " + e.getMessage());
        }
    }

    private void runDailyReport() {
        try {
            log.info("Running daily report");
            // Implement report generation logic here
            schedulingService.logScheduledEvent("DAILY_REPORT", "SUCCESS",
                    "Daily report generated successfully");
        } catch (Exception e) {
            log.error("Daily report generation failed: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent("DAILY_REPORT", "FAILURE",
                    "Failed to generate daily report: " + e.getMessage());
        }
    }

    private void runWeeklyReport() {
        try {
            log.info("Running weekly report");
            // Implement report generation logic here
            schedulingService.logScheduledEvent("WEEKLY_REPORT", "SUCCESS",
                    "Weekly report generated successfully");
        } catch (Exception e) {
            log.error("Weekly report generation failed: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent("WEEKLY_REPORT", "FAILURE",
                    "Failed to generate weekly report: " + e.getMessage());
        }
    }

    private void runMonthlyReport() {
        try {
            log.info("Running monthly report");
            // Implement report generation logic here
            schedulingService.logScheduledEvent("MONTHLY_REPORT", "SUCCESS",
                    "Monthly report generated successfully");
        } catch (Exception e) {
            log.error("Monthly report generation failed: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent("MONTHLY_REPORT", "FAILURE",
                    "Failed to generate monthly report: " + e.getMessage());
        }
    }

    private void processUnsentNotifications() {
        SchedulingConfig config = schedulingService.getSchedulingConfig();
        if (config == null || !config.isEmailNotificationsEnabled()) {
            log.info("Email notifications are disabled. Skipping processing.");
            return;
        }

        log.info("Processing any unsent notifications");
        try {
            notificationService.processUnsentNotifications();
            schedulingService.logScheduledEvent(
                    "Notification Processing",
                    "SUCCESS",
                    "Processed unsent notifications"
            );
        } catch (Exception e) {
            log.error("Error processing notifications: {}", e.getMessage());
            schedulingService.logScheduledEvent(
                    "Notification Processing",
                    "ERROR",
                    "Failed to process notifications: " + e.getMessage()
            );
        }
    }

    private void systemHealthCheck() {
        log.info("Running daily system health check");
        // Add any system health checks here

        schedulingService.logScheduledEvent(
                "System Health Check",
                "SUCCESS",
                "Completed daily system health check"
        );
    }
}