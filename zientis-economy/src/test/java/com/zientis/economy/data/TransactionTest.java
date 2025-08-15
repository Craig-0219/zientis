package com.zientis.economy.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction
 */
class TransactionTest {
    
    private UUID fromPlayerId;
    private UUID toPlayerId;
    
    @BeforeEach
    void setUp() {
        fromPlayerId = UUID.randomUUID();
        toPlayerId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Transfer transaction should build correctly")
    void testTransferTransaction() {
        Transaction transaction = new Transaction.Builder()
            .type(Transaction.TransactionType.TRANSFER)
            .from(fromPlayerId)
            .to(toPlayerId)
            .amount(BigDecimal.valueOf(100.0))
            .description("Test transfer")
            .build();
        
        assertNotNull(transaction.getTransactionId());
        assertEquals(fromPlayerId, transaction.getFromAccount());
        assertEquals(toPlayerId, transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(100.0), transaction.getAmount());
        assertEquals(Transaction.TransactionType.TRANSFER, transaction.getType());
        assertEquals("Test transfer", transaction.getDescription());
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());
        assertNotNull(transaction.getTimestamp());
    }
    
    @Test
    @DisplayName("Deposit transaction should build correctly")
    void testDepositTransaction() {
        Transaction transaction = new Transaction.Builder()
            .type(Transaction.TransactionType.DEPOSIT)
            .to(toPlayerId)
            .amount(50.0)
            .description("Test deposit")
            .build();
        
        assertNull(transaction.getFromAccount());
        assertEquals(toPlayerId, transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(50.0), transaction.getAmount());
        assertEquals(Transaction.TransactionType.DEPOSIT, transaction.getType());
    }
    
    @Test
    @DisplayName("Withdrawal transaction should build correctly")
    void testWithdrawalTransaction() {
        Transaction transaction = new Transaction.Builder()
            .type(Transaction.TransactionType.WITHDRAWAL)
            .from(fromPlayerId)
            .amount(25.0)
            .description("Test withdrawal")
            .build();
        
        assertEquals(fromPlayerId, transaction.getFromAccount());
        assertNull(transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(25.0), transaction.getAmount());
        assertEquals(Transaction.TransactionType.WITHDRAWAL, transaction.getType());
    }
    
    @Test
    @DisplayName("Builder should validate transfer requirements")
    void testTransferValidation() {
        // Missing from account
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.TRANSFER)
                .to(toPlayerId)
                .amount(100.0)
                .build();
        });
        
        // Missing to account
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.TRANSFER)
                .from(fromPlayerId)
                .amount(100.0)
                .build();
        });
    }
    
    @Test
    @DisplayName("Builder should validate deposit requirements")
    void testDepositValidation() {
        // Missing to account
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(100.0)
                .build();
        });
    }
    
    @Test
    @DisplayName("Builder should validate withdrawal requirements")
    void testWithdrawalValidation() {
        // Missing from account
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(100.0)
                .build();
        });
    }
    
    @Test
    @DisplayName("Builder should validate amount")
    void testAmountValidation() {
        // Zero amount
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .to(toPlayerId)
                .amount(BigDecimal.ZERO)
                .build();
        });
        
        // Negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .to(toPlayerId)
                .amount(-10.0)
                .build();
        });
        
        // Missing amount
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .to(toPlayerId)
                .build();
        });
    }
    
    @Test
    @DisplayName("Builder should validate transaction type")
    void testTypeValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction.Builder()
                .from(fromPlayerId)
                .to(toPlayerId)
                .amount(100.0)
                .build(); // Missing type
        });
    }
    
    @Test
    @DisplayName("withStatus should create new transaction with updated status")
    void testWithStatus() {
        Transaction original = new Transaction.Builder()
            .type(Transaction.TransactionType.TRANSFER)
            .from(fromPlayerId)
            .to(toPlayerId)
            .amount(100.0)
            .build();
        
        assertEquals(Transaction.TransactionStatus.PENDING, original.getStatus());
        
        Transaction completed = original.withStatus(Transaction.TransactionStatus.COMPLETED);
        
        // Original should be unchanged
        assertEquals(Transaction.TransactionStatus.PENDING, original.getStatus());
        
        // New transaction should have updated status
        assertEquals(Transaction.TransactionStatus.COMPLETED, completed.getStatus());
        
        // Other fields should be the same
        assertEquals(original.getTransactionId(), completed.getTransactionId());
        assertEquals(original.getAmount(), completed.getAmount());
        assertEquals(original.getType(), completed.getType());
    }
    
    @Test
    @DisplayName("Transaction equality should be based on transaction ID")
    void testTransactionEquality() {
        UUID transactionId = UUID.randomUUID();
        
        Transaction transaction1 = new Transaction.Builder()
            .transactionId(transactionId)
            .type(Transaction.TransactionType.TRANSFER)
            .from(fromPlayerId)
            .to(toPlayerId)
            .amount(100.0)
            .build();
        
        Transaction transaction2 = new Transaction.Builder()
            .transactionId(transactionId)
            .type(Transaction.TransactionType.DEPOSIT)
            .to(toPlayerId)
            .amount(50.0)
            .build();
        
        assertEquals(transaction1, transaction2); // Same transaction ID
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }
    
    @Test
    @DisplayName("toString should include key information")
    void testToString() {
        Transaction transaction = new Transaction.Builder()
            .type(Transaction.TransactionType.TRANSFER)
            .from(fromPlayerId)
            .to(toPlayerId)
            .amount(100.0)
            .build();
        
        String str = transaction.toString();
        assertTrue(str.contains(transaction.getTransactionId().toString()));
        assertTrue(str.contains("TRANSFER"));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("PENDING"));
    }
    
    @Test
    @DisplayName("Custom timestamp should be preserved")
    void testCustomTimestamp() {
        LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        Transaction transaction = new Transaction.Builder()
            .type(Transaction.TransactionType.DEPOSIT)
            .to(toPlayerId)
            .amount(100.0)
            .timestamp(customTime)
            .build();
        
        assertEquals(customTime, transaction.getTimestamp());
    }
}