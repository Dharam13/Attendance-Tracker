package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUser(User user);
    Optional<Subject> findByUserAndCode(User user, String code);

    List<Subject> findByUserId(Long userId);
}