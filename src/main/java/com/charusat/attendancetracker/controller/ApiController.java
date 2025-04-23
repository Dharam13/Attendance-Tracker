package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.service.AttendanceService;
import com.charusat.attendancetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;
    private final AttendanceService attendanceService;

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            List<Subject> subjects = attendanceService.getSubjectsForUser(optUser.get());
            return ResponseEntity.ok(subjects);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceRecord>> getAttendance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long subjectId) {

        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            User user = optUser.get();

            if (subjectId != null) {
                // Get attendance for specific subject
                Optional<Subject> optSubject = attendanceService.getSubjectsForUser(user)
                        .stream()
                        .filter(s -> s.getId().equals(subjectId))
                        .findFirst();

                if (optSubject.isPresent()) {
                    List<AttendanceRecord> records = attendanceService.getAttendanceHistoryForSubject(optSubject.get());
                    return ResponseEntity.ok(records);
                }

                return ResponseEntity.notFound().build();
            } else {
                // Get all attendance records
                List<AttendanceRecord> records = attendanceService.getAttendanceHistoryForUser(user);
                return ResponseEntity.ok(records);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PatchMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> settings) {

        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            User user = optUser.get();

            // Update user settings
            if (settings.containsKey("attendanceThreshold")) {
                user.setAttendanceThreshold(Double.parseDouble(settings.get("attendanceThreshold").toString()));
            }

            if (settings.containsKey("notificationsEnabled")) {
                user.setNotificationsEnabled((Boolean) settings.get("notificationsEnabled"));
            }

            if (settings.containsKey("email")) {
                user.setEmail((String) settings.get("email"));
            }

            userService.updateUser(user);

            return ResponseEntity.ok(Map.of("status", "success"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}