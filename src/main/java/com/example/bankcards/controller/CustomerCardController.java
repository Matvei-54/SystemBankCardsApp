package com.example.bankcards.controller;


import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponseDTO;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CustomerCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Tag(
        name = "Customer Card Management",
        description = "Операции клиента с картами — просмотр, переводы, пополнение, вывод и блокировка."
)
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@RestController
public class CustomerCardController {

    private final CustomerCardService cardFunctionService;

    /**
     * Запрос получений данных карты
     * @param cardNumber номер карты
     * @return dto данных карты
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить данные карты",
            description = "Возвращает детальную информацию по карте по указанному номеру карты.")
    @GetMapping("/get/{cardNumber}")
    public CardResponseDTO getCard(@PathVariable String cardNumber) {

        return cardFunctionService.getCustomerCard(cardNumber);
    }

    /**
     * Запрос на получение всех карт по статусу и пагинацией
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить список карт",
            description = "Возвращает список карт пользователя с возможностью фильтрации по статусу и пагинации.")
    @GetMapping()
    public Page<CardResponseDTO> getCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return cardFunctionService.getCustomerCards(status, page, size);
    }

    /**
     * Запрос перевода средств между своими картами
     * @param transferDto dto c параметрами перевода
     * @param idempotencyKey
     * @return dto транзакции
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Перевести средства между своими картами",
            description = "Выполняет перевод между картами текущего пользователя и возвращает данные транзакции.")
    @PostMapping("/transfer")
    public TransactionResponseDTO transfer(@Valid @RequestBody TransferFundsBetweenUserCardsRequestDTO transferDto,
                                           @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        return cardFunctionService.transferBetweenCards(transferDto, idempotencyKey);
    }

    /**
     * Запрос вывода средств с карты
     * @param withdrawDto dto c параметрами вывода
     * @param idempotencyKey
     * @return dto транзакции
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Вывести средства с карты",
            description = "Выполняет вывод средств с карты пользователя и возвращает данные транзакции.")
    @PostMapping("/withdraw")
    public TransactionResponseDTO withdraw(@Valid @RequestBody WithdrawFundsRequestDTO withdrawDto,
                                           @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        return cardFunctionService.withdrawalFromCard(withdrawDto, idempotencyKey);
    }

    /**
     * Запрос получения всех транзакций по карте
     * @return лист dto траназакций
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить список транзакций по карте",
            description = "Возвращает историю транзакций по указанной карте с пагинацией.")
    @GetMapping("/transactions")
    public List<TransactionResponseDTO> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, ShowTransactionalByCardRequestDTO historyTransactionsDto,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        return cardFunctionService.getTransactionalByCard(historyTransactionsDto, page, size, idempotencyKey);
    }

    /**
     * Запрос блокирования карты
     * @param blockCardDto dto c номером карты
     * @param idempotencyKey
     * @return Строка с ответом
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Заблокировать карту",
            description = "Переводит карту в заблокированное состояние. Возвращаемого значения нет.")
    @PutMapping("/block")
    public void blockCard(@Valid @RequestBody BlockCardRequestDTO blockCardDto,
                            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey){

        cardFunctionService.requestCardBlock(blockCardDto, idempotencyKey);
    }

    /**
     * Запрос пополнения баланса карты
     * @param replenishmentCardDto dto, включаяет номер карты и сумму
     * @param idempotencyKey
     * @return dto транзакции
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Пополнить баланс карты",
            description = "Пополняет баланс карты пользователя и возвращает данные транзакции.")
    @PutMapping("/replenishment")
    public TransactionResponseDTO replenishmentCard(@Valid @RequestBody ReplenishmentCardRequestDTO replenishmentCardDto,
                                                    @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey){

        return cardFunctionService.cardReplenishment(replenishmentCardDto, idempotencyKey);
    }
}
