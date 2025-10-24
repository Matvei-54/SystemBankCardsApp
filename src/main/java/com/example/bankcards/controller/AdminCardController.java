package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.service.AdminCardService;
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

@Tag(name = "Card Management", description = "Администрирование карт — создание, обновление, активация, блокировка и удаление.")
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
@RestController
public class AdminCardController {

    private final AdminCardService adminCardService;

    /**
     * Запрос создания карты
     * @param request - dto с параметрами карты
     * @param idempotencyKey
     * @return dto с параметрами созданной карты
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту", description = "Создаёт новую карту и возвращает её параметры.")
    @PostMapping("/create")
    public CardResponseDTO createCard(@Valid @RequestBody CreateCardRequestDTO request,
                                      @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        return adminCardService.createCard(request, idempotencyKey);
    }


    /**
     * Запрос обновления данных карты
     * @param request dto с новыми параметрами карты
     * @param idempotencyKey
     * @return dto с параметрами обновленной карты
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить карту", description = "Изменяет номер и срок действия карты, возвращает обновлённые данные.")
    @PutMapping("/update")
    public CardResponseDTO updateCard(@Valid @RequestBody UpdateCardRequestDTO request,
                                      @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        return adminCardService.updateCard(request);
    }


    /**
     * Запрос удаления карты
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @Operation(summary = "Удалить карту", description = "Удаляет карту по её номеру. Возвращаемого значения нет.")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void deleteCard(@Valid @RequestBody DeleteCardRequestDTO request,
                             @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardService.deleteCard(request, idempotencyKey);
    }

    /**
     * Запрос изменения статуса карты, на активный.
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту", description = "Переводит карту в активное состояние. Возвращаемого значения нет.")
    @PostMapping("/activate")
    public void activateCard(@Valid @RequestBody ActivateCardRequestDTO request,
                               @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardService.activateCard(request, idempotencyKey);
    }

    /**
     * Запрос изменения статуса карты, на заблокированный.
     * @param request dto - с номером карты
     * @param idempotencyKey
     * @return строка с ответом
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту", description = "Переводит карту в заблокированное состояние. Возвращаемого значения нет.")
    @PostMapping("/block")
    public void blockCard(@Valid @RequestBody BlockCardRequestDTO request,
                            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {

        adminCardService.blockCard(request, idempotencyKey);
    }
}
