
package com.example.bank_transfer_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.bank_transfer_app.exception.AccountNotFoundException;
import com.example.bank_transfer_app.exception.BankException;
import com.example.bank_transfer_app.exception.InsufficientBalanceException;
import com.example.bank_transfer_app.model.BankTransaction;
import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    // Bank fee account
    private static final String BANK_FEE_ACCOUNT = "999999";

    // Transfer fee rate: 1%
    private static final BigDecimal TRANSFER_FEE_RATE =
            new BigDecimal("0.01");

    // ==========================================
    // TRANSFER MONEY WITH 1% BANK FEE
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        try {

            validateAmount(amount);

            if (fromAccount == null || toAccount == null
                    || fromAccount.isBlank() || toAccount.isBlank()) {

                throw new BankException(
                        "Account numbers are required");
            }

            if (fromAccount.equals(toAccount)) {

                throw new BankException(
                        "Sender and receiver accounts must be different");
            }

            if (fromAccount.equals(BANK_FEE_ACCOUNT)
                    || toAccount.equals(BANK_FEE_ACCOUNT)) {

                throw new BankException(
                        "Bank Fees account cannot be used for normal transfers");
            }

            // Check sender account
            customerRepository.getByAccountNumber(fromAccount)
                    .orElseThrow(() ->
                            new AccountNotFoundException(fromAccount));

            // Check receiver account
            customerRepository.getByAccountNumber(toAccount)
                    .orElseThrow(() ->
                            new AccountNotFoundException(toAccount));

            // Check bank fee account
            customerRepository.getByAccountNumber(BANK_FEE_ACCOUNT)
                    .orElseThrow(() ->
                            new AccountNotFoundException(BANK_FEE_ACCOUNT));

            // ======================================
            // CALCULATE 1% TRANSFER FEE
            // ======================================

            BigDecimal fee = amount
                    .multiply(TRANSFER_FEE_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalDebit = amount.add(fee);

            // ======================================
            // UPDATE 1: DEBIT SENDER
            // ======================================

            int debited = customerRepository.debit(
                    fromAccount,
                    totalDebit
            );

            if (debited == 0) {

                throw new InsufficientBalanceException(fromAccount);
            }

            // ======================================
            // UPDATE 2: CREDIT RECEIVER
            // ======================================

            int receiverCredited = customerRepository.credit(
                    toAccount,
                    amount
            );

            if (receiverCredited == 0) {

                throw new BankException(
                        "Receiver account could not be credited");
            }

            // ======================================
            // UPDATE 3: CREDIT BANK FEES ACCOUNT
            // ======================================

            int feeCredited = customerRepository.credit(
                    BANK_FEE_ACCOUNT,
                    fee
            );

            if (feeCredited == 0) {

                throw new BankException(
                        "Bank transfer fee could not be credited");
            }

            // ======================================
            // SIMULATE FAILURE FOR ROLLBACK TEST
            // ======================================

            if (simulateFailure) {

                throw new RuntimeException(
                        "Simulated system failure after all balance updates!");
            }

            // ======================================
            // SAVE SUCCESSFUL TRANSACTION
            // ======================================

            BankTransaction transaction = new BankTransaction(
                    amount,
                    fromAccount,
                    toAccount,
                    TransactionStatus.SUCCESS,
                    "Transfer of LKR " + amount
                            + " | Bank fee (1%): LKR " + fee
            );

            return transactionRepository.save(transaction);

        } catch (RuntimeException e) {

            // Record failed transaction in a separate transaction
            auditService.recordFailure(
                    fromAccount,
                    toAccount,
                    amount,
                    e.getMessage()
            );

            // Rethrow exception to trigger rollback
            throw e;
        }
    }

    // ==========================================
    // CREATE CUSTOMER
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {

            throw new BankException(
                    "Account number "
                            + customer.getAccountNumber()
                            + " already exists");
        }

        if (customerRepository.countEmails(
                customer.getEmail()) > 0) {

            throw new BankException(
                    "Email " + customer.getEmail()
                            + " is already registered");
        }

        return customerRepository.save(customer);
    }

    // ==========================================
    // DEPOSIT MONEY
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    public void deposit(String accountNumber, BigDecimal amount) {

        validateAmount(amount);

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountNumber));

        int credited = customerRepository.credit(
                accountNumber, amount);

        if (credited == 0) {

            throw new BankException(
                    "Deposit could not be completed");
        }

        BankTransaction transaction = new BankTransaction(
                amount,
                "EXTERNAL",
                accountNumber,
                TransactionStatus.SUCCESS,
                "Deposit of LKR " + amount
        );

        transactionRepository.save(transaction);
    }

    // ==========================================
    // WITHDRAW MONEY
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String accountNumber, BigDecimal amount) {

        validateAmount(amount);

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountNumber));

        int debited = customerRepository.debit(
                accountNumber, amount);

        if (debited == 0) {

            throw new InsufficientBalanceException(accountNumber);
        }

        BankTransaction transaction = new BankTransaction(
                amount,
                accountNumber,
                "EXTERNAL",
                TransactionStatus.SUCCESS,
                "Withdrawal of LKR " + amount
        );

        transactionRepository.save(transaction);
    }

    // ==========================================
    // VALIDATE AMOUNT
    // ==========================================

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {

            throw new BankException(
                    "Amount must be greater than zero");
        }
    }
}
