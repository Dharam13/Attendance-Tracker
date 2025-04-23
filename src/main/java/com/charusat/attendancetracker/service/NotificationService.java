package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.Notification;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Add this method for recent notifications (limited to 5)
    public List<Notification> getRecentNotificationsForUser(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    public void processUnsentNotifications() {
        List<Notification> unsentNotifications = notificationRepository.findBySentOrderByCreatedAtAsc(false);

        for (Notification notification : unsentNotifications) {
            if (notification.getUser().getNotificationsEnabled()) {
                try {
                    String to = notification.getUser().getEmail();
                    String subject = "Attendance Notification - CHARUSAT Attendance Tracker";
                    String body = "Dear " + notification.getUser().getName() + ",\n\n" +
                            notification.getMessage() + "\n\n" +
                            "This is an automated message from the CHARUSAT Attendance Tracker system.\n\n" +
                            "Regards,\nCHARUSAT Attendance Tracker";

                    emailService.sendSimpleMessage(to, subject, body);

                    notification.setSent(true);
                    notificationRepository.save(notification);
                } catch (Exception e) {
                    // Log error but continue processing other notifications
                }
            }
        }
    }
}