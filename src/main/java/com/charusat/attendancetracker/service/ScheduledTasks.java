// ScheduledTasks.java
package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final UserRepository userRepository;
    private final AttendanceScraperService attendanceScraperService;
    private final NotificationService notificationService;

    /**
     * Run attendance checks according to configured schedule in application.properties
     */
    @Scheduled(cron = "${attendance.check.schedule}")
    public void checkAttendanceForAllUsers() {
        log.info("Starting scheduled attendance check at {}", LocalDateTime.now());

        List<User> users = userRepository.findAll();
        log.info("Found {} users to check attendance for", users.size());

        for (User user : users) {
            try {
                log.info("Checking attendance for user: {}", user.getEgovId());
                attendanceScraperService.scrapeAttendanceForUser(user);

                // Add a delay between users to avoid overwhelming the system
                Thread.sleep(30000); // 30 second delay
            } catch (Exception e) {
                log.error("Error checking attendance for user {}: {}", user.getEgovId(), e.getMessage());
            }
        }

        log.info("Completed scheduled attendance check at {}", LocalDateTime.now());
    }

    /**
     * Process any unsent notifications every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void processUnsentNotifications() {
        log.info("Processing any unsent notifications");
        notificationService.processUnsentNotifications();
    }

    /**
     * Daily system health check - can be expanded as needed
     */
    @Scheduled(cron = "0 0 6 * * *") // Run at 6 AM daily
    public void systemHealthCheck() {
        log.info("Running daily system health check");
        // Add any system health checks here
    }
}