package com.leasing.system.service;

import com.leasing.system.model.Request;
import com.leasing.system.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    public List<Request> findByClientId(Long clientId) {
        return requestRepository.findByClientId(clientId);
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }
    
    public void deleteById(Long id) {
        requestRepository.deleteById(id);
    }
}
