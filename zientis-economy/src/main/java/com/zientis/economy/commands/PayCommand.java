package com.zientis.economy.commands;

import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.data.Transaction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Command to transfer money between players
 * Usage: /pay <player> <amount> [description]
 */
public class PayCommand implements CommandExecutor, TabCompleter {
    
    private final ZientisEconomyAPI economyAPI;
    private final NumberFormat currencyFormat;
    
    public PayCommand(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("zientis.economy.pay")) {
            player.sendMessage(Component.text("You don't have permission to transfer money!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        // Validate arguments
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /pay <player> <amount> [description]")
                .color(NamedTextColor.YELLOW));
            return true;
        }
        
        // Get target player
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(Component.text("Player '" + args[0] + "' not found!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        // Check if trying to pay themselves
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot pay yourself!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        // Parse amount
        BigDecimal amount;
        try {
            double amountDouble = Double.parseDouble(args[1]);
            if (amountDouble <= 0) {
                player.sendMessage(Component.text("Amount must be positive!")
                    .color(NamedTextColor.RED));
                return true;
            }
            amount = BigDecimal.valueOf(amountDouble);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount: " + args[1])
                .color(NamedTextColor.RED));
            return true;
        }
        
        // Build description
        final String description;
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) sb.append(" ");
                sb.append(args[i]);
            }
            description = sb.toString();
        } else {
            description = "Payment";
        }
        
        // Check if sender has sufficient funds first
        economyAPI.hasFunds(player.getUniqueId(), amount).thenAccept(hasFunds -> {
            if (!hasFunds) {
                player.sendMessage(Component.text("❌ Insufficient funds! You need " + formatCurrency(amount))
                    .color(NamedTextColor.RED));
                return;
            }
            
            // Perform the transfer
            economyAPI.transfer(player.getUniqueId(), target.getUniqueId(), amount, description)
                .thenAccept(transaction -> {
                    if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                        String formattedAmount = formatCurrency(amount);
                        
                        // Notify sender
                        player.sendMessage(Component.text("✅ Sent " + formattedAmount + " to " + target.getName())
                            .color(NamedTextColor.GREEN));
                        
                        // Notify recipient if online
                        if (target.isOnline()) {
                            Player onlineTarget = target.getPlayer();
                            onlineTarget.sendMessage(Component.text("💰 Received " + formattedAmount + " from " + player.getName())
                                .color(NamedTextColor.GREEN));
                            
                            if (!description.equals("Payment")) {
                                onlineTarget.sendMessage(Component.text("📝 Message: " + description)
                                    .color(NamedTextColor.GRAY));
                            }
                        }
                    } else {
                        player.sendMessage(Component.text("❌ Transfer failed: Transaction " + transaction.getStatus())
                            .color(NamedTextColor.RED));
                    }
                })
                .exceptionally(throwable -> {
                    player.sendMessage(Component.text("❌ Transfer failed: " + throwable.getMessage())
                        .color(NamedTextColor.RED));
                    return null;
                });
        }).exceptionally(throwable -> {
            player.sendMessage(Component.text("❌ Failed to check balance: " + throwable.getMessage())
                .color(NamedTextColor.RED));
            return null;
        });
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names
            String partial = args[0].toLowerCase();
            completions = org.bukkit.Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest common amounts
            String partial = args[1].toLowerCase();
            List<String> amounts = List.of("10", "50", "100", "500", "1000");
            completions = amounts.stream()
                .filter(amount -> amount.startsWith(partial))
                .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Suggest description starters
            completions = List.of("for", "thanks", "payment", "gift", "loan");
        }
        
        return completions;
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