package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardEntityRepository extends JpaRepository<CardEntity, Long> {

    Page<CardEntity> findByCustomerEntityId(Long customerId, Pageable pageable);

    Page<CardEntity> findByCustomerEntityIdAndStatus(Long customerId, CardStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM CardEntity c WHERE c.cardNumber = :encryptedCardNumber")
    Optional<CardEntity> findByCardNumberWithLock(@Param("encryptedCardNumber") String cardNumber);

    Optional<CardEntity> findByCardNumber(String cardNumber);
}
