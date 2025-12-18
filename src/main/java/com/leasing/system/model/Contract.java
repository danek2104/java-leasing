package com.leasing.system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Клиент обязателен")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Автомобиль обязателен")
    private Vehicle vehicle;

    @Column(name = "start_date", nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Дата окончания обязательна")
    private LocalDate endDate;

    @Column(nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Сумма обязательна")
    @jakarta.validation.constraints.Min(value = 0, message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    public Contract() {
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
