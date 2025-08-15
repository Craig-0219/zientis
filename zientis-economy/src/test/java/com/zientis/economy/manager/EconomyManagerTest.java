package com.zientis.economy.manager;

import com.zientis.economy.data.EconomyAccount;
import com.zientis.economy.data.Transaction;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EconomyManager
 */
class EconomyManagerTest {
    
    @Mock
    private Plugin mockPlugin;
    
    private EconomyManager economyManager;
    private UUID testPlayerId;
    private AutoCloseable mockitoCloseable;
    
    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
        
        economyManager = new EconomyManager(mockPlugin);
        testPlayerId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Creating new account should give default starting balance")
    void testCreateAccount() throws Exception {
        EconomyAccount account = economyManager.getOrCreateAccount(testPlayerId).get(5, TimeUnit.SECONDS);
        
        assertNotNull(account);
        assertEquals(testPlayerId, account.getPlayerId());
        assertEquals(BigDecimal.valueOf(100.0), account.getBalance()); // Default starting balance
        assertFalse(account.isFrozen());
    }
    
    @Test
    @DisplayName("Getting balance should work correctly")
    void testGetBalance() throws Exception {
        // Create account first
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        BigDecimal balance = economyManager.getBalance(testPlayerId).get(5, TimeUnit.SECONDS);
        assertEquals(BigDecimal.valueOf(100.0), balance);
    }
    
    @Test
    @DisplayName("Deposit should increase balance")
    void testDeposit() throws Exception {
        // Create account first
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        Transaction transaction = economyManager.deposit(testPlayerId, BigDecimal.valueOf(50.0), "Test deposit")
            .get(5, TimeUnit.SECONDS);
        
        assertNotNull(transaction);
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(Transaction.TransactionType.DEPOSIT, transaction.getType());
        assertEquals(BigDecimal.valueOf(50.0), transaction.getAmount());
        
        // Check new balance
        BigDecimal newBalance = economyManager.getBalance(testPlayerId).get();
        assertEquals(BigDecimal.valueOf(150.0), newBalance);
    }
    
    @Test
    @DisplayName("Withdraw should decrease balance")
    void testWithdraw() throws Exception {
        // Create account first
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        Transaction transaction = economyManager.withdraw(testPlayerId, BigDecimal.valueOf(25.0), "Test withdrawal")
            .get(5, TimeUnit.SECONDS);
        
        assertNotNull(transaction);
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(Transaction.TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(BigDecimal.valueOf(25.0), transaction.getAmount());
        
        // Check new balance
        BigDecimal newBalance = economyManager.getBalance(testPlayerId).get();
        assertEquals(BigDecimal.valueOf(75.0), newBalance);
    }
    
    @Test
    @DisplayName("Withdraw more than balance should fail")
    void testInsufficientFunds() {
        // Create account first
        economyManager.getOrCreateAccount(testPlayerId).join();
        
        assertThrows(Exception.class, () -> {
            economyManager.withdraw(testPlayerId, BigDecimal.valueOf(200.0), "Test withdrawal")
                .get(5, TimeUnit.SECONDS);
        });
    }
    
    @Test
    @DisplayName("Transfer between players should work")
    void testTransfer() throws Exception {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Create both accounts
        economyManager.getOrCreateAccount(player1).get();
        economyManager.getOrCreateAccount(player2).get();
        
        Transaction transaction = economyManager.transfer(player1, player2, BigDecimal.valueOf(30.0), "Test transfer")
            .get(5, TimeUnit.SECONDS);
        
        assertNotNull(transaction);
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(Transaction.TransactionType.TRANSFER, transaction.getType());
        assertEquals(BigDecimal.valueOf(30.0), transaction.getAmount());
        assertEquals(player1, transaction.getFromAccount());
        assertEquals(player2, transaction.getToAccount());
        
        // Check balances
        BigDecimal balance1 = economyManager.getBalance(player1).get();
        BigDecimal balance2 = economyManager.getBalance(player2).get();
        
        assertEquals(BigDecimal.valueOf(70.0), balance1); // 100 - 30
        assertEquals(BigDecimal.valueOf(130.0), balance2); // 100 + 30
    }
    
    @Test
    @DisplayName("Transfer to self should fail")
    void testTransferToSelf() {
        economyManager.getOrCreateAccount(testPlayerId).join();
        
        assertThrows(Exception.class, () -> {
            economyManager.transfer(testPlayerId, testPlayerId, BigDecimal.valueOf(10.0), "Self transfer")
                .get(5, TimeUnit.SECONDS);
        });
    }
    
    @Test
    @DisplayName("hasFunds should check balance correctly")
    void testHasFunds() throws Exception {
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        assertTrue(economyManager.hasFunds(testPlayerId, BigDecimal.valueOf(50.0)).get());
        assertTrue(economyManager.hasFunds(testPlayerId, BigDecimal.valueOf(100.0)).get());
        assertFalse(economyManager.hasFunds(testPlayerId, BigDecimal.valueOf(150.0)).get());
    }
    
    @Test
    @DisplayName("Setting balance should work")
    void testSetBalance() throws Exception {
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        Transaction transaction = economyManager.setBalance(testPlayerId, BigDecimal.valueOf(250.0), "Admin set")
            .get(5, TimeUnit.SECONDS);
        
        assertNotNull(transaction);
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        
        BigDecimal newBalance = economyManager.getBalance(testPlayerId).get();
        assertEquals(BigDecimal.valueOf(250.0), newBalance);
    }
    
    @Test
    @DisplayName("Freezing account should prevent transactions")
    void testFreezeAccount() throws Exception {
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        // Freeze account
        Boolean frozen = economyManager.freezeAccount(testPlayerId, "Test freeze").get();
        assertTrue(frozen);
        
        // Check if frozen
        Boolean isFrozen = economyManager.isAccountFrozen(testPlayerId).get();
        assertTrue(isFrozen);
        
        // Try to withdraw (should fail)
        assertThrows(Exception.class, () -> {
            economyManager.withdraw(testPlayerId, BigDecimal.valueOf(10.0), "Test withdrawal")
                .get(5, TimeUnit.SECONDS);
        });
    }
    
    @Test
    @DisplayName("Transaction history should be recorded")
    void testTransactionHistory() throws Exception {
        economyManager.getOrCreateAccount(testPlayerId).get();
        
        // Perform some transactions
        economyManager.deposit(testPlayerId, BigDecimal.valueOf(50.0), "Test deposit").get();
        economyManager.withdraw(testPlayerId, BigDecimal.valueOf(25.0), "Test withdrawal").get();
        
        List<Transaction> history = economyManager.getTransactionHistory(testPlayerId, 10).get();
        
        assertNotNull(history);
        assertEquals(2, history.size());
        
        // Should be sorted by timestamp (newest first)
        assertEquals(Transaction.TransactionType.WITHDRAWAL, history.get(0).getType());
        assertEquals(Transaction.TransactionType.DEPOSIT, history.get(1).getType());
    }
    
    @Test
    @DisplayName("Economy stats should be calculated correctly")
    void testEconomyStats() throws Exception {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Create accounts
        economyManager.getOrCreateAccount(player1).get();
        economyManager.getOrCreateAccount(player2).get();
        
        // Add some money
        economyManager.deposit(player1, BigDecimal.valueOf(100.0), "Test").get();
        
        var stats = economyManager.getEconomyStats().get();
        
        assertNotNull(stats);
        assertEquals(2, stats.getTotalAccounts());
        assertEquals(2, stats.getActiveAccounts());
        assertEquals(BigDecimal.valueOf(300.0), stats.getTotalCirculation()); // 200 + 100
        assertTrue(stats.getTotalTransactions() > 0);
    }
    
    @Test
    @DisplayName("Top players should be sorted by balance")
    void testTopPlayers() throws Exception {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        
        // Create accounts with different balances
        economyManager.getOrCreateAccount(player1).get();
        economyManager.getOrCreateAccount(player2).get();
        economyManager.getOrCreateAccount(player3).get();
        
        economyManager.deposit(player1, BigDecimal.valueOf(200.0), "Test").get(); // 300 total
        economyManager.deposit(player2, BigDecimal.valueOf(50.0), "Test").get();  // 150 total
        // player3 keeps default 100
        
        List<EconomyAccount> topPlayers = economyManager.getTopPlayers(3).get();
        
        assertEquals(3, topPlayers.size());
        assertEquals(player1, topPlayers.get(0).getPlayerId()); // Richest
        assertEquals(player2, topPlayers.get(1).getPlayerId()); // Middle
        assertEquals(player3, topPlayers.get(2).getPlayerId()); // Default balance
    }
    
    @Test
    @DisplayName("Negative amounts should be rejected")
    void testNegativeAmounts() {
        economyManager.getOrCreateAccount(testPlayerId).join();
        
        // Test deposit
        assertThrows(Exception.class, () -> {
            economyManager.deposit(testPlayerId, BigDecimal.valueOf(-10.0), "Negative deposit")
                .get(5, TimeUnit.SECONDS);
        });
        
        // Test withdrawal
        assertThrows(Exception.class, () -> {
            economyManager.withdraw(testPlayerId, BigDecimal.valueOf(-10.0), "Negative withdrawal")
                .get(5, TimeUnit.SECONDS);
        });
        
        // Test transfer
        UUID player2 = UUID.randomUUID();
        economyManager.getOrCreateAccount(player2).join();
        
        assertThrows(Exception.class, () -> {
            economyManager.transfer(testPlayerId, player2, BigDecimal.valueOf(-10.0), "Negative transfer")
                .get(5, TimeUnit.SECONDS);
        });
    }
}