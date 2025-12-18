package com.leasing.system.controller;

import com.leasing.system.model.Client;
import com.leasing.system.model.User;
import com.leasing.system.service.ClientService;
import com.leasing.system.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/client")
public class ClientProfileController {

    private final UserService userService;
    private final ClientService clientService;

    public ClientProfileController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewProfile(Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = clientService.findByUserId(user.getId());
        
        if (client == null) {
            client = new Client();
            // Мы пока не сохраняем его, просто показываем пустую форму или предварительно заполненную тем, что нам известно?
            // Пользователь может быть новым.
        }

        model.addAttribute("client", client);
        return "clients/profile";
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public String updateProfile(@ModelAttribute Client clientForm, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client existingClient = clientService.findByUserId(user.getId());

        if (existingClient != null) {
            existingClient.setFullName(clientForm.getFullName());
            existingClient.setPassportData(clientForm.getPassportData());
            existingClient.setContactInfo(clientForm.getContactInfo());
            clientService.save(existingClient);
        } else {
             // Создать новый профиль клиента для этого пользователя
             clientForm.setUser(user);
             clientService.save(clientForm);
        }

        return "redirect:/client/profile?success";
    }
}
