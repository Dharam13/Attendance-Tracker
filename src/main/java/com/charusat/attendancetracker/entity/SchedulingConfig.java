package com.charusat.attendancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SchedulingConfig Entity - Simplified version
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
    private boolean emailNotificationsEnabled;

    @Column(nullable = false)
    private String attendanceCronExpression;

    @Column(nullable = false)
    private String dailyReportCronExpression;

    @Column(nullable = false)
    private String weeklyReportCronExpression;

    @Column(nullable = false)
    private String monthlyReportCronExpression;

    // These fields are kept for backward compatibility but won't be used actively
    @Column
    private int minutesBefore = 0;

    @Column
    private int minutesAfter = 0;

    @Column
    private String dailyReportTime = "17:00";

    @Column
    private String weeklyReportDay = "Monday";

    @Column
    private String weeklyReportTime = "10:00";

    @Column
    private String monthlyReportDay = "1";

    @Column
    private String monthlyReportTime = "12:00";
}