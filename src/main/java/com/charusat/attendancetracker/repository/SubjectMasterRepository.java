package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.SubjectMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectMasterRepository extends JpaRepository<SubjectMaster, Long> {

    List<SubjectMaster> findByDepartmentAndSemester(String department, Integer semester);

    Optional<SubjectMaster> findBySubjectCode(String subjectCode);
}