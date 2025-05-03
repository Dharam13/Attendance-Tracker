package com.charusat.attendancetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLOutput;

@SpringBootApplication
public class AttendanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceTrackerApplication.class, args);

        System.out.println("................Application started Successfully.............");
    }
}
