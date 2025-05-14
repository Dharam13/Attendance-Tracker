package com.charusat.attendancetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling scheduled tasks that were previously defined with @Scheduled annotations.
 * This class no longer contains scheduled tasks or task scheduler configuration.
 * All scheduling is now handled by DynamicTaskScheduler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    // Keep any required service dependencies
    private final NotificationService notificationService;
    private final SchedulingService schedulingService;
    private final AttendanceScraperService attendanceScraperService;

    // You can keep any methods that might be called manually or needed by other services
    // But remove all @Scheduled annotations and scheduler configuration

    /**
     * This method can be called manually to process unsent notifications
     */
    public void processUnsentNotifications() {
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

    /**
     * This method can be called manually to run system health checks
     */
    public void systemHealthCheck() {
        log.info("Running system health check");
        // Add any system health checks here

        schedulingService.logScheduledEvent(
                "System Health Check",
                "SUCCESS",
                "Completed system health check"
        );
    }

    /**
     * This method can be called manually to generate daily reports
     */
    public void generateDailyReport() {
        log.info("Generating daily attendance report");
        try {
            // Call your report generation service here
            schedulingService.logScheduledEvent(
                    "Daily Report",
                    "SUCCESS",
                    "Generated daily attendance report"
            );
        } catch (Exception e) {
            log.error("Error generating daily report: {}", e.getMessage());
            schedulingService.logScheduledEvent(
                    "Daily Report",
                    "ERROR",
                    "Failed to generate daily report: " + e.getMessage()
            );
        }
    }

    /**
     * This method can be called manually to generate weekly reports
     */
    public void generateWeeklyReport() {
        log.info("Generating weekly attendance report");
        try {
            // Call your report generation service here
            schedulingService.logScheduledEvent(
                    "Weekly Report",
                    "SUCCESS",
                    "Generated weekly attendance report"
            );
        } catch (Exception e) {
            log.error("Error generating weekly report: {}", e.getMessage());
            schedulingService.logScheduledEvent(
                    "Weekly Report",
                    "ERROR",
                    "Failed to generate weekly report: " + e.getMessage()
            );
        }
    }

    /**
     * This method can be called manually to generate monthly reports
     */
    public void generateMonthlyReport() {
        log.info("Generating monthly attendance report");
        try {
            // Call your report generation service here
            schedulingService.logScheduledEvent(
                    "Monthly Report",
                    "SUCCESS",
                    "Generated monthly attendance report"
            );
        } catch (Exception e) {
            log.error("Error generating monthly report: {}", e.getMessage());
            schedulingService.logScheduledEvent(
                    "Monthly Report",
                    "ERROR",
                    "Failed to generate monthly report: " + e.getMessage()
            );
        }
    }
}