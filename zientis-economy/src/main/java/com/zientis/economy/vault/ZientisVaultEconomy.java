package com.zientis.economy.vault;

import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.data.EconomyAccount;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Vault Economy implementation for Zientis Economy System
 * Bridges Zientis Economy with Vault API for plugin compatibility
 */
public class ZientisVaultEconomy implements Economy {
    
    private final ZientisEconomyAPI economyAPI;
    private final Logger logger;
    
    public ZientisVaultEconomy(ZientisEconomyAPI economyAPI, Logger logger) {
        this.economyAPI = economyAPI;
        this.logger = logger;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public String getName() {
        return "Zientis Economy";
    }
    
    @Override
    public boolean hasBankSupport() {
        return false; // Not implemented yet
    }
    
    @Override
    public int fractionalDigits() {
        return 2; // Support cents
    }
    
    @Override
    public String format(double amount) {
        return String.format("$%.2f", amount);
    }
    
    @Override
    public String currencyNamePlural() {
        return "credits";
    }
    
    @Override
    public String currencyNameSingular() {
        return "credit";
    }
    
    @Override
    public boolean hasAccount(String playerName) {
        // For Vault compatibility, we assume all players can have accounts
        return true;
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }
    
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }
    
    @Override
    public double getBalance(String playerName) {
        // This is a blocking call, but Vault API doesn't support async
        // In a real implementation, this should be optimized
        try {
            OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            return getBalance(player);
        } catch (Exception e) {
            logger.warning("Failed to get balance for " + playerName + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            CompletableFuture<BigDecimal> future = economyAPI.getBalance(player.getUniqueId());
            BigDecimal balance = future.get(); // This blocks - not ideal but Vault API limitation
            return balance.doubleValue();
        } catch (Exception e) {
            logger.warning("Failed to get balance for " + player.getName() + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }
    
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }
    
    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }
    
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }
    
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }
    
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        try {
            OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            return withdrawPlayer(player, amount);
        } catch (Exception e) {
            logger.warning("Failed to withdraw from " + playerName + ": " + e.getMessage());
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
            }
            
            UUID playerId = player.getUniqueId();
            BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
            
            // Check if player has sufficient funds
            CompletableFuture<Boolean> hasFunds = economyAPI.hasFunds(playerId, withdrawAmount);
            if (!hasFunds.get()) {
                double currentBalance = getBalance(player);
                return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
            
            // Perform withdrawal
            CompletableFuture<com.zientis.economy.data.Transaction> future = 
                economyAPI.withdraw(playerId, withdrawAmount, "Vault API withdrawal");
            
            com.zientis.economy.data.Transaction transaction = future.get();
            if (transaction.getStatus() == com.zientis.economy.data.Transaction.TransactionStatus.COMPLETED) {
                double newBalance = getBalance(player);
                return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
            } else {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Transaction failed");
            }
            
        } catch (Exception e) {
            logger.warning("Failed to withdraw from " + player.getName() + ": " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        try {
            OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            return depositPlayer(player, amount);
        } catch (Exception e) {
            logger.warning("Failed to deposit to " + playerName + ": " + e.getMessage());
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
            }
            
            UUID playerId = player.getUniqueId();
            BigDecimal depositAmount = BigDecimal.valueOf(amount);
            
            // Perform deposit
            CompletableFuture<com.zientis.economy.data.Transaction> future = 
                economyAPI.deposit(playerId, depositAmount, "Vault API deposit");
            
            com.zientis.economy.data.Transaction transaction = future.get();
            if (transaction.getStatus() == com.zientis.economy.data.Transaction.TransactionStatus.COMPLETED) {
                double newBalance = getBalance(player);
                return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
            } else {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Transaction failed");
            }
            
        } catch (Exception e) {
            logger.warning("Failed to deposit to " + player.getName() + ": " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }
    
    @Override
    public boolean createPlayerAccount(String playerName) {
        try {
            OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            return createPlayerAccount(player);
        } catch (Exception e) {
            logger.warning("Failed to create account for " + playerName + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        try {
            CompletableFuture<EconomyAccount> future = economyAPI.getOrCreateAccount(player.getUniqueId());
            EconomyAccount account = future.get();
            return account != null;
        } catch (Exception e) {
            logger.warning("Failed to create account for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
    
    // Bank methods - not implemented
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
    
    @Override
    public List<String> getBanks() {
        return List.of(); // Empty list - no banks supported
    }
}