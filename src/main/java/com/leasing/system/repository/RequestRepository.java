package com.leasing.system.repository;

import com.leasing.system.model.Request;
import com.leasing.system.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByClientId(Long clientId);
    long countByStatus(RequestStatus status);
    long countByClientIdAndStatus(Long clientId, RequestStatus status);
}
