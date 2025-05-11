package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    List<TimeTable> findByDepartmentAndSemesterAndDivision(String department, Integer semester, Integer division);

    List<TimeTable> findByDepartmentAndSemesterAndDivisionAndDayOfWeek(String department, Integer semester, Integer division, String dayOfWeek);

    @Query("SELECT t FROM TimeTable t WHERE t.department = ?1 AND t.semester = ?2 AND t.division = ?3 AND t.subject.id = ?4")
    List<TimeTable> findClassesForSubject(String department, Integer semester, Integer division, Long subjectId);
}