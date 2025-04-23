package com.charusat.attendancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AttendanceRecordDTO {
    private Long id;
    private Double attendancePercentage;
    private Integer attendedClasses;
    private LocalDateTime recordedAt;
    private Integer totalClasses;
    private Long subjectId;
    private Long userId;
}
