package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.dto.CustomerRegistrationRequestDTO;
import com.example.bankcards.entity.CustomerEntity;
import com.example.bankcards.entity.mapper.CustomerEntityMapper;
import com.example.bankcards.exception.customer.CustomerAlreadyRegisteredException;
import com.example.bankcards.repository.CustomerEntityRepository;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerEntityRepository customerEntityRepository;
    private final CustomerEntityMapper customerEntityMapper;
    private final RoleRepository roleRepository;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    private final IdempotencyService idempotencyService;
    private final long TIME_LIFE_RECORD_DB = 3600;


    /**
     * Метод создание пользователя
     * @return dto зарегистрированного пользователя
     */
    @Transactional
    public CustomerRegistrationResponseDTO registerCustomer(CustomerRegistrationRequestDTO customerDto, String idempotencyKey) {
        if(customerEntityRepository.findByEmail(customerDto.email()).isPresent()) {
            log.error("Customer with email {} already exists", customerDto.email());
            throw new CustomerAlreadyRegisteredException(customerDto.email());
        }

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setEmail(customerDto.email());
        customerEntity.setPassword(argon2PasswordEncoder.encode(customerDto.password()));
        customerEntity.setName(customerDto.name());
        customerEntity.setRoles(Collections.singleton(roleRepository.findByName("USER").get()));
        customerEntity.setAccountNonExpired(true);
        customerEntity.setAccountNonLocked(true);
        customerEntity.setCredentialsNonExpired(true);
        customerEntity.setEnabled(true);
        CustomerRegistrationResponseDTO response = customerEntityMapper
                .toCustomerRegistrationResponse(customerEntityRepository.save(customerEntity));
        idempotencyService.saveIdempotencyKey(idempotencyKey, response);
        return response;
    }

    @Transactional(readOnly = true)
    public Optional<CustomerEntity> findCustomerByEmail(String email) {
        return customerEntityRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerEntity> findCustomerById(Long id) {
        return customerEntityRepository.findById(id);
    }

}
