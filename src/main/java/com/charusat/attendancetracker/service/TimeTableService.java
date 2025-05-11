package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.TimeTable;
import com.charusat.attendancetracker.repository.TimeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.charusat.attendancetracker.model.TimetableEntryDTO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


public interface TimeTableService {
    List<TimetableEntryDTO> getAllTimetableEntries();
    List<TimetableEntryDTO> filterTimetableEntries(String department, Integer semester, String section, String day);
    List<TimetableEntryDTO> getTimetableEntriesByTimeSlot(String timeSlot);
    TimetableEntryDTO getTimetableEntryById(Long id);
    void saveTimeTableEntry(TimetableEntryDTO timetableEntryDTO);
    void deleteTimeTableEntry(Long id);
}