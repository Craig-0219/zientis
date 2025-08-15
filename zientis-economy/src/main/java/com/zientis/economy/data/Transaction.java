package com.zientis.economy.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a financial transaction between accounts
 * Immutable transaction record for audit trail
 */
public class Transaction {
    
    private final UUID transactionId;
    private final UUID fromAccount;
    private final UUID toAccount;
    private final BigDecimal amount;
    private final TransactionType type;
    private final String description;
    private final LocalDateTime timestamp;
    private final TransactionStatus status;
    
    /**
     * Transaction types
     */
    public enum TransactionType {
        TRANSFER,    // Player to player transfer
        DEPOSIT,     // Add money to account (admin/system)
        WITHDRAWAL,  // Remove money from account (admin/system)
        PURCHASE,    // Shop/trade purchase
        SALE,        // Shop/trade sale
        REWARD,      // Quest/achievement reward
        PENALTY      // Fine/penalty
    }
    
    /**
     * Transaction status
     */
    public enum TransactionStatus {
        PENDING,     // Transaction initiated but not complete
        COMPLETED,   // Transaction successfully completed
        FAILED,      // Transaction failed
        CANCELLED    // Transaction cancelled
    }
    
    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId != null ? builder.transactionId : UUID.randomUUID();
        this.fromAccount = builder.fromAccount;
        this.toAccount = builder.toAccount;
        this.amount = builder.amount;
        this.type = builder.type;
        this.description = builder.description;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.status = builder.status != null ? builder.status : TransactionStatus.PENDING;
    }
    
    /**
     * Get transaction ID
     * @return Transaction UUID
     */
    public UUID getTransactionId() {
        return transactionId;
    }
    
    /**
     * Get source account (null for deposits)
     * @return Source account UUID
     */
    public UUID getFromAccount() {
        return fromAccount;
    }
    
    /**
     * Get destination account (null for withdrawals)
     * @return Destination account UUID
     */
    public UUID getToAccount() {
        return toAccount;
    }
    
    /**
     * Get transaction amount
     * @return Transaction amount
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Get transaction type
     * @return Transaction type
     */
    public TransactionType getType() {
        return type;
    }
    
    /**
     * Get transaction description
     * @return Description text
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get transaction timestamp
     * @return Transaction timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get transaction status
     * @return Transaction status
     */
    public TransactionStatus getStatus() {
        return status;
    }
    
    /**
     * Create a new transaction with updated status
     * @param newStatus New status
     * @return New transaction instance with updated status
     */
    public Transaction withStatus(TransactionStatus newStatus) {
        return new Builder()
            .transactionId(this.transactionId)
            .from(this.fromAccount)
            .to(this.toAccount)
            .amount(this.amount)
            .type(this.type)
            .description(this.description)
            .timestamp(this.timestamp)
            .status(newStatus)
            .build();
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%s, from=%s, to=%s, amount=%s, type=%s, status=%s}", 
                           transactionId, fromAccount, toAccount, amount, type, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return transactionId.equals(that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }
    
    /**
     * Builder pattern for creating transactions
     */
    public static class Builder {
        private UUID transactionId;
        private UUID fromAccount;
        private UUID toAccount;
        private BigDecimal amount;
        private TransactionType type;
        private String description;
        private LocalDateTime timestamp;
        private TransactionStatus status;
        
        public Builder transactionId(UUID transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder from(UUID fromAccount) {
            this.fromAccount = fromAccount;
            return this;
        }
        
        public Builder to(UUID toAccount) {
            this.toAccount = toAccount;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder amount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }
        
        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }
        
        public Transaction build() {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            if (type == null) {
                throw new IllegalArgumentException("Transaction type is required");
            }
            if (type == TransactionType.TRANSFER && (fromAccount == null || toAccount == null)) {
                throw new IllegalArgumentException("Transfer requires both from and to accounts");
            }
            if (type == TransactionType.DEPOSIT && toAccount == null) {
                throw new IllegalArgumentException("Deposit requires to account");
            }
            if (type == TransactionType.WITHDRAWAL && fromAccount == null) {
                throw new IllegalArgumentException("Withdrawal requires from account");
            }
            
            return new Transaction(this);
        }
    }
}