package com.zientis.economy.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EconomyAccount
 */
class EconomyAccountTest {
    
    private UUID testPlayerId;
    private EconomyAccount account;
    
    @BeforeEach
    void setUp() {
        testPlayerId = UUID.randomUUID();
        account = new EconomyAccount(testPlayerId, BigDecimal.valueOf(100.0));
    }
    
    @Test
    @DisplayName("Account should initialize with correct values")
    void testAccountInitialization() {
        assertEquals(testPlayerId, account.getPlayerId());
        assertEquals(BigDecimal.valueOf(100.0), account.getBalance());
        assertFalse(account.isFrozen());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getLastUpdated());
    }
    
    @Test
    @DisplayName("Account should initialize with zero balance by default")
    void testDefaultBalance() {
        EconomyAccount defaultAccount = new EconomyAccount(testPlayerId);
        assertEquals(BigDecimal.ZERO, defaultAccount.getBalance());
    }
    
    @Test
    @DisplayName("Adding balance should work correctly")
    void testAddBalance() {
        BigDecimal newBalance = account.addBalance(BigDecimal.valueOf(50.0));
        assertEquals(BigDecimal.valueOf(150.0), newBalance);
        assertEquals(BigDecimal.valueOf(150.0), account.getBalance());
    }
    
    @Test
    @DisplayName("Adding negative amount should throw exception")
    void testAddNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.addBalance(BigDecimal.valueOf(-10.0));
        });
    }
    
    @Test
    @DisplayName("Subtracting balance should work correctly")
    void testSubtractBalance() {
        BigDecimal newBalance = account.subtractBalance(BigDecimal.valueOf(25.0));
        assertEquals(BigDecimal.valueOf(75.0), newBalance);
        assertEquals(BigDecimal.valueOf(75.0), account.getBalance());
    }
    
    @Test
    @DisplayName("Subtracting more than balance should throw exception")
    void testSubtractInsufficientFunds() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.subtractBalance(BigDecimal.valueOf(150.0));
        });
    }
    
    @Test
    @DisplayName("Subtracting negative amount should throw exception")
    void testSubtractNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.subtractBalance(BigDecimal.valueOf(-10.0));
        });
    }
    
    @Test
    @DisplayName("Setting negative balance should throw exception")
    void testSetNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> {
            account.setBalance(BigDecimal.valueOf(-50.0));
        });
    }
    
    @Test
    @DisplayName("hasFunds should work correctly")
    void testHasFunds() {
        assertTrue(account.hasFunds(BigDecimal.valueOf(50.0)));
        assertTrue(account.hasFunds(BigDecimal.valueOf(100.0)));
        assertFalse(account.hasFunds(BigDecimal.valueOf(150.0)));
    }
    
    @Test
    @DisplayName("Freezing and unfreezing account should work")
    void testFreezeAccount() {
        assertFalse(account.isFrozen());
        
        account.setFrozen(true);
        assertTrue(account.isFrozen());
        
        account.setFrozen(false);
        assertFalse(account.isFrozen());
    }
    
    @Test
    @DisplayName("Account equality should be based on player ID")
    void testAccountEquality() {
        EconomyAccount account1 = new EconomyAccount(testPlayerId, BigDecimal.valueOf(100.0));
        EconomyAccount account2 = new EconomyAccount(testPlayerId, BigDecimal.valueOf(200.0));
        EconomyAccount account3 = new EconomyAccount(UUID.randomUUID(), BigDecimal.valueOf(100.0));
        
        assertEquals(account1, account2); // Same player ID
        assertNotEquals(account1, account3); // Different player ID
        assertEquals(account1.hashCode(), account2.hashCode());
    }
    
    @Test
    @DisplayName("toString should include key information")
    void testToString() {
        String str = account.toString();
        assertTrue(str.contains(testPlayerId.toString()));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("false")); // frozen status
    }
}