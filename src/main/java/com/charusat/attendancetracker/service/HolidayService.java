package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.Holiday;
import com.charusat.attendancetracker.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;

    @Autowired
    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public Holiday getHolidayById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));
    }

    public List<Holiday> getHolidaysBetweenDates(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByDateBetween(startDate, endDate);
    }

    public Holiday getHolidayByDate(LocalDate date) {
        return holidayRepository.findByDate(date);
    }

    public boolean isHoliday(LocalDate date) {
        return holidayRepository.isHoliday(date);
    }

    public Holiday saveHoliday(Holiday holiday) {
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }
}