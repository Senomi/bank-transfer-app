package com.example.bank_transfer_app.service;

import java.math.BigDecimal;

import com.example.bank_transfer_app.model.BankTransaction;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BankTransaction recordFailure(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String reason) {

        BigDecimal recordedAmount =
                amount == null ? BigDecimal.ZERO : amount;

        String description =
                (reason == null || reason.isBlank())
                        ? "Transaction failed"
                        : "Transaction failed: " + reason;

        BankTransaction failedTransaction = new BankTransaction(
                recordedAmount,
                fromAccount,
                toAccount,
                TransactionStatus.FAILED,
                description
        );

        return transactionRepository.save(failedTransaction);
    }
}