package com.leasing.system.repository;

import com.leasing.system.model.Vehicle;
import com.leasing.system.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    long countByStatus(VehicleStatus status);
    List<Vehicle> findByStatus(VehicleStatus status);
}
