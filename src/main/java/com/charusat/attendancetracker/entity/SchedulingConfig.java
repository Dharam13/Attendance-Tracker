package com.charusat.attendancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SchedulingConfig Entity
 */
@Entity
@Table(name = "scheduling_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int minutesBefore;

    @Column(nullable = false)
    private int minutesAfter;

    @Column
    private String dailyReportTime;

    @Column
    private String weeklyReportDay;

    @Column
    private String weeklyReportTime;

    @Column
    private String monthlyReportDay;

    @Column
    private String monthlyReportTime;

    @Column
    private boolean emailNotificationsEnabled;

    @Column
    private String attendanceCronExpression;

    @Column
    private String dailyReportCronExpression;

    @Column
    private String weeklyReportCronExpression;

    @Column
    private String monthlyReportCronExpression;
}