package com.leasing.system.controller;

import com.leasing.system.model.Request;
import com.leasing.system.model.Contract;
import com.leasing.system.model.ContractStatus;
import com.leasing.system.model.Request;
import com.leasing.system.model.RequestStatus;
import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.model.VehicleStatus;
import com.leasing.system.service.ClientService;
import com.leasing.system.service.ContractService;
import com.leasing.system.service.PaymentService;
import com.leasing.system.service.RequestService;
import com.leasing.system.service.UserService;
import com.leasing.system.service.VehicleService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final VehicleService vehicleService;
    private final ClientService clientService;
    private final UserService userService;
    private final ContractService contractService;
    private final PaymentService paymentService;

    public RequestController(RequestService requestService, VehicleService vehicleService, ClientService clientService, UserService userService, ContractService contractService, PaymentService paymentService) {
        this.requestService = requestService;
        this.vehicleService = vehicleService;
        this.clientService = clientService;
        this.userService = userService;
        this.contractService = contractService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String listRequests(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        System.out.println("Listing requests for user: " + user.getUsername() + " Role: " + user.getRole());

        if (user.getRole() == Role.CLIENT) {
            var client = clientService.findByUserId(user.getId());
            if (client != null) {
                System.out.println("Client found with ID: " + client.getId());
                var reqs = requestService.findByClientId(client.getId());
                System.out.println("Found " + reqs.size() + " requests for client.");
                model.addAttribute("requests", reqs);
            } else {
                System.out.println("No client profile found for user.");
                model.addAttribute("requests", java.util.List.of());
            }
        } else {
            var reqs = requestService.findAll();
            System.out.println("Admin/Manager: Found " + reqs.size() + " total requests.");
            model.addAttribute("requests", reqs);
        }
        return "requests/list";
    }

    @GetMapping("/new")
    public String createRequestForm(Model model) {
        model.addAttribute("request", new Request());
        // Only show available vehicles
        model.addAttribute("vehicles", vehicleService.findAll().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .collect(java.util.stream.Collectors.toList()));
        return "requests/form";
    }

    @PostMapping
    public String saveRequest(@jakarta.validation.Valid @ModelAttribute Request request, org.springframework.validation.BindingResult bindingResult, Model model) {
        System.out.println("Processing saveRequest...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());

        if (bindingResult.hasErrors()) {
             System.out.println("Validation errors: " + bindingResult.getAllErrors());
             model.addAttribute("vehicles", vehicleService.findAll().stream()
                     .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                     .collect(java.util.stream.Collectors.toList()));
             // Should also add clients if admin
             if (user.getRole() != Role.CLIENT) {
                 model.addAttribute("clients", clientService.findAll());
             }
             return "requests/form";
        }

        if (user.getRole() == Role.CLIENT) {
             var client = clientService.findByUserId(user.getId());
             if (client == null) {
                 System.out.println("Error: Client profile not found for user " + user.getUsername());
                 return "redirect:/dashboard?error=NoClientProfile";
             }
             request.setClient(client);
             request.setRequestDate(LocalDate.now());
             request.setStatus(RequestStatus.PENDING);
        } else {
            // Admin/Manager logic
            if (request.getRequestDate() == null) {
                request.setRequestDate(LocalDate.now());
            }
            if (request.getStatus() == null) {
                request.setStatus(RequestStatus.PENDING);
            }
        }
        
        Request saved = requestService.save(request);
        System.out.println("Request saved with ID: " + saved.getId() + " for client: " + saved.getClient().getFullName());
        
        return "redirect:/requests";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") String statusStr) {
        Request request = requestService.findById(id).orElseThrow();
        RequestStatus newStatus = RequestStatus.valueOf(statusStr);
        
        // Prevent changing already processed requests
        if (request.getStatus() != RequestStatus.PENDING) {
            return "redirect:/requests?error=AlreadyProcessed";
        }

        request.setStatus(newStatus);
        requestService.save(request);

        if (newStatus == RequestStatus.APPROVED) {
            System.out.println("Approving request " + id + " for client " + request.getClient().getId());
            // Create Contract automatically
            Contract contract = new Contract();
            contract.setClient(request.getClient());
            contract.setVehicle(request.getVehicle());
            contract.setStartDate(LocalDate.now());
            contract.setEndDate(LocalDate.now().plusMonths(12)); // Default 1 year
            contract.setAmount(request.getVehicle().getCost()); // Simplified logic
            contract.setStatus(ContractStatus.ACTIVE);
            
            Contract savedContract = contractService.save(contract);
            System.out.println("Created contract " + savedContract.getId() + " for client " + savedContract.getClient().getId());
            
            // Generate Payments
            paymentService.generateSchedule(savedContract);
            
            // Update Vehicle Status
            var vehicle = request.getVehicle();
            vehicle.setStatus(VehicleStatus.LEASED);
            vehicleService.save(vehicle);
        } else if (newStatus == RequestStatus.REJECTED) {
            // Nothing special, vehicle remains AVAILABLE
        }

        return "redirect:/requests";
    }
}
