package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.SubjectMaster;
import com.charusat.attendancetracker.entity.TimeTable;
import com.charusat.attendancetracker.model.TimetableEntryDTO;
import com.charusat.attendancetracker.service.SubjectMasterService;
import com.charusat.attendancetracker.service.TimeTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/timetable")
public class TimetableController {

    @Autowired
    private TimeTableService timeTableService;

    @Autowired
    private SubjectMasterService subjectMasterService;

    /**
     * Display the main timetable management dashboard
     */
    @GetMapping
    public String showTimetableManagement(Model model) {
        model.addAttribute("message", "Welcome to the timetable management dashboard");
        model.addAttribute("subjects", subjectMasterService.getAllSubjects());
        return "admin/timetable_management";
    }

    /**
     * Filter timetable entries based on criteria
     */
    @GetMapping("/filter")
    public String filterTimetable(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String day,
            Model model) {

        List<TimetableEntryDTO> filteredEntries = timeTableService.filterTimetableEntries(
                department, semester, section, day);

        model.addAttribute("timetableEntries", filteredEntries);
        model.addAttribute("message", "Filtered timetable entries");

        // Generate timetable grid data
        Map<String, List<TimetableEntryDTO>> timeTableByHour = new HashMap<>();
        for (int hour = 9; hour <= 15; hour++) {
            String timeSlot = String.format("%d:00-%d:00", hour, hour + 1);
            List<TimetableEntryDTO> entriesForHour = timeTableService.getTimetableEntriesByTimeSlot(timeSlot);
            timeTableByHour.put("timeTable" + hour, entriesForHour);
            model.addAttribute("timeTable" + hour, entriesForHour);
        }

        return "admin_dashboard";
    }

    /**
     * Save a new timetable entry or update an existing one
     */
    @PostMapping("/save")
    public String saveTimetableEntry(
            @ModelAttribute TimetableEntryDTO timetableEntryDTO,
            RedirectAttributes redirectAttributes) {

        try {
            timeTableService.saveTimeTableEntry(timetableEntryDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    timetableEntryDTO.getId() == null ?
                            "Timetable entry added successfully!" :
                            "Timetable entry updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving timetable entry: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Delete a timetable entry
     */
    @PostMapping("/delete")
    public String deleteTimetableEntry(
            @RequestParam Long id,
            RedirectAttributes redirectAttributes) {

        try {
            timeTableService.deleteTimeTableEntry(id);
            redirectAttributes.addFlashAttribute("successMessage", "Timetable entry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting timetable entry: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Get timetable entry details for editing
     */
    @GetMapping("/edit/{id}")
    @ResponseBody
    public TimetableEntryDTO getTimetableEntryForEdit(@PathVariable Long id) {
        return timeTableService.getTimetableEntryById(id);
    }
}