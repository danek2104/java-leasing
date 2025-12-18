package com.leasing.system.controller;

import com.leasing.system.model.Client;
import com.leasing.system.service.ClientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String listClients(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String fullName,
                              @RequestParam(required = false) String passport,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortField,
                              @RequestParam(defaultValue = "asc") String sortDir,
                              Model model) {
        
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        org.springframework.data.domain.Page<Client> clientPage;

        if (fullName != null && !fullName.isEmpty() || passport != null && !passport.isEmpty()) {
            clientPage = clientService.filter(fullName, passport, page, size, sortField, sortDir);
            model.addAttribute("fullName", fullName);
            model.addAttribute("passport", passport);
        } else if (search != null && !search.isEmpty()) {
            clientPage = clientService.search(search, page, size, sortField, sortDir);
            model.addAttribute("search", search);
        } else {
            clientPage = clientService.findAll(page, size, sortField, sortDir);
        }
        
        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", clientPage.getNumber());
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalItems", clientPage.getTotalElements());

        return "clients/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createClientForm(Model model) {
        model.addAttribute("client", new Client());
        return "clients/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String saveClient(@jakarta.validation.Valid @ModelAttribute Client client, org.springframework.validation.BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "clients/form";
        }
        clientService.save(client);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String editClientForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.findById(id).orElseThrow());
        return "clients/form";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteById(id);
        return "redirect:/clients";
    }
}
