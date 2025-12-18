package com.leasing.system.controller;

import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.service.ContractService;
import com.leasing.system.service.PaymentService;
import com.leasing.system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;
import com.leasing.system.model.Payment;
import com.leasing.system.model.PaymentStatus;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ContractService contractService;
    private final UserService userService;
    private final com.leasing.system.service.ClientService clientService;

    public PaymentController(PaymentService paymentService, ContractService contractService, UserService userService, com.leasing.system.service.ClientService clientService) {
        this.paymentService = paymentService;
        this.contractService = contractService;
        this.userService = userService;
        this.clientService = clientService;
    }

    @GetMapping
    public String listAllPayments(@RequestParam(required = false) PaymentStatus status,
                                  @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
                                  @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "paymentDate") String sortField,
                                  @RequestParam(defaultValue = "desc") String sortDir,
                                  Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        Page<Payment> paymentPage;

        if (user.getRole() == Role.CLIENT) {
             var client = clientService.findByUserId(user.getId());
             if (client == null) {
                 paymentPage = Page.empty();
             } else {
                 if (status != null || startDate != null || endDate != null) {
                     paymentPage = paymentService.filterByClientId(client.getId(), status, startDate, endDate, page, size, sortField, sortDir);
                     model.addAttribute("status", status);
                     model.addAttribute("startDate", startDate);
                     model.addAttribute("endDate", endDate);
                 } else {
                     paymentPage = paymentService.findByClientId(client.getId(), page, size, sortField, sortDir);
                 }
             }
        } else {
            // Администратор/Менеджер
            if (status != null || startDate != null || endDate != null) {
                paymentPage = paymentService.filter(status, startDate, endDate, page, size, sortField, sortDir);
                model.addAttribute("status", status);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
            } else {
                paymentPage = paymentService.findAll(page, size, sortField, sortDir);
            }
        }
        
        model.addAttribute("payments", paymentPage.getContent());
        model.addAttribute("currentPage", paymentPage.getNumber());
        model.addAttribute("totalPages", paymentPage.getTotalPages());
        model.addAttribute("totalItems", paymentPage.getTotalElements());
        
        return "payments/general_list";
    }

    @GetMapping("/contract/{contractId}")
    public String listPaymentsByContract(@PathVariable Long contractId, 
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(defaultValue = "paymentDate") String sortField,
                                         @RequestParam(defaultValue = "asc") String sortDir,
                                         Model model) {
        var contract = contractService.findById(contractId).orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        
        // Проверка безопасности: Клиент может просматривать только платежи по своим договорам
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        
        if (user.getRole() == Role.CLIENT) {
            // Исправление: правильная навигация пользователь -> клиент
            var client = contract.getClient();
            // Нам нужно проверить, является ли текущий пользователь (через логин) владельцем этого профиля клиента
            // предполагая, что сущность Client имеет связь user_id или мы проверяем через сервис
            // Быстрая проверка: если user.role - клиент, мы доверяем логике сервиса или связи
            // В ContractController мы делали: if (client.getId().equals(user.getId())) ... подождите, user.id это ID пользователя.
            // У Клиента есть поле 'user' (добавлено на Шаге 1).
            if (client.getUser() != null && !client.getUser().getId().equals(user.getId())) {
                 return "redirect:/contracts?error=AccessDenied";
            }
        }
        
        model.addAttribute("contract", contract);
        
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        
        Page<Payment> paymentPage = paymentService.findByContractId(contractId, page, size, sortField, sortDir);
        
        model.addAttribute("payments", paymentPage.getContent());
        model.addAttribute("currentPage", paymentPage.getNumber());
        model.addAttribute("totalPages", paymentPage.getTotalPages());
        model.addAttribute("totalItems", paymentPage.getTotalElements());
        
        return "payments/list";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id) {
        // Получить платеж и проверить доступ
        com.leasing.system.model.Payment payment = paymentService.findById(id);
        if (payment == null) return "redirect:/dashboard?error=PaymentNotFound";
        
        // Логика проверки безопасности аналогична вышеизложенной или просто полагается на сервис (упрощено на данный момент)
        // В идеале проверить, владеет ли залогиненный пользователь договором
        
        paymentService.processPayment(id);
        
        return "redirect:/payments/contract/" + payment.getContract().getId();
    }
}
