package com.leasing.system.repository;

import com.leasing.system.model.Payment;
import com.leasing.system.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByContractId(Long contractId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.contract c WHERE c.client.id = :clientId AND p.status = :status")
    BigDecimal sumByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") PaymentStatus status);
}
