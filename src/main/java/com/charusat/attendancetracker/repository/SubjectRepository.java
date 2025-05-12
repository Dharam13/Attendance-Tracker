package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUser(User user);
    Optional<Subject> findByUserAndCode(User user, String code);

    // Add this new method to find by user, code AND classType
    Optional<Subject> findByUserAndCodeAndClassType(User user, String code, String classType);
    List<Subject> findByUserId(Long userId);
}