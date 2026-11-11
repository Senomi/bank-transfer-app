package com.example.bank_transfer_app.exception;

public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String accountNumber) {
        super("Account " + accountNumber + " was not found");
    }
}