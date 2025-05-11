package com.charusat.attendancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * SchedulingLog Entity
 */
@Entity
@Table(name = "scheduling_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String status;

    @Column
    private String details;
}