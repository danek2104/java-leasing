package com.leasing.system.controller;

import com.leasing.system.service.PaymentService;

import com.leasing.system.service.VehicleService;
import com.leasing.system.model.Client;
import com.leasing.system.model.Contract;
import com.leasing.system.model.Vehicle;
import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.service.ClientService;
import com.leasing.system.service.ContractService;
import com.leasing.system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ClientService clientService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final PaymentService paymentService;

    public ContractController(ContractService contractService, ClientService clientService, UserService userService, VehicleService vehicleService, PaymentService paymentService) {
        this.contractService = contractService;
        this.clientService = clientService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String listContracts(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());

        if (user.getRole() == Role.CLIENT) {
            // Find client by user
             var client = clientService.findByUserId(user.getId());
             if (client != null) {
                 model.addAttribute("contracts", contractService.findByClientId(client.getId()));
             } else {
                 model.addAttribute("contracts", java.util.List.of());
             }
        } else {
            model.addAttribute("contracts", contractService.findAll());
        }
        return "contracts/list";
    }

    @GetMapping("/new")
    public String createContractForm(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("vehicles", vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE));
        return "contracts/form";
    }

    @PostMapping
    public String saveContract(@jakarta.validation.Valid @ModelAttribute Contract contractForm, org.springframework.validation.BindingResult bindingResult, Model model) {
        System.out.println("Processing saveContract request. ID: " + contractForm.getId());

        // Date Validation
        if (contractForm.getStartDate() != null && contractForm.getEndDate() != null) {
            if (!contractForm.getEndDate().isAfter(contractForm.getStartDate())) {
                bindingResult.rejectValue("endDate", "error.contract", "Дата окончания должна быть позже даты начала");
            }
        }
        
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors found: " + bindingResult.getAllErrors());
            model.addAttribute("clients", clientService.findAll());
            // In case of error, we should ideally recreate the correct vehicle list context.
            // For simplicity, we just show all or try to reconstruct. 
            // Better: show AVAILABLE + the one selected in form.
            var available = vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
            // If user selected something not available (e.g. by hacking form), we might not see it in list, but that's fine.
            // If editing, we need to ensure current one is there.
            if (contractForm.getVehicle() != null && contractForm.getVehicle().getId() != null) {
                 vehicleService.findById(contractForm.getVehicle().getId()).ifPresent(v -> {
                     if (!available.contains(v)) available.add(v);
                 });
            }
            model.addAttribute("vehicles", available);
            return "contracts/form";
        }
        
        // Validation: Check if selected vehicle is AVAILABLE (unless it's the same one being edited)
        Vehicle selectedVehicle = vehicleService.findById(contractForm.getVehicle().getId()).orElse(null);
        if (selectedVehicle == null) {
             bindingResult.rejectValue("vehicle", "error.contract", "Автомобиль не найден");
             model.addAttribute("clients", clientService.findAll());
             model.addAttribute("vehicles", vehicleService.findAll()); // Fallback
             return "contracts/form";
        }
        
        boolean isSameVehicle = false;
        if (contractForm.getId() != null) {
             Contract existing = contractService.findById(contractForm.getId()).orElse(null);
             if (existing != null && existing.getVehicle().getId().equals(selectedVehicle.getId())) {
                 isSameVehicle = true;
             }
        }
        
        if (!isSameVehicle && selectedVehicle.getStatus() != com.leasing.system.model.VehicleStatus.AVAILABLE) {
             bindingResult.rejectValue("vehicle", "error.contract", "Выбранный автомобиль недоступен для аренды");
             model.addAttribute("clients", clientService.findAll());
             // Re-populate correct list
             var list = vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
             if (isSameVehicle) list.add(selectedVehicle); // won't happen here but logic consistency
             model.addAttribute("vehicles", list);
             return "contracts/form";
        }

        if (contractForm.getId() != null) {
            // EDIT MODE
            Contract existingContract = contractService.findById(contractForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + contractForm.getId()));

            // 1. Update simple fields
            existingContract.setStartDate(contractForm.getStartDate());
            existingContract.setEndDate(contractForm.getEndDate());
            existingContract.setAmount(contractForm.getAmount());

            // 2. Handle Vehicle Change
            // If vehicle changed, old vehicle should become AVAILABLE
            if (!existingContract.getVehicle().getId().equals(contractForm.getVehicle().getId())) {
                 Vehicle oldVehicle = existingContract.getVehicle();
                 oldVehicle.setStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
                 vehicleService.save(oldVehicle);
                 
                 // Fetch new vehicle
                 Vehicle newVehicle = vehicleService.findById(contractForm.getVehicle().getId())
                         .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle Id"));
                 existingContract.setVehicle(newVehicle);
            }
            
            // 3. Handle Status Change Logic
            System.out.println("Updating contract status from " + existingContract.getStatus() + " to " + contractForm.getStatus());
            existingContract.setStatus(contractForm.getStatus());
            
            // Re-fetch vehicle to be absolutely sure we have the latest state and attached entity
            Vehicle currentVehicle = vehicleService.findById(existingContract.getVehicle().getId()).orElseThrow();
            System.out.println("Current vehicle status before update: " + currentVehicle.getStatus());
            
            if (existingContract.getStatus() == com.leasing.system.model.ContractStatus.ACTIVE) {
                currentVehicle.setStatus(com.leasing.system.model.VehicleStatus.LEASED);
            } else {
                // CLOSED or TERMINATED
                currentVehicle.setStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
            }
            
            System.out.println("Setting vehicle status to: " + currentVehicle.getStatus());
            vehicleService.save(currentVehicle);
            
            // 4. Update Client (if changed)
            if (!existingContract.getClient().getId().equals(contractForm.getClient().getId())) {
                 // Fetch real client entity by ID
                 Client newClient = clientService.findById(contractForm.getClient().getId())
                         .orElseThrow(() -> new IllegalArgumentException("Invalid client Id"));
                 existingContract.setClient(newClient);
            }

            contractService.save(existingContract);

        } else {
            // CREATE MODE
            // Need to fetch real objects to ensure status sync works
            if (contractForm.getVehicle() != null && contractForm.getVehicle().getId() != null) {
                Vehicle v = vehicleService.findById(contractForm.getVehicle().getId()).orElse(null);
                if (v != null) {
                    contractForm.setVehicle(v);
                    if (contractForm.getStatus() == com.leasing.system.model.ContractStatus.ACTIVE) {
                        v.setStatus(com.leasing.system.model.VehicleStatus.LEASED);
                        vehicleService.save(v);
                    }
                }
            }
            Contract saved = contractService.save(contractForm);
            if (saved.getStatus() == com.leasing.system.model.ContractStatus.ACTIVE) {
                paymentService.generateSchedule(saved);
            }
        }

        return "redirect:/contracts";
    }
    
    @GetMapping("/edit/{id}")
    public String editContractForm(@PathVariable Long id, Model model) {
        Contract contract = contractService.findById(id).orElseThrow();
        model.addAttribute("contract", contract);
        model.addAttribute("clients", clientService.findAll());
        
        java.util.List<Vehicle> vehicles = vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
        Vehicle current = contract.getVehicle();
        // Check if current is already in list (equals uses ID usually, but default equals might be object ref)
        // Better check by ID
        boolean exists = vehicles.stream().anyMatch(v -> v.getId().equals(current.getId()));
        if (!exists) {
            vehicles.add(current);
        }
        
        vehicles.sort(java.util.Comparator.comparing(v -> v.getBrand() + " " + v.getModel()));
        
        model.addAttribute("vehicles", vehicles);
        return "contracts/form";
    }
}
