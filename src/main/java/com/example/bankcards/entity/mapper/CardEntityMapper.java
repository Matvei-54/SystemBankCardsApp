package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CardEntityMapper {

    @Mapping(target = "cardHolder", expression = "java(cardEntity.getCustomerEntity().getName())")
    @Mapping(target = "cardNumber", source = "cardNumber", qualifiedByName = "convertCardNumberToMask")
    @Mapping(target = "currency", expression = "java(cardEntity.getCurrency().toString())")
    CardResponseDTO toCardResponse(CardEntity cardEntity);


    @Named("convertCardNumberToMask")
    default String convertCardNumberToMask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return cardNumber;
        String lastFourDigit = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigit;
    }
}
