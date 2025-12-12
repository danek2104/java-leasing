package com.leasing.system.controller;

import com.leasing.system.dto.UserRegistrationDto;
import com.leasing.system.model.User;
import com.leasing.system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "users/form";
    }

    @PostMapping
    public String saveUser(@jakarta.validation.Valid @ModelAttribute("user") UserRegistrationDto userDto,
                           BindingResult bindingResult,
                           Model model) {
        if (userService.findByUsername(userDto.getUsername()) != null) {
            bindingResult.rejectValue("username", "error.user", "Пользователь с таким именем уже существует");
        }
        // Basic validation matching AuthController logic
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Пароли не совпадают");
        }

        if (userDto.getRole() == com.leasing.system.model.Role.CLIENT) {
            if (userDto.getPassportData() == null || userDto.getPassportData().trim().isEmpty()) {
                bindingResult.rejectValue("passportData", "error.user", "Паспортные данные обязательны для клиента");
            }
        }

        if (bindingResult.hasErrors()) {
            return "users/form";
        }

        userService.registerUser(userDto);
        return "redirect:/users";
    }
}
