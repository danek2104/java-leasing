package com.leasing.system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leasing.system.model.Contract;
import com.leasing.system.model.Payment;
import com.leasing.system.model.PaymentStatus;
import com.leasing.system.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> findByContractId(Long contractId) {
        return paymentRepository.findByContractId(contractId);
    }

    public Page<Payment> findByContractId(Long contractId, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentRepository.findByContractId(contractId, pageable);
    }
    
    public Page<Payment> filter(PaymentStatus status, LocalDate startDate, LocalDate endDate, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentRepository.filter(status, startDate, endDate, pageable);
    }

    public Page<Payment> findByClientId(Long clientId, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentRepository.findByContract_Client_Id(clientId, pageable);
    }

    public Page<Payment> filterByClientId(Long clientId, PaymentStatus status, LocalDate startDate, LocalDate endDate, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentRepository.filterByClientId(clientId, status, startDate, endDate, pageable);
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
    
    public Page<Payment> findAll(int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentRepository.findAll(pageable);
    }

    @Transactional
    public void generateSchedule(Contract contract) {

        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        
        Period period = Period.between(start, end);
        long months = period.toTotalMonths();
        if (months <= 0) months = 1; 

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
