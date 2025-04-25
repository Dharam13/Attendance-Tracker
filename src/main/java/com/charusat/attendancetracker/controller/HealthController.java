package com.charusat.attendancetracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health") // Changed from "/" to "/health"
    public String home() {
        return "Application is running!";
    }
}
