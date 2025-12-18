package com.leasing.system.repository;

import com.leasing.system.model.Payment;
import com.leasing.system.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByContractId(Long contractId);
    Page<Payment> findByContractId(Long contractId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.contract c WHERE c.client.id = :clientId AND p.status = :status")
    BigDecimal sumByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.paymentDate <= :endDate)")
    Page<Payment> filter(@Param("status") PaymentStatus status, 
                         @Param("startDate") java.time.LocalDate startDate, 
                         @Param("endDate") java.time.LocalDate endDate, 
                         Pageable pageable);

    Page<Payment> findByContract_Client_Id(Long clientId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.contract.client.id = :clientId AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.paymentDate <= :endDate)")
    Page<Payment> filterByClientId(@Param("clientId") Long clientId,
                                   @Param("status") PaymentStatus status, 
                                   @Param("startDate") java.time.LocalDate startDate, 
                                   @Param("endDate") java.time.LocalDate endDate, 
                                   Pageable pageable);
}
