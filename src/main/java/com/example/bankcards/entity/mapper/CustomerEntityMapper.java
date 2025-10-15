package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.CustomerRegistrationResponseDTO;
import com.example.bankcards.entity.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {

    CustomerRegistrationResponseDTO toCustomerRegistrationResponse(CustomerEntity customerEntity);
}
