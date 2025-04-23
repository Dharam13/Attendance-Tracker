package com.charusat.attendancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Long subjectId;
    private String message;
    private LocalDateTime createdAt;
    private Boolean sent;
    private LocalDateTime sentAt;
}
