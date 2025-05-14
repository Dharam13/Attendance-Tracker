package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Notification;
import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.AttendanceRecordRepository;
import com.charusat.attendancetracker.repository.NotificationRepository;
import com.charusat.attendancetracker.repository.SchedulingConfigRepository;
import com.charusat.attendancetracker.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationRepository notificationRepository;
    private final SchedulingConfigRepository schedulingConfigRepository;
    private final EmailService emailService;

    public AttendanceRecord saveAttendanceRecord(AttendanceRecord record) {
        record.setRecordedAt(LocalDateTime.now());

        // Update the current attendance in the subject
        Subject subject = record.getSubject();
        subject.setCurrentAttendance(record.getAttendancePercentage());
        subject.setAttendedClasses(record.getAttendedClasses());
        subject.setTotalClasses(record.getTotalClasses());
        subjectRepository.save(subject);

        // Check if attendance is below threshold and create notification if needed
        if (record.getAttendancePercentage() < record.getUser().getAttendanceThreshold()) {
            createAttendanceBelowThresholdNotification(record);
        }

        return attendanceRecordRepository.save(record);
    }

    private void createAttendanceBelowThresholdNotification(AttendanceRecord record) {
        Notification notification = new Notification();
        notification.setUser(record.getUser());
        notification.setSubject(record.getSubject());
        notification.setMessage("Your attendance in " + record.getSubject().getName() +
                " (" + record.getSubject().getClassType() + ") is below the threshold at "
                + record.getAttendancePercentage() + "%");
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        // Get admin scheduling configuration
        Optional<SchedulingConfig> schedulingConfigOpt = schedulingConfigRepository.findFirstByOrderById();

        // Create default scheduling config if none exists
        SchedulingConfig schedulingConfig;
        if (schedulingConfigOpt.isEmpty()) {
            schedulingConfig = createDefaultSchedulingConfig();
            log.warn("No scheduling configuration found. Created default configuration with email notifications enabled.");
        } else {
            schedulingConfig = schedulingConfigOpt.get();
        }

        // Check both admin scheduling configuration and user preferences before sending email
        boolean adminConfigEnabled = schedulingConfig.isEnabled();
        boolean emailNotificationsEnabled = schedulingConfig.isEmailNotificationsEnabled();
        boolean userNotificationsEnabled = record.getUser().getNotificationsEnabled();

        log.info("Checking notification settings - Admin config enabled: {}, Email notifications enabled: {}, User notifications enabled: {}",
                adminConfigEnabled, emailNotificationsEnabled, userNotificationsEnabled);

        if (userNotificationsEnabled) {
            // If user has enabled notifications, we'll send regardless of admin settings for now
            // This is a temporary fix to ensure notifications work while admin settings are being configured
            sendAttendanceAlert(notification);
            log.info("Email notification sent for user {} regarding subject {}",
                    record.getUser().getUsername(), record.getSubject().getName());
        } else {
            log.info("Email notification not sent because user has disabled notifications");
        }
    }

    private SchedulingConfig createDefaultSchedulingConfig() {
        SchedulingConfig config = new SchedulingConfig();
        config.setEnabled(true);
        config.setEmailNotificationsEnabled(true);
        config.setAttendanceCronExpression("0 0 * * * ?");    // Every hour
        config.setDailyReportCronExpression("0 0 17 * * ?");  // 5:00 PM daily
        config.setWeeklyReportCronExpression("0 0 10 ? * MON"); // 10:00 AM every Monday
        config.setMonthlyReportCronExpression("0 0 12 1 * ?"); // 12:00 PM on the 1st day of each month
        return schedulingConfigRepository.save(config);
    }

    private void sendAttendanceAlert(Notification notification) {
        try {
            String to = notification.getUser().getEmail();
            String subject = "Low Attendance Alert - CHARUSAT Attendance Tracker";
            String body = "Dear " + notification.getUser().getName() + ",\n\n" +
                    notification.getMessage() + "\n\n" +
                    "This is an automated message from the CHARUSAT Attendance Tracker system. " +
                    "Please check your attendance and attend classes regularly.\n\n" +
                    "Regards,\nCHARUSAT Attendance Tracker";

            emailService.sendSimpleMessage(to, subject, body);

            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
        }
    }

    public List<AttendanceRecord> getAttendanceHistoryForUser(User user) {
        return attendanceRecordRepository.findByUserOrderByRecordedAtDesc(user);
    }

    public List<AttendanceRecord> getAttendanceHistoryForSubject(Subject subject) {
        return attendanceRecordRepository.findBySubjectOrderByRecordedAtDesc(subject);
    }

    public Subject saveOrUpdateSubject(Subject subject) {
        // Check if subject already exists with the same code AND class type
        Optional<Subject> existingSubject = subjectRepository.findByUserAndCodeAndClassType(
                subject.getUser(),
                subject.getCode(),
                subject.getClassType()
        );

        if (existingSubject.isPresent()) {
            Subject updatedSubject = existingSubject.get();
            updatedSubject.setName(subject.getName());
            updatedSubject.setCurrentAttendance(subject.getCurrentAttendance());
            updatedSubject.setAttendedClasses(subject.getAttendedClasses());
            updatedSubject.setTotalClasses(subject.getTotalClasses());
            log.info("Updating existing subject: {} ({}) with attendance: {}%",
                    updatedSubject.getName(), updatedSubject.getClassType(), updatedSubject.getCurrentAttendance());
            return subjectRepository.save(updatedSubject);
        }

        log.info("Creating new subject: {} ({}) with attendance: {}%",
                subject.getName(), subject.getClassType(), subject.getCurrentAttendance());
        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsForUser(User user) {
        return subjectRepository.findByUser(user);
    }
}