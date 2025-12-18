package com.leasing.system.service;

import com.leasing.system.model.Vehicle;
import com.leasing.system.model.VehicleStatus;
import com.leasing.system.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;



@Service

public class VehicleService {



    private final VehicleRepository vehicleRepository;



    public VehicleService(VehicleRepository vehicleRepository) {

        this.vehicleRepository = vehicleRepository;

    }



    public Page<Vehicle> findAll(int page, int size, String sortField, String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.findAll(pageable);

    }



    public List<Vehicle> findAll() {

        return vehicleRepository.findAll();

    }

    

    public List<Vehicle> findByStatus(VehicleStatus status) {

        return vehicleRepository.findByStatus(status);

    }



    public Vehicle save(Vehicle vehicle) {

        return vehicleRepository.save(vehicle);

    }



    public Optional<Vehicle> findById(Long id) {

        return vehicleRepository.findById(id);

    }



    public Page<Vehicle> search(String query, int page, int size, String sortField, String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.search(query, pageable);

    }



                public Page<Vehicle> filter(String brand, String model, java.math.BigDecimal minCost, java.math.BigDecimal maxCost, int page, int size, String sortField, String sortDir) {



                     if ((brand == null || brand.isEmpty()) && (model == null || model.isEmpty()) && minCost == null && maxCost == null) {



                        return findAll(page, size, sortField, sortDir);



                    }



                    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);



                    Pageable pageable = PageRequest.of(page, size, sort);



                    



                    String brandPattern = (brand != null && !brand.trim().isEmpty()) ? "%" + brand.trim() + "%" : null;



                    String modelPattern = (model != null && !model.trim().isEmpty()) ? "%" + model.trim() + "%" : null;



                    



                    return vehicleRepository.filter(



                        brandPattern,



                        modelPattern,



                        minCost, maxCost,



                        pageable



                    );



                }



    public void deleteById(Long id) {

        vehicleRepository.deleteById(id);

    }

}
