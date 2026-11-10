package com.example.bank_transfer_app.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bank_transfer_app.model.BankTransaction;
import com.example.bank_transfer_app.model.TransactionStatus;

@Repository
public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    // Get all transactions, newest first
    @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC, t.id DESC")
    List<BankTransaction> findAllLatest();

    // Get transaction history for a particular account
    @Query("""
            SELECT t FROM BankTransaction t
            WHERE t.fromAccount = :acc OR t.toAccount = :acc
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<BankTransaction> findHistory(
            @Param("acc") String accountNumber);

    // Get transactions by status
    @Query("""
            SELECT t FROM BankTransaction t
            WHERE t.status = :status
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<BankTransaction> findByStatusLatest(
            @Param("status") TransactionStatus status);

    // Count transactions by status
    @Query("""
            SELECT COUNT(t) FROM BankTransaction t
            WHERE t.status = :status
            """)
    long countByStatus(
            @Param("status") TransactionStatus status);

    // Find successful large transfers
    @Query("""
            SELECT t FROM BankTransaction t
            WHERE t.status = :status
            AND t.amount >= :minAmount
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<BankTransaction> findLargeTransfers(
            @Param("minAmount") BigDecimal minAmount,
            @Param("status") TransactionStatus status);

    // Calculate total successful amount sent by an account
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM BankTransaction t
            WHERE t.fromAccount = :acc
            AND t.status = :status
            """)
    BigDecimal getTotalSentByAccount(
            @Param("acc") String accountNumber,
            @Param("status") TransactionStatus status);
}