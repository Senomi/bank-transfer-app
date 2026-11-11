package com.example.bank_transfer_app.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @Column(name = "account_number", nullable = false, unique = true)
    @NotBlank(message = "Account number is required")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "Account number must be exactly 6 digits"
    )
    private String accountNumber;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "Full name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(precision = 14, scale = 2, nullable = false)
    @NotNull(message = "Opening balance is required")
    @DecimalMin(
            value = "0.00",
            message = "Balance cannot be negative"
    )
    private BigDecimal balance;

    public Customer(
            String accountNumber,
            String fullName,
            String email,
            BigDecimal balance) {

        this.accountNumber = accountNumber;
        this.fullName = fullName;
        this.email = email;
        this.balance = balance;
    }
}