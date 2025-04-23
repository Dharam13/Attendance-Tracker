package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.Notification;
import com.charusat.attendancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findBySentOrderByCreatedAtAsc(Boolean sent);

    List<Notification> findByUserId(Long userId);

    // Add this method for recent notifications
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);
}