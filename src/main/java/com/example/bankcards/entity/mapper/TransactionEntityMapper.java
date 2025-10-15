package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.operations.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionEntityMapper {

    @Mapping(target = "statusTransaction", expression = "java(transactionEntity.getTransactionStatus().toString())")
    TransactionResponseDTO toTransactionResponse(TransactionEntity transactionEntity);
}
