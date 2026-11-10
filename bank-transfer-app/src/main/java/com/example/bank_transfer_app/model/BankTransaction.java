package com.example.bank_transfer_app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_transactions")
@Getter
@Setter
@NoArgsConstructor
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "from_account")
    private String fromAccount;

    @Column(name = "to_account")
    private String toAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public BankTransaction(
            BigDecimal amount,
            String fromAccount,
            String toAccount,
            TransactionStatus status,
            String description) {

        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.status = status;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}