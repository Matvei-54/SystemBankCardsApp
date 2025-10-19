package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.service.AdminCardFunctionService;
import com.example.bankcards.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
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
import java.security.*;

@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
@RestController
public class AdminCardFunctionController {

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
    public CardResponseDTO createCard(@Valid @RequestBody CreateCardRequestDTO request,
                                      @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                                      Principal principal,
                                      Authentication authentication) {

        return adminCardFunctionService.createCard(request, idempotencyKey, principal);
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
    public CardResponseDTO updateCard(@Valid @RequestBody UpdateCardRequestDTO request,
                                      @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

//        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
//
//            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, CardResponseDTO.class);
//        }
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
    public void deleteCard(@Valid @RequestBody DeleteCardRequestDTO request,
                             @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardFunctionService.deleteCard(request, idempotencyKey);
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
    public void activateCard(@Valid @RequestBody ActivateCardRequestDTO request,
                               @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardFunctionService.activateCard(request, idempotencyKey);
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
    public void blockCard(@Valid @RequestBody BlockCardRequestDTO request,
                            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardFunctionService.blockCard(request, idempotencyKey);
    }
}
