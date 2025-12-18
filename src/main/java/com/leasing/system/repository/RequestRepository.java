package com.leasing.system.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leasing.system.model.Request;
import com.leasing.system.model.RequestStatus;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByClientId(Long clientId);
    Page<Request> findByClientId(Long clientId, Pageable pageable);
    
    List<Request> findAllByDeletedFalse();
    Page<Request> findAllByDeletedFalse(Pageable pageable);
    
    List<Request> findByClientIdAndDeletedFalse(Long clientId);
    Page<Request> findByClientIdAndDeletedFalse(Long clientId, Pageable pageable);

    long countByStatus(RequestStatus status);
    long countByClientIdAndStatus(Long clientId, RequestStatus status);

    @Query(value = "SELECT r.* FROM requests r " +
           "LEFT JOIN clients cl ON r.client_id = cl.id " +
           "LEFT JOIN vehicles v ON r.vehicle_id = v.id " +
           "WHERE r.is_deleted = false AND " +
           "(cl.full_name ILIKE :query OR v.brand ILIKE :query OR v.model ILIKE :query)", nativeQuery = true)
    Page<Request> search(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT r.* FROM requests r " +
           "LEFT JOIN clients cl ON r.client_id = cl.id " +
           "LEFT JOIN vehicles v ON r.vehicle_id = v.id " +
           "WHERE r.is_deleted = false AND " +
           "(CAST(:clientName AS text) IS NULL OR cl.full_name ILIKE :clientName) AND " +
           "(CAST(:brand AS text) IS NULL OR v.brand ILIKE :brand) AND " +
           "(CAST(:model AS text) IS NULL OR v.model ILIKE :model) AND " +
           "(CAST(:requestDate AS date) IS NULL OR r.request_date = CAST(:requestDate AS date)) AND " +
           "(CAST(:status AS text) IS NULL OR r.status = :status)", nativeQuery = true)
    Page<Request> filter(@Param("clientName") String clientName,
                         @Param("brand") String brand,
                         @Param("model") String model,
                         @Param("requestDate") java.time.LocalDate requestDate,
                         @Param("status") String status,
                         Pageable pageable);
}
