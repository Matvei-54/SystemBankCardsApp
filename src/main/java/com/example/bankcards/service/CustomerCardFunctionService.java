package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.CardEntity;

import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.entity.enums.TransactionType;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.entity.mapper.TransactionMapper;
import com.example.bankcards.entity.operations.TransactionEntity;
import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.customer.*;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.TransactionEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerCardFunctionService {

    private final CardEntityRepository cardEntityRepository;
    private final CustomerService customerService;
    private final TransactionEntityRepository transactionEntityRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;
    private final IdempotencyService idempotencyService;

    @Transactional(readOnly = true)
    public Page<CardResponseDTO> getCustomerCards(String email, CardStatus status, int page, int size) {

        Long idCustomer = customerService.findCustomerByEmail(email)
                .orElseThrow(()-> new CustomerNotFoundException(email)).getId();

        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.ASC,"createdAt"));
        if(status != null){
            return cardEntityRepository.findByCustomerIdAndStatus(idCustomer, status, pageable)
                    .map(cardMapper::toCardResponse);
        }

        return cardEntityRepository.findByCustomerId(idCustomer, pageable).map(cardMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public CardResponseDTO getCustomerCard(String cartNumber, String email) {

        CardEntity cardEntity = cardEntityRepository.findByCardNumber(cartNumber)
                .orElseThrow(()-> new CardWithNumberNoExistsException(cartNumber));

        if(!email.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        return cardMapper.toCardResponse(cardEntity);
    }

    @Transactional
    public String requestCardBlock(BlockCardRequestDTO blockCardDto, String idempotencyKey, String email) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));

        if(!email.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        if (cardEntity.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        cardEntity.setStatus(CardStatus.BLOCKED);
        cardEntityRepository.save(cardEntity);

        String stringResultResponse = "Card has been blocked";
        idempotencyService.saveIdempotencyKey(idempotencyKey, stringResultResponse);
        return stringResultResponse;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionalByCard(ShowTransactionalByCardRequestDTO Dto,
                                                               int page, int size, String idempotencyKey, String email) {
        CardEntity cardEntity = cardEntityRepository.findByCardNumber(Dto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(Dto.cardNumber()));

        if(!email.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));

        List<TransactionResponseDTO> responses = transactionEntityRepository.findBySourceCard(cardEntity, pageable)
                .stream().map(transactionMapper::toTransactionResponse).toList();

        idempotencyService.saveIdempotencyKey(idempotencyKey, responses);
        return responses;
    }


    @Transactional
    public TransactionResponseDTO transferBetweenCards(TransferFundsBetweenUserCardsRequestDTO transferFundsDto,
                                                       String idempotencyKey, String email) {

        CardEntity cardEntityFrom = cardEntityRepository.findByCardNumberWithLock(transferFundsDto.fromCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.fromCardNumber()));

        CardEntity cardEntityTo = cardEntityRepository.findByCardNumberWithLock(transferFundsDto.toCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.fromCardNumber()));

        if(!email.equals(cardEntityFrom.getCustomerEntity().getEmail())){
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
        TransactionResponseDTO response = transactionMapper.toTransactionResponse(transactionEntityRepository.save(transferTransactionEntity));

        idempotencyService.saveIdempotencyKey(idempotencyKey, response);
        return response;

    }

    @Transactional
    public TransactionResponseDTO withdrawalFromCard(WithdrawFundsRequestDTO withdrawDto, String idempotencyKey, String email){
        CardEntity cardEntityFrom = cardEntityRepository.findByCardNumberWithLock(withdrawDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(withdrawDto.cardNumber()));

        if(!email.equals(cardEntityFrom.getCustomerEntity().getEmail())){
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

        cardEntityRepository.save(cardEntityFrom);

        TransactionResponseDTO response = transactionMapper.toTransactionResponse(transactionEntityRepository.save(withdrawTransactionEntity));
        idempotencyService.saveIdempotencyKey(idempotencyKey, response);

        return response;

    }

    @Transactional
    public TransactionResponseDTO cardReplenishment(ReplenishmentCardRequestDTO replenishmentCardDto, String idempotencyKey,
                                                    String email) {

        CardEntity cardEntity = cardEntityRepository.findByCardNumberWithLock(replenishmentCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(replenishmentCardDto.cardNumber()));

        log.info("Request email: {}, Card owner email: {}", email, cardEntity.getCustomerEntity().getEmail());
        if(!email.equals(cardEntity.getCustomerEntity().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        cardEntity.setBalance(cardEntity.getBalance().add(replenishmentCardDto.amount()));

        TransactionEntity replenishTransactionEntity = new TransactionEntity();
        replenishTransactionEntity.setSourceCardEntity(cardEntity);
        replenishTransactionEntity.setAmount(replenishmentCardDto.amount());
        replenishTransactionEntity.setTransactionType(TransactionType.CREDIT);
        replenishTransactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
        replenishTransactionEntity.setCurrency("RUB");
        TransactionResponseDTO transactionResponseDTO = transactionMapper
                .toTransactionResponse(transactionEntityRepository.save(replenishTransactionEntity));

        replenishTransactionEntity.getTransactionStatus().toString();

        cardEntityRepository.save(cardEntity);

        idempotencyService.saveIdempotencyKey(idempotencyKey, transactionResponseDTO);
        return transactionResponseDTO;
    }
}
