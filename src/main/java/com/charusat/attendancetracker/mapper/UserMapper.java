
package com.charusat.attendancetracker.mapper;

import com.charusat.attendancetracker.dto.UserRegistrationDto;
import com.charusat.attendancetracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRegistrationDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // password encoding kar lena service me
        user.setName(dto.getName());
        user.setEgovId(dto.getEgovId());
        user.setEgovPassword(dto.getEgovPassword());
        user.setAttendanceThreshold(dto.getAttendanceThreshold());
        user.setNotificationsEnabled(dto.getNotificationsEnabled());
        return user;
    }
}

