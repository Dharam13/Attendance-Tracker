package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import com.charusat.attendancetracker.entity.SubjectMaster;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.model.TimetableEntryDTO;
import com.charusat.attendancetracker.service.SchedulingService;
import com.charusat.attendancetracker.service.SubjectMasterService;
import com.charusat.attendancetracker.service.TimeTableService;
import com.charusat.attendancetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private TimeTableService timeTableService;

    @Autowired
    private SubjectMasterService subjectMasterService;

    @Autowired
    private SchedulingService schedulingService;

    /**
     * Main dashboard page
     */
    @GetMapping("/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());
        if (optUser.isPresent()) {
            // Load all required data for dashboard
            loadDashboardData(model);
            return "admin_dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Helper method to load all data needed for the dashboard
     */
    private void loadDashboardData(Model model) {
        // Load timetable entries
        List<TimetableEntryDTO> timetableEntries = timeTableService.getAllTimetableEntries();
        model.addAttribute("timetableEntries", timetableEntries);

        // Load subjects
        List<SubjectMaster> subjects = subjectMasterService.getAllSubjects();
        model.addAttribute("subjects", subjects);

        // Load scheduling configuration
        SchedulingConfig schedulingConfig = schedulingService.getSchedulingConfig();
        model.addAttribute("schedulingConfig", schedulingConfig);

        // Load scheduling logs
        List<SchedulingLog> schedulingLogs = schedulingService.getRecentSchedulingLogs();
        model.addAttribute("schedulingLogs", schedulingLogs);

        // Generate timetable grid data
        generateTimetableGridData(model);

        // Add welcome message
        model.addAttribute("message", "Welcome to the Attendance Tracker Admin Dashboard");
    }

    /**
     * Generate data for timetable grid display
     */
    private void generateTimetableGridData(Model model) {
        // For each hour, get timetable entries for that time slot
        Map<Integer, List<TimetableEntryDTO>> timeTableByHour = new HashMap<>();
        for (int hour = 9; hour <= 15; hour++) {
            String timeSlot = String.format("%d:00-%d:00", hour, hour + 1);
            List<TimetableEntryDTO> entriesForHour = timeTableService.getTimetableEntriesByTimeSlot(timeSlot);

            // Create a list to hold entries for each day of the week
            List<TimetableEntryDTO> entriesByDay = new ArrayList<>(Arrays.asList(null, null, null, null, null));

            // Fill in entries for each day
            for (TimetableEntryDTO entry : entriesForHour) {
                int dayIndex = getDayIndex(entry.getDay());
                if (dayIndex >= 0 && dayIndex < 5) {
                    entriesByDay.set(dayIndex, entry);
                }
            }

            // Add to model
            model.addAttribute("timeTable" + hour, entriesByDay);
        }
    }

    /**
     * Helper method to get day index (0=Monday, 1=Tuesday, etc.)
     */
    private int getDayIndex(String day) {
        switch (day) {
            case "Monday": return 0;
            case "Tuesday": return 1;
            case "Wednesday": return 2;
            case "Thursday": return 3;
            case "Friday": return 4;
            default: return -1;
        }
    }
}