package com.zientis.economy.api;

import com.zientis.economy.data.EconomyAccount;
import com.zientis.economy.data.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main API interface for Zientis Economy System
 * Provides comprehensive economy management functionality
 */
public interface ZientisEconomyAPI {
    
    // ============ Account Management ============
    
    /**
     * Get or create an economy account for a player
     * @param playerId Player UUID
     * @return CompletableFuture containing the player's economy account
     */
    CompletableFuture<EconomyAccount> getOrCreateAccount(UUID playerId);
    
    /**
     * Get player's current balance
     * @param playerId Player UUID
     * @return CompletableFuture containing the balance
     */
    CompletableFuture<BigDecimal> getBalance(UUID playerId);
    
    /**
     * Set player's balance (admin function)
     * @param playerId Player UUID
     * @param amount New balance amount
     * @param reason Reason for balance change
     * @return CompletableFuture containing the transaction result
     */
    CompletableFuture<Transaction> setBalance(UUID playerId, BigDecimal amount, String reason);
    
    /**
     * Add money to player's account
     * @param playerId Player UUID
     * @param amount Amount to add
     * @param reason Reason for the deposit
     * @return CompletableFuture containing the transaction result
     */
    CompletableFuture<Transaction> deposit(UUID playerId, BigDecimal amount, String reason);
    
    /**
     * Remove money from player's account
     * @param playerId Player UUID
     * @param amount Amount to withdraw
     * @param reason Reason for the withdrawal
     * @return CompletableFuture containing the transaction result
     */
    CompletableFuture<Transaction> withdraw(UUID playerId, BigDecimal amount, String reason);
    
    // ============ Transfer System ============
    
    /**
     * Transfer money between players
     * @param fromPlayer Source player UUID
     * @param toPlayer Destination player UUID
     * @param amount Amount to transfer
     * @param description Transfer description
     * @return CompletableFuture containing the transaction result
     */
    CompletableFuture<Transaction> transfer(UUID fromPlayer, UUID toPlayer, BigDecimal amount, String description);
    
    /**
     * Check if player has sufficient funds
     * @param playerId Player UUID
     * @param amount Amount to check
     * @return CompletableFuture containing true if sufficient funds
     */
    CompletableFuture<Boolean> hasFunds(UUID playerId, BigDecimal amount);
    
    // ============ Transaction History ============
    
    /**
     * Get transaction history for a player
     * @param playerId Player UUID
     * @param limit Maximum number of transactions to return
     * @return CompletableFuture containing list of transactions
     */
    CompletableFuture<List<Transaction>> getTransactionHistory(UUID playerId, int limit);
    
    /**
     * Get transaction by ID
     * @param transactionId Transaction UUID
     * @return CompletableFuture containing the transaction or null if not found
     */
    CompletableFuture<Transaction> getTransaction(UUID transactionId);
    
    /**
     * Get all transactions between two players
     * @param player1 First player UUID
     * @param player2 Second player UUID
     * @param limit Maximum number of transactions to return
     * @return CompletableFuture containing list of transactions
     */
    CompletableFuture<List<Transaction>> getTransactionsBetween(UUID player1, UUID player2, int limit);
    
    // ============ Account Management ============
    
    /**
     * Freeze a player's account (prevent transactions)
     * @param playerId Player UUID
     * @param reason Reason for freezing
     * @return CompletableFuture containing true if successful
     */
    CompletableFuture<Boolean> freezeAccount(UUID playerId, String reason);
    
    /**
     * Unfreeze a player's account
     * @param playerId Player UUID
     * @param reason Reason for unfreezing
     * @return CompletableFuture containing true if successful
     */
    CompletableFuture<Boolean> unfreezeAccount(UUID playerId, String reason);
    
    /**
     * Check if account is frozen
     * @param playerId Player UUID
     * @return CompletableFuture containing true if account is frozen
     */
    CompletableFuture<Boolean> isAccountFrozen(UUID playerId);
    
    // ============ Statistics ============
    
    /**
     * Get total money in circulation
     * @return CompletableFuture containing total circulation amount
     */
    CompletableFuture<BigDecimal> getTotalCirculation();
    
    /**
     * Get economy statistics
     * @return CompletableFuture containing economy statistics
     */
    CompletableFuture<EconomyStats> getEconomyStats();
    
    /**
     * Get top richest players
     * @param limit Number of top players to return
     * @return CompletableFuture containing list of accounts ordered by balance
     */
    CompletableFuture<List<EconomyAccount>> getTopPlayers(int limit);
    
    // ============ Administrative ============
    
    /**
     * Purge inactive accounts older than specified days
     * @param days Minimum days of inactivity
     * @return CompletableFuture containing number of purged accounts
     */
    CompletableFuture<Integer> purgeInactiveAccounts(int days);
    
    /**
     * Backup economy data
     * @return CompletableFuture containing backup result
     */
    CompletableFuture<Boolean> backupEconomyData();
    
    // ============ Discord Bot API ============
    
    /**
     * Get Discord-formatted economy data for a player
     * @param playerId Player UUID
     * @return CompletableFuture containing Discord economy data
     */
    CompletableFuture<com.zientis.economy.discord.DiscordEconomyData> getDiscordEconomyData(UUID playerId);
    
    /**
     * Get Discord-formatted economy data by Discord user ID
     * @param discordUserId Discord user ID
     * @return CompletableFuture containing Discord economy data
     */
    CompletableFuture<com.zientis.economy.discord.DiscordEconomyData> getDiscordEconomyDataByDiscordUser(String discordUserId);
    
    /**
     * Get Discord-formatted wealth ranking
     * @param limit Number of top players to return
     * @return CompletableFuture containing list of Discord economy data
     */
    CompletableFuture<List<com.zientis.economy.discord.DiscordEconomyData>> getDiscordWealthRanking(int limit);
    
    /**
     * Handle Discord command for economy system
     * @param command Command name
     * @param args Command arguments
     * @param discordUserId Discord user ID
     * @return CompletableFuture containing command execution result
     */
    CompletableFuture<String> handleDiscordEconomyCommand(String command, String[] args, String discordUserId);
    
    /**
     * Send Discord webhook notification for economy events
     * @param eventType Event type (transfer, large_transaction, etc.)
     * @param playerId Related player ID
     * @param amount Transaction amount
     * @param message Notification message
     * @return CompletableFuture containing success status
     */
    CompletableFuture<Boolean> sendDiscordEconomyNotification(String eventType, UUID playerId, double amount, String message);
    
    /**
     * Get Discord-formatted server economy statistics
     * @return CompletableFuture containing Discord-formatted economy stats
     */
    CompletableFuture<String> getDiscordEconomyStats();
    
    /**
     * Economy statistics holder
     */
    class EconomyStats {
        private final BigDecimal totalCirculation;
        private final int totalAccounts;
        private final int activeAccounts;
        private final BigDecimal averageBalance;
        private final int totalTransactions;
        
        public EconomyStats(BigDecimal totalCirculation, int totalAccounts, int activeAccounts, 
                           BigDecimal averageBalance, int totalTransactions) {
            this.totalCirculation = totalCirculation;
            this.totalAccounts = totalAccounts;
            this.activeAccounts = activeAccounts;
            this.averageBalance = averageBalance;
            this.totalTransactions = totalTransactions;
        }
        
        public BigDecimal getTotalCirculation() { return totalCirculation; }
        public int getTotalAccounts() { return totalAccounts; }
        public int getActiveAccounts() { return activeAccounts; }
        public BigDecimal getAverageBalance() { return averageBalance; }
        public int getTotalTransactions() { return totalTransactions; }
    }
}