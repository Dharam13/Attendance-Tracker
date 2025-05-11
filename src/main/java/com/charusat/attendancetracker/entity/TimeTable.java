package com.charusat.attendancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "timetable")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department; // CE, IT, etc.

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private Integer division;

    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(nullable = false)
    private String timeSlot; // e.g., "9:10-10:10"

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectMaster subject;

    @Column(nullable = false)
    private String facultyCode; // Faculty code as per the timetable
}