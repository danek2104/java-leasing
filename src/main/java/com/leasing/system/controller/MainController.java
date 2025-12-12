package com.leasing.system.controller;

import com.leasing.system.model.Client;
import com.leasing.system.model.ContractStatus;
import com.leasing.system.model.PaymentStatus;
import com.leasing.system.model.RequestStatus;
import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.repository.ContractRepository;
import com.leasing.system.repository.PaymentRepository;
import com.leasing.system.repository.RequestRepository;
import com.leasing.system.repository.VehicleRepository;
import com.leasing.system.service.ClientService;
import com.leasing.system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class MainController {

    private final UserService userService;
    private final ClientService clientService;
    private final VehicleRepository vehicleRepository;
    private final ContractRepository contractRepository;
    private final RequestRepository requestRepository;
    private final PaymentRepository paymentRepository;

    public MainController(UserService userService, ClientService clientService, 
                          VehicleRepository vehicleRepository, 
                          ContractRepository contractRepository, 
                          RequestRepository requestRepository,
                          PaymentRepository paymentRepository) {
        this.userService = userService;
        this.clientService = clientService;
        this.vehicleRepository = vehicleRepository;
        this.contractRepository = contractRepository;
        this.requestRepository = requestRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/logout";
        }
        
        model.addAttribute("username", username);
        model.addAttribute("role", user.getRole().name());

        long totalVehicles = vehicleRepository.count();
        long activeContracts = 0;
        long pendingRequests = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal expectedRevenue = BigDecimal.ZERO;

        if (user.getRole() == Role.CLIENT) {
            Client client = clientService.findByUserId(user.getId());
            if (client != null) {
                activeContracts = contractRepository.countByClientIdAndStatus(client.getId(), ContractStatus.ACTIVE);
                pendingRequests = requestRepository.countByClientIdAndStatus(client.getId(), RequestStatus.PENDING);
                totalRevenue = paymentRepository.sumByClientIdAndStatus(client.getId(), PaymentStatus.PAID);
                expectedRevenue = paymentRepository.sumByClientIdAndStatus(client.getId(), PaymentStatus.PENDING);
            }
        } else {
            activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
            pendingRequests = requestRepository.countByStatus(RequestStatus.PENDING);
            totalRevenue = paymentRepository.sumByStatus(PaymentStatus.PAID);
            expectedRevenue = paymentRepository.sumByStatus(PaymentStatus.PENDING);
        }

        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("activeContracts", activeContracts);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("expectedRevenue", expectedRevenue);
        
        return "dashboard";
    }
}