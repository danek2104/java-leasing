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
    public String listContracts(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String clientName,
                                @RequestParam(required = false) String brand,
                                @RequestParam(required = false) String modelName,
                                @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
                                @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
                                @RequestParam(required = false) java.math.BigDecimal minAmount,
                                @RequestParam(required = false) java.math.BigDecimal maxAmount,
                                @RequestParam(required = false) com.leasing.system.model.ContractStatus status,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "id") String sortField,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        
        org.springframework.data.domain.Page<Contract> contractPage;

        if (user.getRole() == Role.CLIENT) {
            // Найти клиента по пользователю
             var client = clientService.findByUserId(user.getId());
             if (client != null) {
                 contractPage = contractService.findByClientId(client.getId(), page, size, sortField, sortDir);
             } else {
                 contractPage = org.springframework.data.domain.Page.empty();
             }
        } else {
            if (search != null && !search.isEmpty()) {
                contractPage = contractService.search(search, page, size, sortField, sortDir);
                model.addAttribute("search", search);
            } else if (clientName != null || brand != null || modelName != null || startDate != null || endDate != null || minAmount != null || maxAmount != null || status != null) {
                contractPage = contractService.filter(clientName, brand, modelName, startDate, endDate, minAmount, maxAmount, status, page, size, sortField, sortDir);
                model.addAttribute("clientName", clientName);
                model.addAttribute("brand", brand);
                model.addAttribute("modelName", modelName);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("minAmount", minAmount);
                model.addAttribute("maxAmount", maxAmount);
                model.addAttribute("status", status);
            } else {
                contractPage = contractService.findAll(page, size, sortField, sortDir);
            }
        }
        
        model.addAttribute("contracts", contractPage.getContent());
        model.addAttribute("currentPage", contractPage.getNumber());
        model.addAttribute("totalPages", contractPage.getTotalPages());
        model.addAttribute("totalItems", contractPage.getTotalElements());
        
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

        // Проверка даты
        if (contractForm.getStartDate() != null && contractForm.getEndDate() != null) {
            if (!contractForm.getEndDate().isAfter(contractForm.getStartDate())) {
                bindingResult.rejectValue("endDate", "error.contract", "Дата окончания должна быть позже даты начала");
            }
        }
        
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors found: " + bindingResult.getAllErrors());
            model.addAttribute("clients", clientService.findAll());
            // В случае ошибки нам в идеале следует воссоздать правильный контекст списка автомобилей.
            // Для простоты мы просто показываем все или пытаемся восстановить. 
            // Лучше: показать ДОСТУПНЫЕ + тот, который выбран в форме.
            var available = vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
            // Если пользователь выбрал что-то недоступное (например, взломав форму), мы можем не увидеть это в списке, но это нормально.
            // При редактировании нам нужно убедиться, что текущий автомобиль присутствует.
            if (contractForm.getVehicle() != null && contractForm.getVehicle().getId() != null) {
                 vehicleService.findById(contractForm.getVehicle().getId()).ifPresent(v -> {
                     if (!available.contains(v)) available.add(v);
                 });
            }
            model.addAttribute("vehicles", available);
            return "contracts/form";
        }
        
        // Валидация: Проверить, ДОСТУПЕН ли выбранный автомобиль (если это не тот же самый, который редактируется)
        Vehicle selectedVehicle = vehicleService.findById(contractForm.getVehicle().getId()).orElse(null);
        if (selectedVehicle == null) {
             bindingResult.rejectValue("vehicle", "error.contract", "Автомобиль не найден");
             model.addAttribute("clients", clientService.findAll());
             model.addAttribute("vehicles", vehicleService.findAll()); // Резервный вариант
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
             // Заново заполнить правильный список
             var list = vehicleService.findByStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
             if (isSameVehicle) list.add(selectedVehicle); // здесь этого не произойдет, но логическая согласованность
             model.addAttribute("vehicles", list);
             return "contracts/form";
        }

        if (contractForm.getId() != null) {
            // РЕЖИМ РЕДАКТИРОВАНИЯ
            Contract existingContract = contractService.findById(contractForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + contractForm.getId()));

            // 1. Обновить простые поля
            existingContract.setStartDate(contractForm.getStartDate());
            existingContract.setEndDate(contractForm.getEndDate());
            existingContract.setAmount(contractForm.getAmount());

            // 2. Обработка изменения автомобиля
            // Если автомобиль изменился, старый автомобиль должен стать ДОСТУПНЫМ
            if (!existingContract.getVehicle().getId().equals(contractForm.getVehicle().getId())) {
                 Vehicle oldVehicle = existingContract.getVehicle();
                 oldVehicle.setStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
                 vehicleService.save(oldVehicle);
                 
                 // Получить новый автомобиль
                 Vehicle newVehicle = vehicleService.findById(contractForm.getVehicle().getId())
                         .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle Id"));
                 existingContract.setVehicle(newVehicle);
            }
            
            // 3. Логика обработки изменения статуса
            System.out.println("Updating contract status from " + existingContract.getStatus() + " to " + contractForm.getStatus());
            existingContract.setStatus(contractForm.getStatus());
            
            // Повторно получить автомобиль, чтобы быть абсолютно уверенными, что у нас есть последнее состояние и прикрепленная сущность
            Vehicle currentVehicle = vehicleService.findById(existingContract.getVehicle().getId()).orElseThrow();
            System.out.println("Current vehicle status before update: " + currentVehicle.getStatus());
            
            if (existingContract.getStatus() == com.leasing.system.model.ContractStatus.ACTIVE) {
                currentVehicle.setStatus(com.leasing.system.model.VehicleStatus.LEASED);
            } else {
                // ЗАКРЫТ или РАСТОРГНУТ
                currentVehicle.setStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
            }
            
            System.out.println("Setting vehicle status to: " + currentVehicle.getStatus());
            vehicleService.save(currentVehicle);
            
            // 4. Обновить клиента (если изменился)
            if (!existingContract.getClient().getId().equals(contractForm.getClient().getId())) {
                 // Получить реальную сущность клиента по ID
                 Client newClient = clientService.findById(contractForm.getClient().getId())
                         .orElseThrow(() -> new IllegalArgumentException("Invalid client Id"));
                 existingContract.setClient(newClient);
            }

            contractService.save(existingContract);

        } else {
            // РЕЖИМ СОЗДАНИЯ
            // Нужно получить реальные объекты, чтобы синхронизация статусов работала
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
        // Проверить, есть ли текущий уже в списке (equals обычно использует ID, но по умолчанию может быть ссылка на объект)
        // Лучше проверить по ID
        boolean exists = vehicles.stream().anyMatch(v -> v.getId().equals(current.getId()));
        if (!exists) {
            vehicles.add(current);
        }
        
        vehicles.sort(java.util.Comparator.comparing(v -> v.getBrand() + " " + v.getModel()));
        
        model.addAttribute("vehicles", vehicles);
        return "contracts/form";
    }

    @PostMapping("/delete/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteContract(@PathVariable Long id) {
        contractService.deleteById(id);
        return "redirect:/contracts";
    }
}
