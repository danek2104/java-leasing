package com.leasing.system.controller;

import com.leasing.system.dto.UserRegistrationDto;
import com.leasing.system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@jakarta.validation.Valid @ModelAttribute("user") UserRegistrationDto userDto,
                               BindingResult bindingResult,
                               Model model) {
        if (userService.findByUsername(userDto.getUsername()) != null) {
            bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует");
        }

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Пароли не совпадают");
        }

        if (userDto.getRole() == com.leasing.system.model.Role.CLIENT) {
            if (userDto.getPassportData() == null || userDto.getPassportData().trim().isEmpty()) {
                bindingResult.rejectValue("passportData", "error.user", "Паспортные данные обязательны для клиента");
            }
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.registerUser(userDto);
        return "redirect:/login?success";
    }
}
