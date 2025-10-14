package com.example.bankcards.service;

import com.example.bankcards.entity.CustomerEntity;
import com.example.bankcards.entity.app_class.CustomerDetails;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CustomerEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerDetailService implements UserDetailsService {

    private final CustomerEntityRepository customerEntityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomerEntity customerEntity = customerEntityRepository.findByEmail(username).orElseThrow(()-> new CustomerNotFoundException(username));

        return new CustomerDetails(customerEntity);
    }
}
