package com.leasing.system.service;

import com.leasing.system.model.Request;
import com.leasing.system.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public Page<Request> findAll(int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return requestRepository.findAllByDeletedFalse(pageable);
    }
    
    public List<Request> findAll() {
        return requestRepository.findAllByDeletedFalse();
    }

    public Page<Request> findByClientId(Long clientId, int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return requestRepository.findByClientIdAndDeletedFalse(clientId, pageable);
    }
    
    public List<Request> findByClientId(Long clientId) {
        return requestRepository.findByClientIdAndDeletedFalse(clientId);
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    public Page<Request> search(String query, int page, int size, String sortField, String sortDir) {
        String searchPattern = (query != null && !query.trim().isEmpty()) ? "%" + query.trim() + "%" : null;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return requestRepository.search(searchPattern, pageable);
    }

    public Page<Request> filter(String clientName, String brand, String model, java.time.LocalDate requestDate, com.leasing.system.model.RequestStatus status, int page, int size, String sortField, String sortDir) {
         if ((clientName == null || clientName.isEmpty()) && 
            (brand == null || brand.isEmpty()) && 
            (model == null || model.isEmpty()) && 
            requestDate == null &&
            status == null) {
            return findAll(page, size, sortField, sortDir);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        String clientNamePattern = (clientName != null && !clientName.trim().isEmpty()) ? "%" + clientName.trim() + "%" : null;
        String brandPattern = (brand != null && !brand.trim().isEmpty()) ? "%" + brand.trim() + "%" : null;
        String modelPattern = (model != null && !model.trim().isEmpty()) ? "%" + model.trim() + "%" : null;
        
        return requestRepository.filter(
            clientNamePattern,
            brandPattern,
            modelPattern,
            requestDate,
            status != null ? status.name() : null,
            pageable
        );
    }
    
    public void deleteById(Long id) {
        Request request = requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found"));
        request.setDeleted(true);
        requestRepository.save(request);
    }
}
