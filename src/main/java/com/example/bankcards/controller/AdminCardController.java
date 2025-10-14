package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.service.AdminCardFunctionService;
import com.example.bankcards.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import javax.validation.constraints.*;

@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
@RestController
public class AdminCardController {

    private final AdminCardFunctionService adminCardFunctionService;
    private final IdempotencyService idempotencyService;

    /**
     * Запрос создания карты
     * @param request - dto с параметрами карты
     * @param idempotencyKey
     * @return dto с параметрами созданной карты
     */
    @Operation(summary = "Создать карту", description = "В ответе возвращается dto карты")
    @Tag(name = "admin", description = "Card API")
    @PostMapping("/create")
    public CardResponse createCard(@Valid @RequestBody CreateCardRequestDto request,
                                   @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {

            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, CardResponse.class);
        }
        return adminCardFunctionService.createCard(request, idempotencyKey);
    }


    /**
     * Запрос обновления данных карты
     * @param request dto с новыми параметрами карты
     * @param idempotencyKey
     * @return dto с параметрами обновленной карты
     */
    @Operation(summary = "Изменение номера и срока действия карты", description = "В ответе возвращается dto карты")
    @Tag(name = "admin", description = "Card API")
    @PutMapping("/update")
    public CardResponse updateCard(@Valid @RequestBody UpdateCardRequestDto request,
                                   @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {

            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, CardResponse.class);
        }
        return adminCardFunctionService.updateCard(request);
    }


    /**
     * Запрос удаления карты
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @Operation(summary = "Удаление карты", description = "В ответе ничего не возвращается.")
    @Tag(name = "delete", description = "Card API")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String deleteCard(@Valid @RequestBody DeleteCardRequestDto request,
                             @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, String.class);
        }

        return adminCardFunctionService.deleteCard(request, idempotencyKey);
    }

    /**
     * Запрос изменения статуса карты, на активный.
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @Operation(summary = "Активировать карту", description = "В ответе ничего не возвращается.")
    @Tag(name = "admin", description = "Card API")
    @PostMapping("/activate")
    public String activateCard(@Valid @RequestBody ActivateCardRequestDto request,
                               @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, String.class);
        }
        return adminCardFunctionService.activateCard(request, idempotencyKey);
    }

    /**
     * Запрос изменения статуса карты, на заблокированный.
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @Operation(summary = "Заблокировать карту", description = "В ответе ничего не возвращается.")
    @Tag(name = "admin", description = "Card API")
    @PostMapping("/blocked")
    public String blockCard(@Valid @RequestBody BlockCardRequestDto request,
                            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, String.class);
        }
        return adminCardFunctionService.blockCard(request, idempotencyKey);
    }
}
