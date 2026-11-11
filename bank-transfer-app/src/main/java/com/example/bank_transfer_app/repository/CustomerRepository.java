
package com.example.bank_transfer_app.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.bank_transfer_app.model.Customer;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, String> {

    // Find customer using account number
    @Query("""
            SELECT c FROM Customer c
            WHERE c.accountNumber = :accountNumber
            """)
    Optional<Customer> getByAccountNumber(
            @Param("accountNumber") String accountNumber);

    // Check whether account number already exists
    @Query("""
            SELECT COUNT(c) FROM Customer c
            WHERE c.accountNumber = :accountNumber
            """)
    long countAccounts(
            @Param("accountNumber") String accountNumber);

    // Check whether email already exists
    @Query("""
            SELECT COUNT(c) FROM Customer c
            WHERE c.email = :email
            """)
    long countEmails(
            @Param("email") String email);

    // Get all customers ordered by name
    @Query("SELECT c FROM Customer c ORDER BY c.fullName ASC")
    List<Customer> findAllOrdered();

    // Search customers by name
    @Query("""
            SELECT c FROM Customer c
            WHERE LOWER(c.fullName)
            LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.fullName ASC
            """)
    List<Customer> searchByName(
            @Param("keyword") String keyword);

    // Calculate total balance of all accounts
    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Customer c")
    BigDecimal getTotalBalance();

    // Native SQL query to count customers
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // Debit money from an account
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Customer c
            SET c.balance = c.balance - :amount
            WHERE c.accountNumber = :accountNumber
            AND c.balance >= :amount
            """)
    int debit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount);

    // Credit money to an account
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Customer c
            SET c.balance = c.balance + :amount
            WHERE c.accountNumber = :accountNumber
            """)
    int credit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount);
}
