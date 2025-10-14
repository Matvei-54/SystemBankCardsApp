package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.operations.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, Long> {


    List<TransactionEntity> findBySourceCard(CardEntity cardEntity);

    List<TransactionEntity> findBySourceCard(CardEntity cardEntity, Pageable pageable);
}
