package com.example.bankcards.entity;


import com.example.bankcards.entity.enums.*;
import com.example.bankcards.entity.enums.converter.CardStatusConverter;
import com.example.bankcards.entity.operations.TransactionEntity;
import com.example.bankcards.util.CardNumberEncryptorConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "card_entity")
public class CardEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_card")
    @SequenceGenerator(name = "sequence_card", sequenceName = "card_main_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "card_number")
    @Convert(converter = CardNumberEncryptorConverter.class)
    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private CustomerEntity customerEntity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "card_status")
    @Convert(converter = CardStatusConverter.class)
    private CardStatus status;

    @Column(name = "card_balance")
    private BigDecimal balance;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToMany(mappedBy = "sourceCardEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> history;
}
