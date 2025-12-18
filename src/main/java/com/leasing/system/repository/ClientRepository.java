package com.leasing.system.repository;

import com.leasing.system.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUserId(Long userId);

    @Query(value = "SELECT * FROM clients c WHERE c.full_name ILIKE :query OR c.passport_data ILIKE :query", nativeQuery = true)
    Page<Client> search(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM clients c WHERE " +
           "(:fullName IS NULL OR c.full_name ILIKE :fullName) AND " +
           "(:passport IS NULL OR c.passport_data ILIKE :passport)", nativeQuery = true)
    Page<Client> filter(@Param("fullName") String fullName, @Param("passport") String passport, Pageable pageable);
}
