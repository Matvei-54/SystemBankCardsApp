package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.entity.enums.*;
import com.example.bankcards.entity.mapper.*;
import com.example.bankcards.entity.operations.*;
import com.example.bankcards.exception.card.*;
import com.example.bankcards.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerEntityCardEntityFunctionServiceTest {

//    @Mock
//    private CardEntityRepository cardEntityRepository;
//    @Mock
//    private CustomerService customerService;
//    @Mock
//    private TransactionEntityRepository transactionEntityRepository;
//    @Mock
//    private CardMapper cardMapper;
//    @Mock
//    private TransactionMapper transactionMapper;
//    @Mock
//    private IdempotencyService idempotencyService;
//    @Mock
//    private SecurityContext securityContext;
//    @Mock
//    private Authentication authentication;
//
//    @InjectMocks
//    private CustomerCardFunctionService service;
//
//    private CustomerEntity customerEntity;
//    private CardEntity cardEntity;
//    private TransactionEntity transactionEntity;
//    private CardResponseDTO cardResponseDTO;
//    private TransactionResponseDTO transactionResponseDTO;
//    private String customerEmail = "customer@gmail.com";
//
//    @BeforeEach
//    void setUp() {
//        customerEntity = new CustomerEntity();
//        customerEntity.setId(1L);
//        customerEntity.setEmail(customerEmail);
//
//        cardEntity = new CardEntity();
//        cardEntity.setId(1L);
//        cardEntity.setCustomerEntity(customerEntity);
//        cardEntity.setCardNumber("1234567890123456");
//        cardEntity.setStatus(CardStatus.ACTIVE);
//        cardEntity.setBalance(new BigDecimal("1000.00"));
//
//        transactionEntity = new TransactionEntity();
//        transactionEntity.setId(1L);
//        transactionEntity.setSourceCardEntity(cardEntity);
//        transactionEntity.setAmount(new BigDecimal("100.00"));
//        transactionEntity.setCurrency("RUB");
//        transactionEntity.setTransactionType(TransactionType.TRANSFER);
//        transactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
//
//        cardResponseDTO = new CardResponseDTO();
//        transactionResponseDTO = new TransactionResponseDTO();
//
//    }
//
//    @DisplayName("Вывести список карт пользователя.")
//    @Test
//    void getCustomerCards_Success() {
//        Long customerId = 1L;
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
//        Page<CardEntity> cardPage = new PageImpl<>(List.of(cardEntity));
//
//        when(customerService.findCustomerByEmail(customerEmail)).thenReturn(Optional.of(customerEntity));
//        when(cardEntityRepository.findByCustomerId(customerId, pageable)).thenReturn(cardPage);
//        when(cardMapper.toCardResponse(cardEntity)).thenReturn(cardResponseDTO);
//
//        Page<CardResponseDTO> result = service.getCustomerCards(customerEmail, null, 0, 10);
//
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//        assertEquals(cardResponseDTO, result.getContent().get(0));
//        verify(cardEntityRepository).findByCustomerId(customerId, pageable);
//    }
//
//    @DisplayName("Вывести данные карты пользователя.")
//    @Test
//    void getCustomerCard_Success() {
//        String cardNumber = "1234567890123456";
//        when(cardEntityRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(cardEntity));
//        when(cardMapper.toCardResponse(cardEntity)).thenReturn(cardResponseDTO);
//
//        CardResponseDTO result = service.getCustomerCard(cardNumber, customerEmail);
//
//        assertEquals(cardResponseDTO, result);
//        verify(cardEntityRepository).findByCardNumber(cardNumber);
//    }
//
//    @DisplayName("Карта не найдена.")
//    @Test
//    void getCustomerCard_CardNotFound_ThrowsException() {
//        String cardNumber = "1234567890123456";
//        when(cardEntityRepository.findByCardNumber(cardNumber)).thenReturn(Optional.empty());
//
//        assertThrows(CardWithNumberNoExistsException.class, () ->
//            service.getCustomerCard(cardNumber, customerEmail));
//    }
//
//    @DisplayName("Операци блокировки карты")
//    @Test
//    void requestCardBlock_Success() {
//        String cardNumber = "1234567890123456";
//        BlockCardRequestDTO request = new BlockCardRequestDTO(cardNumber);
//
//        when(cardEntityRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(cardEntity));
//
//        String result = service.requestCardBlock(request, "idemKey", customerEmail);
//
//        assertEquals("Card has been blocked", result);
//        assertEquals(CardStatus.BLOCKED, cardEntity.getStatus());
//        verify(cardEntityRepository).save(cardEntity);
//    }
//
//    @DisplayName("Карта уже заблокирована")
//    @Test
//    void requestCardBlock_AlreadyBlocked_ThrowsException() {
//        String cardNumber = "1234567890123456";
//        cardEntity.setStatus(CardStatus.BLOCKED);
//        BlockCardRequestDTO request = new BlockCardRequestDTO(cardNumber);
//
//        when(cardEntityRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(cardEntity));
//
//        assertThrows(RuntimeException.class, () ->
//            service.requestCardBlock(request, "idemKey", customerEmail));
//    }
//
//    @DisplayName("Вывести список транзакций по карте.")
//    @Test
//    void getTransactionalByCard_Success() {
//        String cardNumber = "1234567890123456";
//        ShowTransactionalByCardRequestDTO request = new ShowTransactionalByCardRequestDTO(cardNumber);
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
//
//        when(cardEntityRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(cardEntity));
//        when(transactionEntityRepository.findBySourceCard(cardEntity, pageable)).thenReturn(List.of(transactionEntity));
//        when(transactionMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponseDTO);
//
//        List<TransactionResponseDTO> result = service.getTransactionalByCard(request, 0, 10, "idemKey", customerEmail);
//
//        assertEquals(1, result.size());
//        assertEquals(transactionResponseDTO, result.get(0));
//    }
//
//    @DisplayName("Операция перевода средств между своими картами.")
//    @Test
//    void transferBetweenCards_Success() {
//        String fromCardNumber = "1234567890123456";
//        String toCardNumber = "9876543210987654";
//        CardEntity cardEntityTo = new CardEntity();
//        cardEntityTo.setId(2L);
//        cardEntityTo.setCustomerEntity(customerEntity);
//        cardEntityTo.setCardNumber(toCardNumber);
//        cardEntityTo.setStatus(CardStatus.ACTIVE);
//        cardEntityTo.setBalance(new BigDecimal("500.00"));
//
//        TransferFundsBetweenUserCardsRequestDTO request = new TransferFundsBetweenUserCardsRequestDTO(
//            fromCardNumber, toCardNumber, new BigDecimal("100.00"), "RUB");
//
//        when(cardEntityRepository.findByCardNumberWithLock(fromCardNumber)).thenReturn(Optional.of(cardEntity));
//        when(cardEntityRepository.findByCardNumberWithLock(toCardNumber)).thenReturn(Optional.of(cardEntityTo));
//        when(transactionEntityRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);
//        when(transactionMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponseDTO);
//
//        TransactionResponseDTO result = service.transferBetweenCards(request, "idemKey", customerEmail);
//
//        assertEquals(transactionResponseDTO, result);
//        assertEquals(new BigDecimal("900.00"), cardEntity.getBalance());
//        assertEquals(new BigDecimal("600.00"), cardEntityTo.getBalance());
//        verify(cardEntityRepository, times(2)).save(any(CardEntity.class));
//        verify(transactionEntityRepository).save(any(TransactionEntity.class));
//    }
//
//    @DisplayName("Вывод средств с карты.")
//    @Test
//    void withdrawalFromCard_Success() {
//        String cardNumber = "1234567890123456";
//        WithdrawFundsRequestDTO request = new WithdrawFundsRequestDTO(cardNumber, new BigDecimal("100.00"), "RUB");
//
//        when(cardEntityRepository.findByCardNumberWithLock(cardNumber)).thenReturn(Optional.of(cardEntity));
//        when(transactionEntityRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);
//        when(transactionMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponseDTO);
//
//        TransactionResponseDTO result = service.withdrawalFromCard(request, "idemKey", customerEmail);
//
//        assertEquals(transactionResponseDTO, result);
//        assertEquals(new BigDecimal("900.00"), cardEntity.getBalance());
//        verify(cardEntityRepository).save(cardEntity);
//        verify(transactionEntityRepository).save(any(TransactionEntity.class));
//    }
//
//    @DisplayName("Операция пополнения карты.")
//    @Test
//    void cardReplenishment_Success() {
//        String cardNumber = "1234567890123456";
//        ReplenishmentCardRequestDTO request = new ReplenishmentCardRequestDTO(cardNumber, new BigDecimal("100.00"));
//
//        when(cardEntityRepository.findByCardNumberWithLock(cardNumber)).thenReturn(Optional.of(cardEntity));
//        when(transactionEntityRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);
//        when(transactionMapper.toTransactionResponse(transactionEntity)).thenReturn(transactionResponseDTO);
//
//        TransactionResponseDTO result = service.cardReplenishment(request, "idemKey", customerEmail);
//
//        assertEquals(transactionResponseDTO, result);
//        assertEquals(new BigDecimal("1100.00"), cardEntity.getBalance());
//        verify(cardEntityRepository).save(cardEntity);
//        verify(transactionEntityRepository).save(any(TransactionEntity.class));
//    }
//
//    @DisplayName("Недостаточно рседств для перервода.")
//    @Test
//    void transferBetweenCards_InsufficientFunds_ThrowsException() {
//        String fromCardNumber = "1234567890123456";
//        String toCardNumber = "9876543210987654";
//        CardEntity cardEntityTo = new CardEntity();
//        cardEntityTo.setId(2L);
//        cardEntityTo.setCustomerEntity(customerEntity);
//        cardEntityTo.setCardNumber(toCardNumber);
//        cardEntityTo.setStatus(CardStatus.ACTIVE);
//        cardEntityTo.setBalance(new BigDecimal("500.00"));
//
//        TransferFundsBetweenUserCardsRequestDTO request = new TransferFundsBetweenUserCardsRequestDTO(
//            fromCardNumber, toCardNumber, new BigDecimal("2000.00"), "RUB");
//
//        when(cardEntityRepository.findByCardNumberWithLock(fromCardNumber)).thenReturn(Optional.of(cardEntity));
//        when(cardEntityRepository.findByCardNumberWithLock(toCardNumber)).thenReturn(Optional.of(cardEntityTo));
//
//        assertThrows(InsufficientFundsException.class, () ->
//            service.transferBetweenCards(request, "idemKey", customerEmail));
//    }
}