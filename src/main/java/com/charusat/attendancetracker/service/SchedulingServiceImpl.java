package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Override
    public SchedulingConfig getSchedulingConfig() {
        return null; // placeholder
    }

    @Override
    public List<SchedulingLog> getRecentSchedulingLogs() {
        return Collections.emptyList(); // placeholder
    }

    @Override
    public void saveSchedulingConfig(SchedulingConfig config) {
        // placeholder
    }

    @Override
    public String generateAttendanceCronExpression(SchedulingConfig config) {
        return ""; // placeholder
    }

    @Override
    public String generateDailyReportCronExpression(SchedulingConfig config) {
        return ""; // placeholder
    }

    @Override
    public String generateWeeklyReportCronExpression(SchedulingConfig config) {
        return ""; // placeholder
    }

    @Override
    public String generateMonthlyReportCronExpression(SchedulingConfig config) {
        return ""; // placeholder
    }
}
