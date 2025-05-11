package com.charusat.attendancetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for TimeTable entry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableEntryDTO {

    private Long id;
    private String department;
    private Integer semester;
    private String section;
    private String day;
    private String timeSlot;
    private String subject;
    private String faculty;
    private String room;
}