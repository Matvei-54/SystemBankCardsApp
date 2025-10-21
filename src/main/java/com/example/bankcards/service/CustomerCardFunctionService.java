package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.CardEntity;

import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.entity.enums.TransactionType;
import com.example.bankcards.entity.mapper.CardEntityMapper;
import com.example.bankcards.entity.mapper.TransactionEntityMapper;
import com.example.bankcards.entity.operations.TransactionEntity;
import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.customer.*;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.TransactionEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerCardFunctionService {

    private final CardEntityRepository cardEntityRepository;
    private final CustomerService customerService;
    private final TransactionEntityRepository transactionEntityRepository;
    private final CardEntityMapper cardEntityMapper;
    private final TransactionEntityMapper transactionEntityMapper;
    private final IdempotencyService idempotencyService;

    @Transactional(readOnly = true)
    public Page<CardResponseDTO> getCustomerCards(CardStatus status, int page, int size) {

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long idCustomer = customerService.findCustomerByEmail(emailCustomer)
                .orElseThrow(()-> new CustomerNotFoundException(emailCustomer)).getId();

        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.ASC,"createdAt"));
        if(status != null){
            return cardEntityRepository.findByCustomerEntityIdAndStatus(idCustomer, status, pageable)
                    .map(cardEntityMapper::toCardResponse);
        }

        return cardEntityRepository.findByCustomerEntityId(idCustomer, pageable).map(cardEntityMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public CardResponseDTO getCustomerCard(String cartNumber) {

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CardEntity cardEntity = cardEntityRepository.findByCardNumber(cartNumber)
                .orElseThrow(()-> new CardWithNumberNoExistsException(cartNumber));

//        if(!email.equals(cardEntity.getCustomerEntity().getEmail())){
//            throw new NoAccessToOtherDataException();
//        }

        return cardEntityMapper.toCardResponse(cardEntity);
    }

    @Transactional
    public void requestCardBlock(BlockCardRequestDTO blockCardDto, String idempotencyKey) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!emailCustomer.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        if (cardEntity.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        cardEntity.setStatus(CardStatus.BLOCKED);
        cardEntityRepository.save(cardEntity);

    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionalByCard(ShowTransactionalByCardRequestDTO Dto,
                                                               int page, int size, String idempotencyKey) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(Dto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(Dto.cardNumber()));

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!emailCustomer.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));

        return transactionEntityRepository.findBySourceCardEntity(cardEntity, pageable)
                .stream().map(transactionEntityMapper::toTransactionResponse).toList();
    }


    @Cacheable(value = "key:transfer", key = "#idempotencyKey", unless = "#result == null")
    @Transactional
    public TransactionResponseDTO transferBetweenCards(TransferFundsBetweenUserCardsRequestDTO transferFundsDto,
                                                       String idempotencyKey) {

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<String> listCardNumberOrderByBlock = List.of(transferFundsDto.fromCardNumber(), transferFundsDto.toCardNumber());

        CardEntity cardEntityFrom = cardEntityRepository.findByCardNumberWithLock(transferFundsDto.fromCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.fromCardNumber()));

        CardEntity cardEntityTo = cardEntityRepository.findByCardNumberWithLock(transferFundsDto.toCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.toCardNumber()));

        if(!emailCustomer.equals(cardEntityFrom.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        if (cardEntityFrom.getStatus() != CardStatus.ACTIVE || cardEntityTo.getStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException();
        }

        if (cardEntityFrom.getBalance().compareTo(transferFundsDto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        cardEntityFrom.setBalance((cardEntityFrom.getBalance().subtract(transferFundsDto.amount())));
        cardEntityTo.setBalance(cardEntityTo.getBalance().add(transferFundsDto.amount()));

        TransactionEntity transferTransactionEntity = new TransactionEntity();
        transferTransactionEntity.setSourceCardEntity(cardEntityFrom);
        transferTransactionEntity.setTargetCardEntity(cardEntityTo);
        transferTransactionEntity.setAmount(transferFundsDto.amount());
        transferTransactionEntity.setCurrency(transferFundsDto.currency());
        transferTransactionEntity.setTransactionType(TransactionType.TRANSFER);
        transferTransactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);

        cardEntityRepository.save(cardEntityFrom);
        cardEntityRepository.save(cardEntityTo);
        TransactionResponseDTO response = transactionEntityMapper.toTransactionResponse(transactionEntityRepository.save(transferTransactionEntity));

        //idempotencyService.saveIdempotencyKey(idempotencyKey, response);
        return response;

    }

    @Cacheable(value = "key:withdrawal", key = "#idempotencyKey", unless = "#result == null")
    @Transactional
    public TransactionResponseDTO withdrawalFromCard(WithdrawFundsRequestDTO withdrawDto, String idempotencyKey){

        CardEntity cardEntityFrom = cardEntityRepository.findByCardNumberWithLock(withdrawDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(withdrawDto.cardNumber()));

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!emailCustomer.equals(cardEntityFrom.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        BigDecimal amountWithdraw = withdrawDto.amount();

        if(cardEntityFrom.getStatus() != CardStatus.ACTIVE){
            throw new CardBlockedException();
        }

        if (cardEntityFrom.getBalance().compareTo(amountWithdraw) < 0) {
            throw new InsufficientFundsException();
        }

        cardEntityFrom.setBalance(cardEntityFrom.getBalance().subtract(amountWithdraw));

        TransactionEntity withdrawTransactionEntity = new TransactionEntity();
        withdrawTransactionEntity.setSourceCardEntity(cardEntityFrom);
        withdrawTransactionEntity.setAmount(withdrawDto.amount());
        withdrawTransactionEntity.setCurrency(withdrawDto.currency());
        withdrawTransactionEntity.setTransactionType(TransactionType.DEBIT);
        withdrawTransactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);

        withdrawTransactionEntity = transactionEntityRepository.save(withdrawTransactionEntity);

        cardEntityRepository.save(cardEntityFrom);

        return transactionEntityMapper.toTransactionResponse(withdrawTransactionEntity);

    }

    @Cacheable(value = "key:replenishment", key = "#idempotencyKey", unless = "#result == null")
    @Transactional
    public TransactionResponseDTO cardReplenishment(ReplenishmentCardRequestDTO replenishmentCardDto, String idempotencyKey) {

        CardEntity cardEntity = cardEntityRepository.findByCardNumberWithLock(replenishmentCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(replenishmentCardDto.cardNumber()));

        String emailCustomer = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Request email: {}, Card owner email: {}", emailCustomer, cardEntity.getCustomerEntity().getEmail());
        if(!emailCustomer.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        cardEntity.setBalance(cardEntity.getBalance().add(replenishmentCardDto.amount()));

        TransactionEntity replenishTransactionEntity = new TransactionEntity();
        replenishTransactionEntity.setSourceCardEntity(cardEntity);
        replenishTransactionEntity.setAmount(replenishmentCardDto.amount());
        replenishTransactionEntity.setTransactionType(TransactionType.CREDIT);
        replenishTransactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
        replenishTransactionEntity.setCurrency("RUB");

        replenishTransactionEntity.getTransactionStatus().toString();
        replenishTransactionEntity = transactionEntityRepository.save(replenishTransactionEntity);

        TransactionResponseDTO transactionResponseDTO = transactionEntityMapper
                .toTransactionResponse(replenishTransactionEntity);

        cardEntityRepository.save(cardEntity);

        return transactionResponseDTO;
    }
}
