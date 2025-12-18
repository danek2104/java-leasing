package com.leasing.system.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leasing.system.model.Contract;
import com.leasing.system.model.ContractStatus;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByClientId(Long clientId);
    Page<Contract> findByClientId(Long clientId, Pageable pageable);
    
    List<Contract> findAllByDeletedFalse();
    Page<Contract> findAllByDeletedFalse(Pageable pageable);
    
    List<Contract> findByClientIdAndDeletedFalse(Long clientId);
    Page<Contract> findByClientIdAndDeletedFalse(Long clientId, Pageable pageable);

    long countByStatus(ContractStatus status);
    long countByClientIdAndStatus(Long clientId, ContractStatus status);

    @Query(value = "SELECT c.* FROM contracts c " +
           "LEFT JOIN clients cl ON c.client_id = cl.id " +
           "LEFT JOIN vehicles v ON c.vehicle_id = v.id " +
           "WHERE c.is_deleted = false AND " +
           "(cl.full_name ILIKE :query OR v.brand ILIKE :query OR v.model ILIKE :query)", nativeQuery = true)
    Page<Contract> search(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT c.* FROM contracts c " +
           "LEFT JOIN clients cl ON c.client_id = cl.id " +
           "LEFT JOIN vehicles v ON c.vehicle_id = v.id " +
           "WHERE c.is_deleted = false AND " +
           "(CAST(:clientName AS text) IS NULL OR cl.full_name ILIKE :clientName) AND " +
           "(CAST(:brand AS text) IS NULL OR v.brand ILIKE :brand) AND " +
           "(CAST(:model AS text) IS NULL OR v.model ILIKE :model) AND " +
           "(CAST(:startDate AS date) IS NULL OR c.start_date >= :startDate) AND " +
           "(CAST(:endDate AS date) IS NULL OR c.end_date <= :endDate) AND " +
           "(CAST(:minAmount AS numeric) IS NULL OR c.amount >= :minAmount) AND " +
           "(CAST(:maxAmount AS numeric) IS NULL OR c.amount <= :maxAmount) AND " +
           "(CAST(:status AS text) IS NULL OR c.status = :status)", nativeQuery = true)
    Page<Contract> filter(@Param("clientName") String clientName,
                          @Param("brand") String brand,
                          @Param("model") String model,
                          @Param("startDate") java.time.LocalDate startDate,
                          @Param("endDate") java.time.LocalDate endDate,
                          @Param("minAmount") java.math.BigDecimal minAmount,
                          @Param("maxAmount") java.math.BigDecimal maxAmount,
                          @Param("status") String status,
                          Pageable pageable);
}
