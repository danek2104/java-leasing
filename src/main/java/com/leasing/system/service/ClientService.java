package com.leasing.system.service;

import com.leasing.system.model.Client;
import com.leasing.system.model.User;
import com.leasing.system.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
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
    
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }
}
