package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.dto.UserRegistrationDto;
import com.charusat.attendancetracker.entity.User;
import com.charusat.attendancetracker.mapper.UserMapper;
import com.charusat.attendancetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Convert DTO to entity
            User user = userMapper.toEntity(registrationDto);

            // Register the user
            userService.registerUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/login?registered";
        } catch (Exception e) {
            // Handle exceptions (like duplicate username)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }
}