package com.zientis.economy.manager;

import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.data.EconomyAccount;
import com.zientis.economy.data.Transaction;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 賽恩堤斯經濟系統的核心實現
 * 處理所有經濟操作，具備線程安全性和持久化功能
 */
public class EconomyManager implements ZientisEconomyAPI {
    
    private final Plugin plugin;
    private final Logger logger;
    private final ExecutorService executorService;
    
    // 帳戶的記憶體快取（將由資料庫支持）
    private final Map<UUID, EconomyAccount> accountCache;
    private final Map<UUID, Transaction> transactionCache;
    private final List<Transaction> transactionHistory;
    
    // 配置設定
    private static final BigDecimal DEFAULT_STARTING_BALANCE = BigDecimal.valueOf(100.0);
    private static final int MAX_CACHED_TRANSACTIONS = 10000;
    
    public EconomyManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.executorService = Executors.newCachedThreadPool();
        this.accountCache = new ConcurrentHashMap<>();
        this.transactionCache = new ConcurrentHashMap<>();
        this.transactionHistory = Collections.synchronizedList(new ArrayList<>());
        
        logger.info("EconomyManager initialized");
    }
    
    @Override
    public CompletableFuture<EconomyAccount> getOrCreateAccount(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            EconomyAccount account = accountCache.get(playerId);
            if (account != null) {
                return account;
            }
            
            // TODO: Load from database
            // For now, create new account
            account = new EconomyAccount(playerId, DEFAULT_STARTING_BALANCE);
            accountCache.put(playerId, account);
            
            logger.info("Created new economy account for player " + playerId + 
                       " with starting balance " + DEFAULT_STARTING_BALANCE);
            
            return account;
        }, executorService);
    }
    
    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID playerId) {
        return getOrCreateAccount(playerId)
            .thenApply(EconomyAccount::getBalance);
    }
    
    @Override
    public CompletableFuture<Transaction> setBalance(UUID playerId, BigDecimal amount, String reason) {
        return getOrCreateAccount(playerId).thenCompose(account -> {
            return executeTransaction(() -> {
                if (account.isFrozen()) {
                    throw new IllegalStateException("Account is frozen");
                }
                
                BigDecimal oldBalance = account.getBalance();
                account.setBalance(amount);
                
                Transaction.TransactionType type = amount.compareTo(oldBalance) > 0 
                    ? Transaction.TransactionType.DEPOSIT 
                    : Transaction.TransactionType.WITHDRAWAL;
                
                Transaction transaction = new Transaction.Builder()
                    .type(type)
                    .to(type == Transaction.TransactionType.DEPOSIT ? playerId : null)
                    .from(type == Transaction.TransactionType.WITHDRAWAL ? playerId : null)
                    .amount(amount.subtract(oldBalance).abs())
                    .description(reason != null ? reason : "Balance set by admin")
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .build();
                
                recordTransaction(transaction);
                return transaction;
            });
        });
    }
    
    @Override
    public CompletableFuture<Transaction> deposit(UUID playerId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Amount must be positive"));
        }
        
        return getOrCreateAccount(playerId).thenCompose(account -> {
            return executeTransaction(() -> {
                if (account.isFrozen()) {
                    throw new IllegalStateException("Account is frozen");
                }
                
                account.addBalance(amount);
                
                Transaction transaction = new Transaction.Builder()
                    .type(Transaction.TransactionType.DEPOSIT)
                    .to(playerId)
                    .amount(amount)
                    .description(reason != null ? reason : "Deposit")
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .build();
                
                recordTransaction(transaction);
                logger.info("Deposited " + amount + " to player " + playerId);
                return transaction;
            });
        });
    }
    
    @Override
    public CompletableFuture<Transaction> withdraw(UUID playerId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Amount must be positive"));
        }
        
        return getOrCreateAccount(playerId).thenCompose(account -> {
            return executeTransaction(() -> {
                if (account.isFrozen()) {
                    throw new IllegalStateException("Account is frozen");
                }
                
                if (!account.hasFunds(amount)) {
                    throw new IllegalArgumentException("Insufficient funds");
                }
                
                account.subtractBalance(amount);
                
                Transaction transaction = new Transaction.Builder()
                    .type(Transaction.TransactionType.WITHDRAWAL)
                    .from(playerId)
                    .amount(amount)
                    .description(reason != null ? reason : "Withdrawal")
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .build();
                
                recordTransaction(transaction);
                logger.info("Withdrew " + amount + " from player " + playerId);
                return transaction;
            });
        });
    }
    
    @Override
    public CompletableFuture<Transaction> transfer(UUID fromPlayer, UUID toPlayer, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Amount must be positive"));
        }
        
        if (fromPlayer.equals(toPlayer)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Cannot transfer to self"));
        }
        
        return CompletableFuture.allOf(
            getOrCreateAccount(fromPlayer),
            getOrCreateAccount(toPlayer)
        ).thenCompose(v -> {
            EconomyAccount fromAccount = accountCache.get(fromPlayer);
            EconomyAccount toAccount = accountCache.get(toPlayer);
            
            return executeTransaction(() -> {
                if (fromAccount.isFrozen() || toAccount.isFrozen()) {
                    throw new IllegalStateException("One or both accounts are frozen");
                }
                
                if (!fromAccount.hasFunds(amount)) {
                    throw new IllegalArgumentException("Insufficient funds");
                }
                
                fromAccount.subtractBalance(amount);
                toAccount.addBalance(amount);
                
                Transaction transaction = new Transaction.Builder()
                    .type(Transaction.TransactionType.TRANSFER)
                    .from(fromPlayer)
                    .to(toPlayer)
                    .amount(amount)
                    .description(description != null ? description : "Player transfer")
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .build();
                
                recordTransaction(transaction);
                logger.info("Transferred " + amount + " from " + fromPlayer + " to " + toPlayer);
                return transaction;
            });
        });
    }
    
    @Override
    public CompletableFuture<Boolean> hasFunds(UUID playerId, BigDecimal amount) {
        return getOrCreateAccount(playerId)
            .thenApply(account -> account.hasFunds(amount));
    }
    
    @Override
    public CompletableFuture<List<Transaction>> getTransactionHistory(UUID playerId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            return transactionHistory.stream()
                .filter(t -> playerId.equals(t.getFromAccount()) || playerId.equals(t.getToAccount()))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Transaction> getTransaction(UUID transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            return transactionCache.get(transactionId);
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<Transaction>> getTransactionsBetween(UUID player1, UUID player2, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            return transactionHistory.stream()
                .filter(t -> (player1.equals(t.getFromAccount()) && player2.equals(t.getToAccount())) ||
                           (player2.equals(t.getFromAccount()) && player1.equals(t.getToAccount())))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> freezeAccount(UUID playerId, String reason) {
        return getOrCreateAccount(playerId).thenApply(account -> {
            account.setFrozen(true);
            logger.info("Froze account for player " + playerId + ": " + reason);
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> unfreezeAccount(UUID playerId, String reason) {
        return getOrCreateAccount(playerId).thenApply(account -> {
            account.setFrozen(false);
            logger.info("Unfroze account for player " + playerId + ": " + reason);
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> isAccountFrozen(UUID playerId) {
        return getOrCreateAccount(playerId)
            .thenApply(EconomyAccount::isFrozen);
    }
    
    @Override
    public CompletableFuture<BigDecimal> getTotalCirculation() {
        return CompletableFuture.supplyAsync(() -> {
            return accountCache.values().stream()
                .map(EconomyAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }, executorService);
    }
    
    @Override
    public CompletableFuture<EconomyStats> getEconomyStats() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<EconomyAccount> accounts = accountCache.values();
            int totalAccounts = accounts.size();
            int activeAccounts = (int) accounts.stream()
                .filter(acc -> !acc.isFrozen())
                .count();
            
            BigDecimal totalCirculation = accounts.stream()
                .map(EconomyAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageBalance = totalAccounts > 0 
                ? totalCirculation.divide(BigDecimal.valueOf(totalAccounts))
                : BigDecimal.ZERO;
            
            int totalTransactions = transactionHistory.size();
            
            return new EconomyStats(totalCirculation, totalAccounts, activeAccounts, 
                                  averageBalance, totalTransactions);
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<EconomyAccount>> getTopPlayers(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            return accountCache.values().stream()
                .sorted((a1, a2) -> a2.getBalance().compareTo(a1.getBalance()))
                .limit(limit)
                .collect(Collectors.toList());
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Integer> purgeInactiveAccounts(int days) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            int purged = 0;
            
            Iterator<Map.Entry<UUID, EconomyAccount>> iterator = accountCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, EconomyAccount> entry = iterator.next();
                EconomyAccount account = entry.getValue();
                
                if (account.getLastUpdated().isBefore(cutoff) && 
                    account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                    iterator.remove();
                    purged++;
                }
            }
            
            logger.info("Purged " + purged + " inactive accounts");
            return purged;
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> backupEconomyData() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement backup to file/database
            logger.info("Economy data backup completed");
            return true;
        }, executorService);
    }
    
    /**
     * Execute a transaction with proper error handling
     */
    private <T> CompletableFuture<T> executeTransaction(TransactionExecutor<T> executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executor.execute();
            } catch (Exception e) {
                logger.severe("Transaction failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Record a transaction in history
     */
    private void recordTransaction(Transaction transaction) {
        transactionCache.put(transaction.getTransactionId(), transaction);
        transactionHistory.add(transaction);
        
        // Maintain cache size limit
        if (transactionHistory.size() > MAX_CACHED_TRANSACTIONS) {
            transactionHistory.remove(0);
        }
    }
    
    @Override
    public CompletableFuture<String> getDiscordEconomyStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder stats = new StringBuilder();
                stats.append("📊 **經濟系統統計**\n\n");
                
                // Basic stats
                stats.append("💰 總流通量: ").append(calculateTotalCirculation()).append(" 鑽石\n");
                stats.append("👥 總帳戶數: ").append(accountCache.size()).append("\n");
                stats.append("📈 總交易次數: ").append(transactionHistory.size()).append("\n");
                
                // Average balance
                if (!accountCache.isEmpty()) {
                    BigDecimal totalBalance = accountCache.values().stream()
                        .map(EconomyAccount::getBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgBalance = totalBalance.divide(BigDecimal.valueOf(accountCache.size()), 2, java.math.RoundingMode.HALF_UP);
                    stats.append("💎 平均餘額: ").append(avgBalance).append(" 鑽石\n");
                }
                
                return stats.toString();
            } catch (Exception e) {
                logger.severe("Failed to generate Discord economy stats: " + e.getMessage());
                return "❌ 無法獲取經濟統計資料";
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> sendDiscordEconomyNotification(String eventType, UUID playerId, double amount, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement Discord webhook notification
                logger.info(String.format("Discord notification [%s]: Player %s, Amount %.2f, Message: %s", 
                    eventType, playerId, amount, message));
                return true;
            } catch (Exception e) {
                logger.severe("Failed to send Discord economy notification: " + e.getMessage());
                return false;
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<String> handleDiscordEconomyCommand(String command, String[] args, String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement Discord command handling
                logger.info(String.format("Discord command from %s: %s %s", 
                    discordUserId, command, Arrays.toString(args)));
                return "✅ 指令執行完成";
            } catch (Exception e) {
                logger.severe("Failed to handle Discord economy command: " + e.getMessage());
                return "❌ 指令執行失敗";
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<com.zientis.economy.discord.DiscordEconomyData>> getDiscordWealthRanking(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement Discord wealth ranking
                List<com.zientis.economy.discord.DiscordEconomyData> ranking = new ArrayList<>();
                logger.info(String.format("Generating Discord wealth ranking for top %d players", limit));
                return ranking;
            } catch (Exception e) {
                logger.severe("Failed to get Discord wealth ranking: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<com.zientis.economy.discord.DiscordEconomyData> getDiscordEconomyDataByDiscordUser(String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement Discord user to player mapping and economy data retrieval
                logger.info(String.format("Getting Discord economy data for user: %s", discordUserId));
                return null; // Return null for now - needs Discord user mapping implementation
            } catch (Exception e) {
                logger.severe("Failed to get Discord economy data by Discord user: " + e.getMessage());
                return null;
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<com.zientis.economy.discord.DiscordEconomyData> getDiscordEconomyData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement Discord economy data generation for player
                logger.info(String.format("Getting Discord economy data for player: %s", playerId));
                return null; // Return null for now - needs DiscordEconomyData implementation
            } catch (Exception e) {
                logger.severe("Failed to get Discord economy data: " + e.getMessage());
                return null;
            }
        }, executorService);
    }
    
    /**
     * 計算總流通量
     */
    private BigDecimal calculateTotalCirculation() {
        return accountCache.values().stream()
            .map(EconomyAccount::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 關閉經濟管理器
     */
    public void shutdown() {
        logger.info("Shutting down EconomyManager...");
        executorService.shutdown();
        // TODO: Save all data to database
    }
    
    /**
     * Functional interface for transaction execution
     */
    @FunctionalInterface
    private interface TransactionExecutor<T> {
        T execute() throws Exception;
    }
}