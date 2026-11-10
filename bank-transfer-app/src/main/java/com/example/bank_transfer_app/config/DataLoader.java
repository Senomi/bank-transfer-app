package com.example.bank_transfer_app.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() == 0) {

            // Initial customers (Total balance: LKR 85,000.00)

            customerRepository.save(
                    new Customer("100001", "Kamal Perera",
                            "kamal@mail.com", new BigDecimal("50000.00")));

            customerRepository.save(
                    new Customer("100002", "Nimali Silva",
                            "nimali@mail.com", new BigDecimal("25000.00")));

            customerRepository.save(
                    new Customer("100003", "Saman Kumara",
                            "saman@mail.com", new BigDecimal("10000.00")));

            // Bank fee collection account

            customerRepository.save(
                    new Customer("999999", "Bank Fees",
                            "bank@system.local", new BigDecimal("0.00")));
        }
    }
}