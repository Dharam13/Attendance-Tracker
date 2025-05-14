package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.User;
import java.util.List;

public interface SchedulingService {
    /**
     * Get the current scheduling configuration
     */
    SchedulingConfig getSchedulingConfig();

    /**
     * Get recent scheduling logs
     */
    List<com.charusat.attendancetracker.entity.SchedulingLog> getRecentSchedulingLogs();

    /**
     * Save scheduling configuration
     */
    void saveSchedulingConfig(SchedulingConfig config);

    /**
     * Log a scheduled event
     */
    void logScheduledEvent(String eventType, String status, String details);

    /**
     * Get all users that need attendance scraping
     */
    List<User> getUsersForAttendanceScraping();
}