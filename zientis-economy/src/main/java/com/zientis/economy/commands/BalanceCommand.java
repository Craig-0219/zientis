package com.zientis.economy.commands;

import com.zientis.economy.api.ZientisEconomyAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Command to check player balance
 * Usage: /balance [player]
 */
public class BalanceCommand implements CommandExecutor {
    
    private final ZientisEconomyAPI economyAPI;
    private final NumberFormat currencyFormat;
    
    public BalanceCommand(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if checking another player's balance
        if (args.length > 0) {
            // Requires permission to check other players
            if (!sender.hasPermission("zientis.economy.balance.others")) {
                sender.sendMessage(Component.text("You don't have permission to check other players' balance!")
                    .color(NamedTextColor.RED));
                return true;
            }
            
            OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[0]);
            showBalance(sender, target, false);
            return true;
        }
        
        // Show own balance
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can check their balance!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        showBalance(sender, player, true);
        return true;
    }
    
    /**
     * Show balance information to the sender
     * @param sender Command sender
     * @param target Target player
     * @param isOwnBalance True if checking own balance
     */
    private void showBalance(CommandSender sender, OfflinePlayer target, boolean isOwnBalance) {
        economyAPI.getBalance(target.getUniqueId()).thenAccept(balance -> {
            String formattedBalance = formatCurrency(balance);
            
            if (isOwnBalance) {
                sender.sendMessage(Component.text("💰 Your balance: " + formattedBalance)
                    .color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("💰 " + target.getName() + "'s balance: " + formattedBalance)
                    .color(NamedTextColor.YELLOW));
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(Component.text("❌ Failed to get balance: " + throwable.getMessage())
                .color(NamedTextColor.RED));
            return null;
        });
    }
    
    /**
     * Format currency amount for display
     * @param amount Amount to format
     * @return Formatted currency string
     */
    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount.doubleValue());
    }
}