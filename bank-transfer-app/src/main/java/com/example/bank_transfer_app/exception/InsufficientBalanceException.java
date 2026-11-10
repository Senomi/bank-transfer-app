package com.example.bank_transfer_app.exception;

public class InsufficientBalanceException extends BankException {

    public InsufficientBalanceException(String accountNumber) {
        super("Insufficient balance in account " + accountNumber);
    }
}