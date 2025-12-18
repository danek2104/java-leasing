package com.leasing.system.service;

import com.leasing.system.model.Client;
import com.leasing.system.model.User;
import com.leasing.system.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Page<Client> findAll(int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return clientRepository.findAll(pageable);
    }
    
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public Client findByUserId(Long userId) {
        return clientRepository.findByUserId(userId).orElse(null);
    }

    public Page<Client> search(String query, int page, int size, String sortField, String sortDir) {
        String searchPattern = (query != null && !query.trim().isEmpty()) ? "%" + query.trim() + "%" : null;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return clientRepository.search(searchPattern, pageable);
    }

    public Page<Client> filter(String fullName, String passport, int page, int size, String sortField, String sortDir) {
        String fullNamePattern = (fullName != null && !fullName.trim().isEmpty()) ? "%" + fullName.trim() + "%" : null;
        String passportPattern = (passport != null && !passport.trim().isEmpty()) ? "%" + passport.trim() + "%" : null;
        
        if (fullNamePattern == null && passportPattern == null) {
            return findAll(page, size, sortField, sortDir);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return clientRepository.filter(fullNamePattern, passportPattern, pageable);
    }
    
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }
}
