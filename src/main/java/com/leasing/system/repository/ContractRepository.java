package com.leasing.system.repository;

import com.leasing.system.model.Contract;
import com.leasing.system.model.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByClientId(Long clientId);
    long countByStatus(ContractStatus status);
    long countByClientIdAndStatus(Long clientId, ContractStatus status);
}
