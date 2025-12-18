package com.leasing.system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.leasing.system.model.Contract;
import com.leasing.system.repository.ContractRepository;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public Page<Contract> findAll(int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return contractRepository.findAllByDeletedFalse(pageable);
    }

    public List<Contract> findAll() {
        return contractRepository.findAllByDeletedFalse();
    }

    public Page<Contract> findByClientId(Long clientId, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return contractRepository.findByClientIdAndDeletedFalse(clientId, pageable);
    }

    public List<Contract> findByClientId(Long clientId) {
        return contractRepository.findByClientIdAndDeletedFalse(clientId);
    }

    public Contract save(Contract contract) {
        return contractRepository.save(contract);
    }

    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    public Page<Contract> search(String query, int page, int size, String sortField, String sortDir) {
        String searchPattern = (query != null && !query.trim().isEmpty()) ? "%" + query.trim() + "%" : null;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return contractRepository.search(searchPattern, pageable);
    }

    public Page<Contract> filter(String clientName, String brand, String model, java.time.LocalDate startDate, java.time.LocalDate endDate, java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, com.leasing.system.model.ContractStatus status, int page, int size, String sortField, String sortDir) {
        if ((clientName == null || clientName.isEmpty()) && 
            (brand == null || brand.isEmpty()) && 
            (model == null || model.isEmpty()) && 
            startDate == null && endDate == null && 
            minAmount == null && maxAmount == null &&
            status == null) {
            return findAll(page, size, sortField, sortDir);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        String clientNamePattern = (clientName != null && !clientName.trim().isEmpty()) ? "%" + clientName.trim() + "%" : null;
        String brandPattern = (brand != null && !brand.trim().isEmpty()) ? "%" + brand.trim() + "%" : null;
        String modelPattern = (model != null && !model.trim().isEmpty()) ? "%" + model.trim() + "%" : null;

        return contractRepository.filter(
            clientNamePattern,
            brandPattern,
            modelPattern,
            startDate, endDate, minAmount, maxAmount, 
            status != null ? status.name() : null,
            pageable
        );
    }
    
    public void deleteById(Long id) {
        Contract contract = contractRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        contract.setDeleted(true);
        if (contract.getVehicle() != null) {
             contract.getVehicle().setStatus(com.leasing.system.model.VehicleStatus.AVAILABLE);
        }
        contractRepository.save(contract);
    }
}
