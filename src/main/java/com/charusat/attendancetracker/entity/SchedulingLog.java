// SchedulingLog.java
package com.charusat.attendancetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String eventType;
    private String status;
    private String details;

    // Constructor with required fields
    public SchedulingLog(String eventType, String status, String details) {
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.status = status;
        this.details = details;
    }
}