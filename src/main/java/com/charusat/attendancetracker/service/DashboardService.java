package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.dto.*;
import com.charusat.attendancetracker.entity.*;
import com.charusat.attendancetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public UserDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getAttendanceThreshold(),
                user.getNotificationsEnabled()
        );
    }

    public List<SubjectDto> getSubjectsForUser(Long userId) {
        List<Subject> subjects = subjectRepository.findByUserId(userId);
        return subjects.stream()
                .map(subject -> {
                    List<AttendanceRecordDTO> attendanceDTOs = subject.getAttendanceRecords().stream()
                            .map(record -> new AttendanceRecordDTO(
                                    record.getId(),
                                    record.getAttendancePercentage(),
                                    record.getAttendedClasses(),
                                    record.getRecordedAt(),
                                    record.getTotalClasses(),
                                    subject.getId(),
                                    subject.getUser().getId()
                            ))
                            .collect(Collectors.toList());

                    return new SubjectDto(
                            subject.getId(),
                            subject.getName(),
                            subject.getCode(),
                            subject.getClassType(),
                            subject.getAttendedClasses(),
                            subject.getTotalClasses(),
                            subject.getCurrentAttendance(),
                            attendanceDTOs
                    );
                })
                .collect(Collectors.toList());
    }


    public List<AttendanceRecordDTO> getAttendanceRecordsForUser(Long userId) {
        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByUserId(userId);
        return attendanceRecords.stream()
                .map(record -> new AttendanceRecordDTO(
                        record.getId(),
                        record.getAttendancePercentage(),
                        record.getAttendedClasses(),
                        record.getRecordedAt(),
                        record.getTotalClasses(),
                        record.getSubject().getId(),
                        record.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream()
                .map(notification -> new NotificationDTO(
                        notification.getId(),
                        notification.getUser().getId(),
                        notification.getSubject().getId(),
                        notification.getMessage(),
                        notification.getCreatedAt(),
                        notification.getSent(),
                        notification.getSentAt()
                ))
                .collect(Collectors.toList());
    }
}
