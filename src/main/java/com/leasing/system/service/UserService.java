package com.leasing.system.service;

import com.leasing.system.dto.UserRegistrationDto;
import com.leasing.system.model.Client;
import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.repository.ClientRepository;
import com.leasing.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.leasing.system.dto.UserEditDto;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());
        
        user = userRepository.save(user);

        if (registrationDto.getRole() == Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            client.setFullName(registrationDto.getFullName());
            client.setContactInfo(registrationDto.getContactInfo());
            client.setPassportData(registrationDto.getPassportData());
            clientRepository.save(client);
        }
    }

    public User saveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void updateUser(UserEditDto userEditDto) {
        User existingUser = userRepository.findById(userEditDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userEditDto.getId()));

        existingUser.setUsername(userEditDto.getUsername());
        existingUser.setRole(userEditDto.getRole());

        if (userEditDto.getPassword() != null && !userEditDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userEditDto.getPassword()));
        }
        userRepository.save(existingUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
