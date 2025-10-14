package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.*;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.entity.mapper.TransactionMapper;
import com.example.bankcards.exception.card.CardWithNumberAlreadyExistsException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardNumberEncryptorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCardFunctionService {

    private final CardRepository cardRepository;
    private final CustomerService customerService;
    private final CardMapper cardMapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final IdempotencyService idempotencyService;
    private final CardNumberEncryptorUtil cardEncryptorUtil;


    @Transactional
    public CardResponseDTO createCard(CreateCardRequestDTO createCardDto, String idempotencyKey) {

        if(cardRepository.findByCardNumber(createCardDto.cardNumber()).isPresent()) {
            throw new CardWithNumberAlreadyExistsException(createCardDto.cardNumber());
        }

        CustomerEntity customer = customerService.findCustomerByEmail(createCardDto.cardOwner())
                .orElseThrow(()-> new CustomerNotFoundException(createCardDto.cardOwner()));

        CardEntity cardEntity = new CardEntity();

        cardEntity.setCardNumber(createCardDto.cardNumber());

        cardEntity.setCustomerEntity(customer);

        cardEntity.setExpiryDate(createCardDto.expiryDate());
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntity.setBalance(BigDecimal.ZERO);
        cardEntity.setCurrency("RUB");

        cardEntity = cardRepository.save(cardEntity);

        CardResponseDTO response = cardMapper.toCardResponse(cardEntity);
        idempotencyService.saveIdempotencyKey(idempotencyKey, response);

        return response;
    }

    public CardResponseDTO updateCard(UpdateCardRequestDTO updateDto) {
        CardEntity cardEntity = cardRepository.findByCardNumber(updateDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(updateDto.cardNumber()));

        cardEntity.setCardNumber(updateDto.newCardNumber());
        cardEntity.setExpiryDate(updateDto.newExpiryDate());

        cardEntity = cardRepository.save(cardEntity);

        return cardMapper.toCardResponse(cardEntity);
    }

    @Transactional
    public void blockCard(BlockCardRequestDTO blockCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));
        cardEntity.setStatus(CardStatus.BLOCKED);
        cardRepository.save(cardEntity);
    }

    @Transactional
    public void activateCard(ActivateCardRequestDTO activateCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardRepository.findByCardNumber(activateCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(activateCardDto.cardNumber()));
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardRepository.save(cardEntity);
    }

    @Transactional
    public void deleteCard(DeleteCardRequestDTO deleteCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardRepository.findByCardNumber(deleteCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(deleteCardDto.cardNumber()));

        cardRepository.deleteById(cardEntity.getId());
    }

    @Transactional(readOnly = true)
    public List<CardResponseDTO> getAllCards() {
        return cardRepository.findAll().stream().map(cardMapper::toCardResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getCardTransactions(ShowTransactionalByCardRequestDTO cardDto) {
        CardEntity cardEntity = cardRepository.findByCardNumber(cardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(cardDto.cardNumber()));
        return transactionRepository.findBySourceCard(cardEntity).stream().map(transactionMapper::toTransactionResponse).toList();
    }
}
