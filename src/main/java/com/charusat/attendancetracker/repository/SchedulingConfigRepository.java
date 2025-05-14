// SchedulingConfigRepository.java
package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchedulingConfigRepository extends JpaRepository<SchedulingConfig, Long> {
    // Return the latest config
    SchedulingConfig findTopByOrderByIdDesc();
    // Method to find the first scheduling config (assuming there's only one global config)
    Optional<SchedulingConfig> findFirstByOrderById();
}