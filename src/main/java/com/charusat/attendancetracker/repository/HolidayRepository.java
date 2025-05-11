package com.charusat.attendancetracker.repository;

import com.charusat.attendancetracker.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE h.date = ?1")
    Holiday findByDate(LocalDate date);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h WHERE h.date = ?1")
    boolean isHoliday(LocalDate date);
}