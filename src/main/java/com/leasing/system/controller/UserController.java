package com.leasing.system.controller;

import com.leasing.system.dto.UserRegistrationDto;
import com.leasing.system.model.User;
import com.leasing.system.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.leasing.system.dto.UserEditDto;

import com.leasing.system.dto.UserEditDto;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
        // Базовая валидация, соответствующая логике AuthController
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

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        UserEditDto userDto = new UserEditDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setRole(user.getRole());
        model.addAttribute("user", userDto);
        return "users/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") UserEditDto userDto) {
        User user = userService.findById(id);
        user.setUsername(userDto.getUsername());
        user.setRole(userDto.getRole());
        
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(userDto.getPassword());
        }
        
        userDto.setId(id); // Убедиться, что используется ID из пути
        userService.updateUser(userDto);
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }
}
