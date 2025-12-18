package com.leasing.system.config;

import com.leasing.system.model.Client;
import com.leasing.system.model.Contract;
import com.leasing.system.model.ContractStatus;
import com.leasing.system.model.Request;
import com.leasing.system.model.RequestStatus;
import com.leasing.system.model.Role;
import com.leasing.system.model.User;
import com.leasing.system.model.Vehicle;
import com.leasing.system.model.VehicleStatus;
import com.leasing.system.repository.ClientRepository;
import com.leasing.system.repository.ContractRepository;
import com.leasing.system.repository.RequestRepository;
import com.leasing.system.repository.UserRepository;
import com.leasing.system.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            ClientRepository clientRepository,
            VehicleRepository vehicleRepository,
            ContractRepository contractRepository,
            RequestRepository requestRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Пользователи
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }
            if (userRepository.findByUsername("manager").isEmpty()) {
                User manager = new User();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager"));
                manager.setRole(Role.MANAGER);
                userRepository.save(manager);
            }
            
            Client clientProfile = null;
            if (userRepository.findByUsername("client").isEmpty()) {
                User userClient = new User();
                userClient.setUsername("client");
                userClient.setPassword(passwordEncoder.encode("client"));
                userClient.setRole(Role.CLIENT);
                userClient = userRepository.save(userClient);
                
                clientProfile = new Client();
                clientProfile.setFullName("Ivan Ivanov");
                clientProfile.setPassportData("1234 567890");
                clientProfile.setContactInfo("ivan@example.com");
                clientProfile.setUser(userClient);
                clientProfile = clientRepository.save(clientProfile);
            } else {
                 // Получить существующий профиль клиента, если пользователь существует (для повторных запусков, если ddl-auto обновлен)
                 User u = userRepository.findByUsername("client").get();
                 clientProfile = clientRepository.findByUserId(u.getId()).orElse(null);
            }

            // 2. Автомобили
            if (vehicleRepository.count() == 0) {
                Vehicle v1 = new Vehicle();
                v1.setBrand("Toyota");
                v1.setModel("Camry");
                v1.setCost(new BigDecimal("25000.00"));
                v1.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(v1);

                Vehicle v2 = new Vehicle();
                v2.setBrand("BMW");
                v2.setModel("X5");
                v2.setCost(new BigDecimal("60000.00"));
                v2.setStatus(VehicleStatus.LEASED);
                vehicleRepository.save(v2);

                Vehicle v3 = new Vehicle();
                v3.setBrand("Tesla");
                v3.setModel("Model 3");
                v3.setCost(new BigDecimal("45000.00"));
                v3.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(v3);
                
                // 3. Договоры и Заявки (Связанные с клиентом и автомобилями)
                if (clientProfile != null) {
                    // Договор на BMW X5
                    Contract c1 = new Contract();
                    c1.setClient(clientProfile);
                    c1.setVehicle(v2);
                    c1.setStartDate(LocalDate.now().minusMonths(2));
                    c1.setEndDate(LocalDate.now().plusMonths(10));
                    c1.setAmount(new BigDecimal("60000.00"));
                    c1.setStatus(ContractStatus.ACTIVE);
                    contractRepository.save(c1);

                    // Заявка на Tesla
                    Request r1 = new Request();
                    r1.setClient(clientProfile);
                    r1.setVehicle(v3);
                    r1.setRequestDate(LocalDate.now());
                    r1.setStatus(RequestStatus.PENDING);
                    requestRepository.save(r1);
                }
            }
        };
    }
}