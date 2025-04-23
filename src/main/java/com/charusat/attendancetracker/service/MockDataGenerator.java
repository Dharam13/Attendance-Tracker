package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MockDataGenerator {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public void generateMockData() {
        List<User> users = userRepository.findAll();
        users.forEach(this::generateMockDataForUser);
    }

    public void generateMockDataForUser(User user) {
        // Create sample subjects
        String[][] subjectData = {
                {"CE262", "Data Communication Networks", "LECT"},
                {"CE262", "Data Communication Networks", "LAB"},
                {"CE264", "Database Systems", "LECT"},
                {"CE264", "Database Systems", "LAB"}
        };

        for (String[] data : subjectData) {
            // Create subject
            Subject subject = new Subject();
            subject.setCode(data[0]);
            subject.setName(data[1]);
            subject.setUser(user);

            // Generate random attendance
            double attendance = 60.0 + random.nextDouble() * 35.0;
            subject.setCurrentAttendance(attendance);

            Subject savedSubject = attendanceService.saveOrUpdateSubject(subject);

            // Create attendance record
            AttendanceRecord record = new AttendanceRecord();
            record.setSubject(savedSubject);
            record.setUser(user);
            record.setAttendancePercentage(attendance);
            record.setRecordedAt(LocalDateTime.now());

            // Generate attendance counts
            int totalClasses = 30 + random.nextInt(15);
            int attendedClasses = (int) Math.round(totalClasses * (attendance / 100));

            record.setTotalClasses(totalClasses);
            record.setAttendedClasses(attendedClasses);

            attendanceService.saveAttendanceRecord(record);
        }
    }
}