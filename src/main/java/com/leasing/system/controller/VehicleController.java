package com.leasing.system.controller;

import com.leasing.system.model.Vehicle;
import com.leasing.system.service.VehicleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String listVehicles(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String brand,
                               @RequestParam(required = false) String modelName,
                               @RequestParam(required = false) java.math.BigDecimal minCost,
                               @RequestParam(required = false) java.math.BigDecimal maxCost,
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
        
        org.springframework.data.domain.Page<Vehicle> vehiclePage;

        if (search != null && !search.isEmpty()) {
            vehiclePage = vehicleService.search(search, page, size, sortField, sortDir);
            model.addAttribute("search", search);
        } else if (brand != null || modelName != null || minCost != null || maxCost != null) {
            vehiclePage = vehicleService.filter(brand, modelName, minCost, maxCost, page, size, sortField, sortDir);
            model.addAttribute("brand", brand);
            model.addAttribute("modelName", modelName);
            model.addAttribute("minCost", minCost);
            model.addAttribute("maxCost", maxCost);
        } else {
            vehiclePage = vehicleService.findAll(page, size, sortField, sortDir);
        }
        
        model.addAttribute("vehicles", vehiclePage.getContent());
        model.addAttribute("currentPage", vehiclePage.getNumber());
        model.addAttribute("totalPages", vehiclePage.getTotalPages());
        model.addAttribute("totalItems", vehiclePage.getTotalElements());

        return "vehicles/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createVehicleForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "vehicles/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String saveVehicle(@jakarta.validation.Valid @ModelAttribute Vehicle vehicle, org.springframework.validation.BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "vehicles/form";
        }
        vehicleService.save(vehicle);
        return "redirect:/vehicles";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String editVehicleForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", vehicleService.findById(id).orElseThrow());
        return "vehicles/form";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return "redirect:/vehicles?error=CannotDeleteInUse";
        }
        return "redirect:/vehicles";
    }
}
