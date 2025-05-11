package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.AttendanceRecord;
import com.charusat.attendancetracker.entity.Notification;
import com.charusat.attendancetracker.entity.Subject;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.service.AttendanceScraperService;
import com.charusat.attendancetracker.service.AttendanceService;
import com.charusat.attendancetracker.service.NotificationService;
import com.charusat.attendancetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final NotificationService notificationService;
    private final AttendanceScraperService attendanceScraperService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            User user = optUser.get();
            List<Subject> subjects = attendanceService.getSubjectsForUser(user);
            List<Notification> recentNotifications = notificationService.getRecentNotificationsForUser(user);

            // Calculate statistics for dashboard
            double averageAttendance = subjects.stream()
                    .mapToDouble(Subject::getCurrentAttendance)
                    .average()
                    .orElse(0.0);

            int totalSubjects = subjects.size();

            int belowThresholdCount = (int) subjects.stream()
                    .filter(s -> s.getCurrentAttendance() < user.getAttendanceThreshold())
                    .count();

            int unreadNotifications = (int) recentNotifications.stream()
                    .filter(n -> !n.getSent())
                    .count();

            // Add all needed attributes to the model
            model.addAttribute("user", user);
            model.addAttribute("subjects", subjects);
            model.addAttribute("recentNotifications", recentNotifications);
            model.addAttribute("averageAttendance", averageAttendance);
            model.addAttribute("totalSubjects", totalSubjects);
            model.addAttribute("belowThresholdCount", belowThresholdCount);
            model.addAttribute("unreadNotifications", unreadNotifications);

            return "dashboard";
        }

        return "redirect:/login";
    }

//    @GetMapping("/admin/dashboard")
//    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());
//        if (optUser.isPresent()) {
//            return "admin_dashboard";
//
//        }
//        return "redirect:/login";
//    }


    @PostMapping("/sync-attendance")
    @ResponseBody
    public Map<String, Object> syncAttendance(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> optUser = userService.findByUsername(userDetails.getUsername());
            if (optUser.isPresent()) {
                attendanceScraperService.scrapeAttendanceForUser(optUser.get());
                response.put("success", true);
                response.put("message", "Attendance data synced successfully!");
            } else {
                response.put("success", false);
                response.put("message", "User not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to sync attendance: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            model.addAttribute("user", optUser.get());
            return "settings";
        }

        return "redirect:/login";
    }

    @PostMapping("/settings")
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @ModelAttribute User updatedUser,
                                 @RequestParam(value = "egovPassword", required = false) String newPassword) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            User user = optUser.get();

            user.setName(updatedUser.getName());  // Add this if you want to update the name
            user.setEmail(updatedUser.getEmail());
            user.setAttendanceThreshold(updatedUser.getAttendanceThreshold());
            user.setNotificationsEnabled(updatedUser.getNotificationsEnabled());

            // Only update password if one was provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setEgovPassword(newPassword);
            }

            // Add logging to verify values before saving
            System.out.println("Updating user: " + user.getUsername());
            System.out.println("New Email: " + user.getEmail());
            System.out.println("New Threshold: " + user.getAttendanceThreshold());
            System.out.println("Notifications: " + user.getNotificationsEnabled());

            userService.updateUser(user);
            return "redirect:/settings?success";
        }

        return "redirect:/login";
    }

    @GetMapping("/check-attendance")
    public String checkAttendance(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> optUser = userService.findByUsername(userDetails.getUsername());

        if (optUser.isPresent()) {
            // Trigger manual attendance check
            attendanceScraperService.scrapeAttendanceForUser(optUser.get());
            return "redirect:/dashboard?checked";
        }

        return "redirect:/login";
    }
}