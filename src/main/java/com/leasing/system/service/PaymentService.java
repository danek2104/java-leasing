package com.leasing.system.service;

import com.leasing.system.model.Contract;
import com.leasing.system.model.Payment;
import com.leasing.system.model.PaymentStatus;
import com.leasing.system.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> findByContractId(Long contractId) {
        return paymentRepository.findByContractId(contractId);
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional
    public void generateSchedule(Contract contract) {
        // Clear existing payments if any (e.g. on contract update/re-generation)
        // For simplicity, we just add new ones or assume it's called on creation.
        // If contract is updated, handling existing paid payments is complex. 
        // We will assume this is called only once on ACTIVATION.
        
        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        
        // Calculate months duration roughly
        Period period = Period.between(start, end);
        long months = period.toTotalMonths();
        if (months <= 0) months = 1; // Fallback

        BigDecimal monthlyAmount = contract.getAmount().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= months; i++) {
            Payment payment = new Payment();
            payment.setContract(contract);
            payment.setPaymentDate(start.plusMonths(i));
            payment.setAmount(monthlyAmount);
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
        }
    }
    
    public Payment findById(Long id) {
         return paymentRepository.findById(id).orElse(null);
    }
}
