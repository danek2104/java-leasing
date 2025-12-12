package com.leasing.system.service;

import com.leasing.system.model.Contract;
import com.leasing.system.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    public List<Contract> findByClientId(Long clientId) {
        return contractRepository.findByClientId(clientId);
    }

    public Contract save(Contract contract) {
        return contractRepository.save(contract);
    }

    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }
    
    public void deleteById(Long id) {
        contractRepository.deleteById(id);
    }
}
