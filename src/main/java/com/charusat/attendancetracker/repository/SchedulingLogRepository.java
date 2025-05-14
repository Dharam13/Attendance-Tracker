// SchedulingLogRepository.java
package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.SchedulingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchedulingLogRepository extends JpaRepository<SchedulingLog, Long> {
    List<SchedulingLog> findTop50ByOrderByTimestampDesc();
}