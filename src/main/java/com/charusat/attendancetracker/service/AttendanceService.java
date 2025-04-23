package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Notification;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.AttendanceRecordRepository;
import com.charusat.attendancetracker.repository.NotificationRepository;
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
    private final EmailService emailService;

    public AttendanceRecord saveAttendanceRecord(AttendanceRecord record) {
        record.setRecordedAt(LocalDateTime.now());

        // Update the current attendance in the subject
        Subject subject = record.getSubject();
        subject.setCurrentAttendance(record.getAttendancePercentage());
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
                " is below the threshold at " + record.getAttendancePercentage() + "%");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Send email notification if enabled
        if (record.getUser().getNotificationsEnabled()) {
            sendAttendanceAlert(notification);
        }
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
        // Check if subject already exists
        Optional<Subject> existingSubject = subjectRepository.findByUserAndCode(subject.getUser(), subject.getCode());

        if (existingSubject.isPresent()) {
            Subject updatedSubject = existingSubject.get();
            updatedSubject.setName(subject.getName());
            updatedSubject.setCurrentAttendance(subject.getCurrentAttendance());
            return subjectRepository.save(updatedSubject);
        }

        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsForUser(User user) {
        return subjectRepository.findByUser(user);
    }
}