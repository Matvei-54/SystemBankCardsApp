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
import org.springframework.cache.annotation.*;
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


    /**
     * Метод создание пользователя
     * @return dto зарегистрированного пользователя
     */
    @Cacheable(value = "key:register", key = "#idempotencyKey", unless = "#result == null")
    @Transactional
    public CustomerRegistrationResponseDTO registerCustomer(CustomerRegistrationRequestDTO customerDto, String idempotencyKey) {
        if(customerEntityRepository.findByEmail(customerDto.email()).isPresent()) {
            log.error("Customer with email {} already exists", customerDto.email());
            throw new CustomerAlreadyRegisteredException(customerDto.email());
        }

        CustomerEntity customerEntity = CustomerEntity.builder()
                        .email(customerDto.email())
                        .password(argon2PasswordEncoder.encode(customerDto.password()))
                        .name(customerDto.name())
                        .roles(Collections.singleton(roleRepository.findByName("USER").get()))
                        .isAccountNonExpired(true)
                        .isAccountNonExpired(true)
                        .isCredentialsNonExpired(true)
                        .isEnabled(true)
                        .build();

        customerEntity = customerEntityRepository.save(customerEntity);

        return customerEntityMapper.toCustomerRegistrationResponse(customerEntity);
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
