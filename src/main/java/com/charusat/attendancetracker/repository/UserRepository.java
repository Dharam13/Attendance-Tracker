package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEgovId(String egovId);
    @Query("SELECT u FROM User u WHERE u.egovId IS NOT NULL")
    List<User> findByEgovIdIsNotNull();
}