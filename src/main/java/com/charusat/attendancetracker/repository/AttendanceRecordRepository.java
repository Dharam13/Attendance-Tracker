package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByUserOrderByRecordedAtDesc(User user);

    List<AttendanceRecord> findBySubjectOrderByRecordedAtDesc(Subject subject);

    List<AttendanceRecord> findByUserAndSubject(User user, Subject subject);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user = ?1 AND ar.recordedAt >= ?2")
    List<AttendanceRecord> findRecentRecords(User user, LocalDateTime since);

    List<AttendanceRecord> findByUserId(Long userId);
}