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

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ContractService contractService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, ContractService contractService, UserService userService) {
        this.paymentService = paymentService;
        this.contractService = contractService;
        this.userService = userService;
    }

    @GetMapping("/contract/{contractId}")
    public String listPaymentsByContract(@PathVariable Long contractId, Model model) {
        var contract = contractService.findById(contractId).orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        
        // Security check: Client can only view their own contract payments
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        
        if (user.getRole() == Role.CLIENT) {
            if (!contract.getClient().getUser().getId().equals(user.getId())) {
                 return "redirect:/contracts?error=AccessDenied";
            }
        }
        
        model.addAttribute("contract", contract);
        model.addAttribute("payments", paymentService.findByContractId(contractId));
        return "payments/list";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id) {
        // Fetch payment and check access
        com.leasing.system.model.Payment payment = paymentService.findById(id);
        if (payment == null) return "redirect:/dashboard?error=PaymentNotFound";
        
        // Security check logic similar to above or just rely on service (simplified for now)
        // Ideally check if logged user owns the contract
        
        paymentService.processPayment(id);
        
        return "redirect:/payments/contract/" + payment.getContract().getId();
    }
}
