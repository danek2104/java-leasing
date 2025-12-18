package com.leasing.system.repository;

import com.leasing.system.model.Vehicle;
import com.leasing.system.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    long countByStatus(VehicleStatus status);
    List<Vehicle> findByStatus(VehicleStatus status);

    @Query(value = "SELECT * FROM vehicles v WHERE v.brand ILIKE :query OR v.model ILIKE :query", nativeQuery = true)
    Page<Vehicle> search(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM vehicles v WHERE " +
           "(:brand IS NULL OR v.brand ILIKE :brand) AND " +
           "(:model IS NULL OR v.model ILIKE :model) AND " +
           "(:minCost IS NULL OR v.cost >= :minCost) AND " +
           "(:maxCost IS NULL OR v.cost <= :maxCost)", nativeQuery = true)
    Page<Vehicle> filter(@Param("brand") String brand, 
                         @Param("model") String model, 
                         @Param("minCost") java.math.BigDecimal minCost, 
                         @Param("maxCost") java.math.BigDecimal maxCost,
                         Pageable pageable);
}
