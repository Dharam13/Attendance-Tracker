// SchedulingConfigRepository.java
package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulingConfigRepository extends JpaRepository<SchedulingConfig, Long> {
    // Return the latest config
    SchedulingConfig findTopByOrderByIdDesc();
}