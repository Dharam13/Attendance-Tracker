package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.SchedulingConfigRepository;
import com.charusat.attendancetracker.repository.SchedulingLogRepository;
import com.charusat.attendancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulingServiceImpl implements SchedulingService {

    private final SchedulingConfigRepository configRepository;
    private final SchedulingLogRepository logRepository;
    private final UserRepository userRepository;

    @Override
    public SchedulingConfig getSchedulingConfig() {
        return configRepository.findTopByOrderByIdDesc();
    }

    @Override
    public List<SchedulingLog> getRecentSchedulingLogs() {
        return logRepository.findTop50ByOrderByTimestampDesc();
    }

    @Override
    public void saveSchedulingConfig(SchedulingConfig config) {
        configRepository.save(config);

        // Log the configuration change
        logScheduledEvent(
                "CONFIGURATION_UPDATE",
                "SUCCESS",
                "Updated scheduling configuration"
        );
    }

    @Override
    public void logScheduledEvent(String eventType, String status, String details) {
        SchedulingLog schedulingLog = new SchedulingLog();
        schedulingLog.setEventType(eventType);
        schedulingLog.setStatus(status);
        schedulingLog.setDetails(details);
        schedulingLog.setTimestamp(LocalDateTime.now());
        logRepository.save(schedulingLog);

        log.info("Scheduling log: {} | {} | {}", eventType, status, details);
    }

    @Override
    public List<User> getUsersForAttendanceScraping() {
        return userRepository.findByEgovIdIsNotNull();
    }
}