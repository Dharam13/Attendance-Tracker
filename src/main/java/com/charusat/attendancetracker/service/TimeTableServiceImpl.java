package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.model.TimetableEntryDTO;
import com.charusat.attendancetracker.repository.TimeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TimeTableServiceImpl implements TimeTableService {

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Override
    public List<TimetableEntryDTO> getAllTimetableEntries() {
        return Collections.emptyList(); // placeholder
    }

    @Override
    public List<TimetableEntryDTO> filterTimetableEntries(String department, Integer semester, String section, String day) {
        return Collections.emptyList(); // placeholder
    }

    @Override
    public List<TimetableEntryDTO> getTimetableEntriesByTimeSlot(String timeSlot) {
        return Collections.emptyList(); // placeholder
    }

    @Override
    public TimetableEntryDTO getTimetableEntryById(Long id) {
        return null; // placeholder
    }

    @Override
    public void saveTimeTableEntry(TimetableEntryDTO timetableEntryDTO) {
        // placeholder - do nothing
    }

    @Override
    public void deleteTimeTableEntry(Long id) {
        // placeholder - do nothing
    }
}
