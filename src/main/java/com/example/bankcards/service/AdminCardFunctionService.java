package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.*;
import com.example.bankcards.entity.enums.*;
import com.example.bankcards.entity.mapper.CardEntityMapper;
import com.example.bankcards.entity.mapper.TransactionEntityMapper;
import com.example.bankcards.exception.card.CardWithNumberAlreadyExistsException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.TransactionEntityRepository;
import com.example.bankcards.util.CardNumberEncryptorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCardFunctionService {

    private final CardEntityRepository cardEntityRepository;
    private final CustomerService customerService;
    private final CardEntityMapper cardEntityMapper;
    private final TransactionEntityRepository transactionEntityRepository;
    private final TransactionEntityMapper transactionEntityMapper;
    private final IdempotencyService idempotencyService;
    private final CardNumberEncryptorUtil cardEncryptorUtil;


    @Cacheable(value = "idempotent:create-card", key = "#idempotencyKey", unless = "#result == null")
    @Transactional
    public CardResponseDTO createCard(CreateCardRequestDTO createCardDto, String idempotencyKey) {

        if(idempotencyService.idempotencyKeyCheck(idempotencyKey)){
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, CardResponseDTO.class);
        }

        if(cardEntityRepository.findByCardNumber(createCardDto.cardNumber()).isPresent()) {
            throw new CardWithNumberAlreadyExistsException(createCardDto.cardNumber());
        }

        CustomerEntity customer = customerService.findCustomerByEmail(createCardDto.cardOwner())
                .orElseThrow(()-> new CustomerNotFoundException(createCardDto.cardOwner()));

        CardEntity cardEntity = CardEntity.builder()
                .cardNumber(createCardDto.cardNumber())
                .customerEntity(customer)
                .expiryDate(createCardDto.expiryDate())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency(Currency.RUB)
                .build();

        cardEntity = cardEntityRepository.save(cardEntity);

        return cardEntityMapper.toCardResponse(cardEntity);
    }

    @Transactional
    public CardResponseDTO updateCard(UpdateCardRequestDTO updateDto) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(updateDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(updateDto.cardNumber()));

        cardEntity.setCardNumber(updateDto.newCardNumber());
        cardEntity.setExpiryDate(updateDto.newExpiryDate());

        cardEntity = cardEntityRepository.save(cardEntity);

        return cardEntityMapper.toCardResponse(cardEntity);
    }

    @Transactional
    public void blockCard(BlockCardRequestDTO blockCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));
        cardEntity.setStatus(CardStatus.BLOCKED);
        cardEntityRepository.save(cardEntity);
    }

    @Transactional
    public void activateCard(ActivateCardRequestDTO activateCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(activateCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(activateCardDto.cardNumber()));
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntityRepository.save(cardEntity);
    }

    @Transactional
    public void deleteCard(DeleteCardRequestDTO deleteCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(deleteCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(deleteCardDto.cardNumber()));

        cardEntityRepository.deleteById(cardEntity.getId());
    }

    @Transactional(readOnly = true)
    public List<CardResponseDTO> getAllCards() {
        return cardEntityRepository.findAll().stream().map(cardEntityMapper::toCardResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getCardTransactions(ShowTransactionalByCardRequestDTO cardDto) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(cardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(cardDto.cardNumber()));

        return cardEntity.getHistory().stream().map(transactionEntityMapper::toTransactionResponse).toList();
    }
}
