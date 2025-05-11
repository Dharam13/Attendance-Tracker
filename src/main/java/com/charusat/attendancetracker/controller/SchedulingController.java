package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import com.charusat.attendancetracker.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/scheduling")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    /**
     * Display scheduling configuration
     */
    @GetMapping
    public String showSchedulingConfig(Model model) {
        SchedulingConfig config = schedulingService.getSchedulingConfig();
        List<SchedulingLog> logs = schedulingService.getRecentSchedulingLogs();

        model.addAttribute("schedulingConfig", config);
        model.addAttribute("schedulingLogs", logs);

        return "admin_dashboard";
    }

    /**
     * Save scheduling configuration
     */
    @PostMapping("/save")
    public String saveSchedulingConfig(
            @RequestParam(required = false, defaultValue = "false") boolean attendanceSchedulingEnabled,
            @RequestParam(required = false, defaultValue = "10") int minutesBefore,
            @RequestParam(required = false, defaultValue = "5") int minutesAfter,
            @RequestParam(required = false, defaultValue = "17:00") String dailyReportTime,
            @RequestParam(required = false, defaultValue = "Monday") String weeklyReportDay,
            @RequestParam(required = false, defaultValue = "10:00") String weeklyReportTime,
            @RequestParam(required = false, defaultValue = "1") String monthlyReportDay,
            @RequestParam(required = false, defaultValue = "12:00") String monthlyReportTime,
            @RequestParam(required = false, defaultValue = "false") boolean emailNotificationsEnabled,
            RedirectAttributes redirectAttributes) {

        try {
            SchedulingConfig config = new SchedulingConfig();
            config.setEnabled(attendanceSchedulingEnabled);
            config.setMinutesBefore(minutesBefore);
            config.setMinutesAfter(minutesAfter);
            config.setDailyReportTime(dailyReportTime);
            config.setWeeklyReportDay(weeklyReportDay);
            config.setWeeklyReportTime(weeklyReportTime);
            config.setMonthlyReportDay(monthlyReportDay);
            config.setMonthlyReportTime(monthlyReportTime);
            config.setEmailNotificationsEnabled(emailNotificationsEnabled);

            // Generate cron expressions based on configuration
            config.setAttendanceCronExpression(schedulingService.generateAttendanceCronExpression(config));
            config.setDailyReportCronExpression(schedulingService.generateDailyReportCronExpression(config));
            config.setWeeklyReportCronExpression(schedulingService.generateWeeklyReportCronExpression(config));
            config.setMonthlyReportCronExpression(schedulingService.generateMonthlyReportCronExpression(config));

            schedulingService.saveSchedulingConfig(config);

            redirectAttributes.addFlashAttribute("successMessage", "Scheduling configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving scheduling configuration: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}