package com.charusat.attendancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subject_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private String department; // CE, IT, etc.

    @Column(nullable = false)
    private Integer semester;
}