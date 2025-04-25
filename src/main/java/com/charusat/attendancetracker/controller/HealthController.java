package com.charusat.attendancetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());

        // Check if Chrome is installed
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "google-chrome");
            Process p = pb.start();
            int exitCode = p.waitFor();
            status.put("chrome_installed", exitCode == 0);
        } catch (Exception e) {
            status.put("chrome_installed", false);
            status.put("chrome_error", e.getMessage());
        }

        return ResponseEntity.ok(status);
    }
}