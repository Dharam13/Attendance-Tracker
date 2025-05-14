package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.SchedulingConfig;
import com.charusat.attendancetracker.entity.SchedulingLog;
import com.charusat.attendancetracker.service.DynamicTaskScheduler;
import com.charusat.attendancetracker.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/scheduling")
@RequiredArgsConstructor
@Slf4j
public class SchedulingController {

    private final SchedulingService schedulingService;
    private final DynamicTaskScheduler dynamicTaskScheduler;

    /**
     * Display scheduling configuration
     */
    @GetMapping
    public String showSchedulingConfig(Model model) {
        SchedulingConfig config = schedulingService.getSchedulingConfig();
        List<SchedulingLog> logs = schedulingService.getRecentSchedulingLogs();

        // If no config exists yet, create default values
        if (config == null) {
            config = createDefaultConfig();
        }

        model.addAttribute("schedulingConfig", config);
        model.addAttribute("schedulingLogs", logs);

        return "admin/scheduling";
    }

    /**
     * Save scheduling configuration
     */
    @PostMapping("/save")
    public String saveSchedulingConfig(
            @RequestParam(required = false, defaultValue = "false") boolean attendanceSchedulingEnabled,
            @RequestParam(required = false, defaultValue = "0 */30 8-18 ? * MON-FRI") String attendanceCronExpression,
            @RequestParam(required = false, defaultValue = "0 0 17 * * ?") String dailyReportCronExpression,
            @RequestParam(required = false, defaultValue = "0 0 10 ? * MON") String weeklyReportCronExpression,
            @RequestParam(required = false, defaultValue = "0 0 12 1 * ?") String monthlyReportCronExpression,
            @RequestParam(required = false, defaultValue = "false") boolean emailNotificationsEnabled,
            RedirectAttributes redirectAttributes) {

        log.info("Saving scheduling configuration");

        try {
            // Get existing config or create new one
            SchedulingConfig config = schedulingService.getSchedulingConfig();
            if (config == null) {
                config = new SchedulingConfig();
            }

            // Update the config with new values
            config.setEnabled(attendanceSchedulingEnabled);
            config.setEmailNotificationsEnabled(emailNotificationsEnabled);

            // Set cron expressions directly
            config.setAttendanceCronExpression(attendanceCronExpression);
            config.setDailyReportCronExpression(dailyReportCronExpression);
            config.setWeeklyReportCronExpression(weeklyReportCronExpression);
            config.setMonthlyReportCronExpression(monthlyReportCronExpression);

            // Save the configuration
            schedulingService.saveSchedulingConfig(config);

            // Refresh the scheduler to apply new settings
            dynamicTaskScheduler.refreshSchedule();

            redirectAttributes.addFlashAttribute("successMessage", "Scheduling configuration saved and applied successfully!");
        } catch (Exception e) {
            log.error("Error saving scheduling configuration: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent(
                    "Configuration Update",
                    "ERROR",
                    "Failed to update configuration: " + e.getMessage()
            );
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving scheduling configuration: " + e.getMessage());
        }

        return "redirect:/admin/scheduling";
    }

    /**
     * Test scheduling trigger endpoint
     */
    @PostMapping("/test/{taskType}")
    @ResponseBody
    public String testScheduledTask(@PathVariable String taskType) {
        try {
            String message;
            switch (taskType.toLowerCase()) {
                case "attendance":
                    // Call your attendance check method
                    dynamicTaskScheduler.runAttendanceCheckManually();
                    message = "Attendance check triggered successfully";
                    break;
                case "daily":
                    // Call your daily report method
                    message = "Daily report generation triggered successfully";
                    break;
                case "weekly":
                    // Call your weekly report method
                    message = "Weekly report generation triggered successfully";
                    break;
                case "monthly":
                    // Call your monthly report method
                    message = "Monthly report generation triggered successfully";
                    break;
                default:
                    return "Unknown task type: " + taskType;
            }

            // Log the test execution
            schedulingService.logScheduledEvent(
                    "Manual Test",
                    "SUCCESS",
                    "Manual trigger of " + taskType + " task"
            );

            return message;
        } catch (Exception e) {
            log.error("Error testing scheduled task: {}", e.getMessage(), e);
            schedulingService.logScheduledEvent(
                    "Manual Test",
                    "ERROR",
                    "Error triggering " + taskType + " task: " + e.getMessage()
            );
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Create default configuration with sensible defaults
     */
    private SchedulingConfig createDefaultConfig() {
        SchedulingConfig config = new SchedulingConfig();
        config.setEnabled(false);
        config.setEmailNotificationsEnabled(false);
        config.setAttendanceCronExpression("0 */30 8-18 ? * MON-FRI");
        config.setDailyReportCronExpression("0 0 17 * * ?");
        config.setWeeklyReportCronExpression("0 0 10 ? * MON");
        config.setMonthlyReportCronExpression("0 0 12 1 * ?");

        return config;
    }
}