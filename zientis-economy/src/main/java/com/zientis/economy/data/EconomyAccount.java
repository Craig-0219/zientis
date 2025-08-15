package com.zientis.economy.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a player's economy account
 * Stores balance and account metadata
 */
public class EconomyAccount {
    
    private final UUID playerId;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private boolean frozen;
    
    public EconomyAccount(UUID playerId) {
        this(playerId, BigDecimal.ZERO);
    }
    
    public EconomyAccount(UUID playerId, BigDecimal initialBalance) {
        this.playerId = playerId;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.frozen = false;
    }
    
    /**
     * Get the player UUID for this account
     * @return Player UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Get current account balance
     * @return Account balance
     */
    public BigDecimal getBalance() {
        return balance;
    }
    
    /**
     * Set account balance
     * @param balance New balance
     */
    public void setBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = balance;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Add amount to account balance
     * @param amount Amount to add
     * @return New balance
     */
    public BigDecimal addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot add negative amount");
        }
        this.balance = this.balance.add(amount);
        this.lastUpdated = LocalDateTime.now();
        return this.balance;
    }
    
    /**
     * Subtract amount from account balance
     * @param amount Amount to subtract
     * @return New balance
     * @throws IllegalArgumentException if insufficient funds
     */
    public BigDecimal subtractBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot subtract negative amount");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
        this.lastUpdated = LocalDateTime.now();
        return this.balance;
    }
    
    /**
     * Check if account has sufficient funds
     * @param amount Amount to check
     * @return True if sufficient funds available
     */
    public boolean hasFunds(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * Get account creation time
     * @return Creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set account creation time (for loading from database)
     * @param createdAt Creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get last update time
     * @return Last update timestamp
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Check if account is frozen
     * @return True if account is frozen
     */
    public boolean isFrozen() {
        return frozen;
    }
    
    /**
     * Set account frozen status
     * @param frozen Frozen status
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        this.lastUpdated = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("EconomyAccount{playerId=%s, balance=%s, frozen=%s}", 
                            playerId, balance, frozen);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EconomyAccount that = (EconomyAccount) obj;
        return playerId.equals(that.playerId);
    }
    
    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}