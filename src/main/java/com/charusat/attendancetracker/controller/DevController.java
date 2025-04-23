package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.repository.UserRepository;
import com.charusat.attendancetracker.service.AttendanceScraperService;
import com.charusat.attendancetracker.service.MockDataGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Slf4j
public class DevController {
    private final AttendanceScraperService attendanceScraperService;
    private final UserRepository userRepository;
    private final MockDataGenerator mockDataGenerator;

    // Track ongoing scrape jobs
    private final Map<Long, Boolean> scrapingJobs = new ConcurrentHashMap<>();

    @PostMapping("/generate-mock-data")
    public ResponseEntity<?> generateMockData() {
        mockDataGenerator.generateMockData();
        return ResponseEntity.ok("Mock data generated successfully");
    }

    @PostMapping("/scrape/{userId}")
    public ResponseEntity<?> scrapeUserData(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if this user is already being scraped
        if (scrapingJobs.getOrDefault(userId, false)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Scraping already in progress for user " + user.getEgovId());
        }

        log.info("Starting scraping process for user ID: {}, eGovID: {}", userId, user.getEgovId());
        scrapingJobs.put(userId, true);

        CompletableFuture.runAsync(() -> {
            try {
                attendanceScraperService.scrapeAttendanceForUser(user);
                log.info("Scraping completed successfully for user: {}", user.getEgovId());
            } catch (Exception e) {
                log.error("Error during scraping for user {}: {}", user.getEgovId(), e.getMessage(), e);
            } finally {
                scrapingJobs.remove(userId);
            }
        });

        return ResponseEntity.ok("Scraping initiated for user " + user.getEgovId());
    }

    @PostMapping("/scrape/all")
    public ResponseEntity<?> scrapeAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Starting scraping process for all {} users", users.size());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Scraping initiated for " + users.size() + " users");
        response.put("userCount", users.size());

        CompletableFuture.runAsync(() -> {
            for (User user : users) {
                try {
                    // Skip if already being scraped
                    if (scrapingJobs.putIfAbsent(user.getId(), true) != null) {
                        log.info("Skipping user {} as scraping is already in progress", user.getEgovId());
                        continue;
                    }

                    log.info("Scraping attendance for user: {}", user.getEgovId());
                    attendanceScraperService.scrapeAttendanceForUser(user);
                    log.info("Scraping completed for user: {}", user.getEgovId());
                } catch (Exception e) {
                    log.error("Error scraping attendance for user {}: {}", user.getEgovId(), e.getMessage(), e);
                } finally {
                    scrapingJobs.remove(user.getId());
                }

                // Add a small delay between users to prevent overwhelming the system
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Completed scraping process for all users");
        });

        return ResponseEntity.ok(response);
    }

    @GetMapping("/scrape/status")
    public ResponseEntity<?> getScrapingStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeJobs", scrapingJobs.size());
        status.put("activeUserIds", scrapingJobs.keySet());

        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getDevStatus() {
        return ResponseEntity.ok("Dev API is operational");
    }
}