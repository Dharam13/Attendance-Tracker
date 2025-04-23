package com.charusat.attendancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    private Long id;
    private String name;
    private String code;
    private String classType;  // Add this field
    private int attendedClasses;
    private int totalClasses;
    private Double currentAttendance;
    private List<AttendanceRecordDTO> attendanceRecords = new ArrayList<>();
}