package com.charusat.attendancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private boolean success;
    private String message;
    private UserDto user;
    // You could add JWT token here if implementing token-based auth
    // private String token;
}