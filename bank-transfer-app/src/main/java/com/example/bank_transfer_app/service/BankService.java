
package com.example.bank_transfer_app.service;

import java.math.BigDecimal;

import org.springframework.transaction.annotation.Propagation;
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

    // Transfer money between accounts

    // TASK 10: Experiment without an outer transaction
    @Transactional(rollbackFor = Exception.class)
    //@Transactional(propagation = Propagation.SUPPORTS)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        try {

            validateAmount(amount);

            if (fromAccount == null || toAccount == null
                    || fromAccount.isBlank() || toAccount.isBlank()) {
                throw new BankException("Account numbers are required");
            }

            if (fromAccount.equals(toAccount)) {
                throw new BankException(
                        "Sender and receiver accounts must be different");
            }

            // Check sender account
            customerRepository.getByAccountNumber(fromAccount)
                    .orElseThrow(() ->
                            new AccountNotFoundException(fromAccount));

            // Check receiver account
            customerRepository.getByAccountNumber(toAccount)
                    .orElseThrow(() ->
                            new AccountNotFoundException(toAccount));

            // 1. Debit transfer amount from sender
            int debited = customerRepository.debit(
                    fromAccount, amount);

            if (debited == 0) {
                throw new InsufficientBalanceException(fromAccount);
            }

            // 2. Simulate failure for rollback testing
            if (simulateFailure) {
                throw new RuntimeException(
                        "Simulated system failure after the withdrawal!");
            }

            // 3. Credit transfer amount to receiver
            int receiverCredited = customerRepository.credit(
                    toAccount, amount);

            if (receiverCredited == 0) {
                throw new BankException(
                        "Receiver account could not be credited");
            }

            // 4. Save successful transaction
            BankTransaction transaction = new BankTransaction(
                    amount,
                    fromAccount,
                    toAccount,
                    TransactionStatus.SUCCESS,
                    "Transfer of LKR " + amount
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

            // Re-throw exception
            throw e;
        }
    }

    // Create new customer
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

    // Validate transaction amount
    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {

            throw new BankException(
                    "Amount must be greater than zero");
        }
    }
}
