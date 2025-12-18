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
    public String listRequests(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String clientName,
                               @RequestParam(required = false) String brand,
                               @RequestParam(required = false) String modelName,
                               @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate requestDate,
                               @RequestParam(required = false) RequestStatus status,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "id") String sortField,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        System.out.println("Listing requests for user: " + user.getUsername() + " Role: " + user.getRole());
        
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        
        org.springframework.data.domain.Page<Request> requestPage;

        if (user.getRole() == Role.CLIENT) {
            var client = clientService.findByUserId(user.getId());
            if (client != null) {
                System.out.println("Client found with ID: " + client.getId());
                requestPage = requestService.findByClientId(client.getId(), page, size, sortField, sortDir);
            } else {
                System.out.println("No client profile found for user.");
                requestPage = org.springframework.data.domain.Page.empty();
            }
        } else {
            if (search != null && !search.isEmpty()) {
                requestPage = requestService.search(search, page, size, sortField, sortDir);
                model.addAttribute("search", search);
            } else if (clientName != null || brand != null || modelName != null || requestDate != null || status != null) {
                requestPage = requestService.filter(clientName, brand, modelName, requestDate, status, page, size, sortField, sortDir);
                model.addAttribute("clientName", clientName);
                model.addAttribute("brand", brand);
                model.addAttribute("modelName", modelName);
                model.addAttribute("requestDate", requestDate);
                model.addAttribute("status", status);
            } else {
                requestPage = requestService.findAll(page, size, sortField, sortDir);
            }
        }
        
        model.addAttribute("requests", requestPage.getContent());
        model.addAttribute("currentPage", requestPage.getNumber());
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("totalItems", requestPage.getTotalElements());

        return "requests/list";
    }

    @GetMapping("/new")
    public String createRequestForm(Model model) {
        model.addAttribute("request", new Request());
        // Показать только доступные автомобили
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
             // Также следует добавить клиентов, если это администратор
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
            // Логика Администратора/Менеджера
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
        
        // Предотвратить изменение уже обработанных заявок
        if (request.getStatus() != RequestStatus.PENDING) {
            return "redirect:/requests?error=AlreadyProcessed";
        }

        request.setStatus(newStatus);
        requestService.save(request);

        if (newStatus == RequestStatus.APPROVED) {
            System.out.println("Approving request " + id + " for client " + request.getClient().getId());
            // Автоматическое создание договора
            Contract contract = new Contract();
            contract.setClient(request.getClient());
            contract.setVehicle(request.getVehicle());
            contract.setStartDate(LocalDate.now());
            contract.setEndDate(LocalDate.now().plusMonths(12)); // По умолчанию 1 год
            contract.setAmount(request.getVehicle().getCost()); // Упрощенная логика
            contract.setStatus(ContractStatus.ACTIVE);
            
            Contract savedContract = contractService.save(contract);
            System.out.println("Created contract " + savedContract.getId() + " for client " + savedContract.getClient().getId());
            
            // Генерировать платежи
            paymentService.generateSchedule(savedContract);
            
            // Обновить статус автомобиля
            var vehicle = request.getVehicle();
            vehicle.setStatus(VehicleStatus.LEASED);
            vehicleService.save(vehicle);
        } else if (newStatus == RequestStatus.REJECTED) {
            // Ничего особенного, автомобиль остается ДОСТУПНЫМ
        }

        return "redirect:/requests";
    }

    @PostMapping("/delete/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteRequest(@PathVariable Long id) {
        requestService.deleteById(id);
        return "redirect:/requests";
    }
}
