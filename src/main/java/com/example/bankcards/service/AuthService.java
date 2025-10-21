package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.CustomerEntity;
import com.example.bankcards.entity.app_class.CustomerDetails;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CustomerEntityRepository;
import com.example.bankcards.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomerEntityRepository customerEntityRepository;

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomerEntity customerEntity = customerEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomerNotFoundException(request.email()));

        CustomerDetails customerDetails = new CustomerDetails(customerEntity);

        String token = "jwt-token: " + jwtUtil.generateToken(customerDetails);
        return new AuthResponse(token);
    }

    public long getCustomerId(){

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CustomerEntity customer = customerEntityRepository.findByEmail(emailCustomer)
                .orElseThrow(() -> new CustomerNotFoundException(emailCustomer));

        return customer.getId();
    }
}
