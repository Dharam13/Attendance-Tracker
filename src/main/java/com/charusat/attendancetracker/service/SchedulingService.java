package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SchedulingService {
    SchedulingConfig getSchedulingConfig();
    List<SchedulingLog> getRecentSchedulingLogs();
    void saveSchedulingConfig(SchedulingConfig config);

    // Cron expression generators
    String generateAttendanceCronExpression(SchedulingConfig config);
    String generateDailyReportCronExpression(SchedulingConfig config);
    String generateWeeklyReportCronExpression(SchedulingConfig config);
    String generateMonthlyReportCronExpression(SchedulingConfig config);
}
