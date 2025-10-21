package com.example.bankcards.entity.operations;

import com.example.bankcards.entity.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.*;
import com.example.bankcards.entity.enums.converter.TransactionStatusConverter;
import com.example.bankcards.entity.enums.converter.TransactionTypeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "transaction_entity")
public class TransactionEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_transaction")
    @SequenceGenerator(name = "sequence_transaction", sequenceName = "transaction_main_sequence", allocationSize = 1)
    private Long id;

    private BigDecimal amount;
    private Currency currency;

    @Column(name = "transaction_status")
    @Convert(converter = TransactionStatusConverter.class)
    private TransactionStatus transactionStatus;

    @Column(name = "transaction_type")
    @Convert(converter = TransactionTypeConverter.class)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "source_card_id", referencedColumnName = "id")
    private CardEntity sourceCardEntity;

    @ManyToOne
    @JoinColumn(name = "target_card_id")
    private CardEntity targetCardEntity;
}
