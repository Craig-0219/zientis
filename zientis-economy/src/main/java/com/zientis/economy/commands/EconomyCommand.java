package com.zientis.economy.commands;

import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.data.EconomyAccount;
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
 * Administrative economy command
 * Usage: /economy <subcommand> [args...]
 */
public class EconomyCommand implements CommandExecutor, TabCompleter {
    
    private final ZientisEconomyAPI economyAPI;
    private final NumberFormat currencyFormat;
    
    public EconomyCommand(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("zientis.economy.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use economy admin commands!")
                .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "give":
            case "add":
                handleGive(sender, args);
                break;
            case "take":
            case "remove":
                handleTake(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "balance":
            case "bal":
                handleBalance(sender, args);
                break;
            case "freeze":
                handleFreeze(sender, args);
                break;
            case "unfreeze":
                handleUnfreeze(sender, args);
                break;
            case "stats":
                handleStats(sender);
                break;
            case "top":
                handleTop(sender, args);
                break;
            case "history":
                handleHistory(sender, args);
                break;
            case "purge":
                handlePurge(sender, args);
                break;
            case "backup":
                handleBackup(sender);
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand: " + subcommand)
                    .color(NamedTextColor.RED));
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Zientis Economy Admin Commands ===")
            .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/economy give <player> <amount> - Give money to player")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy take <player> <amount> - Take money from player")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy set <player> <amount> - Set player's balance")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy balance <player> - Check player's balance")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy freeze <player> - Freeze player's account")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy unfreeze <player> - Unfreeze player's account")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy stats - Show economy statistics")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy top [amount] - Show richest players")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy history <player> [limit] - Show transaction history")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy purge <days> - Purge inactive accounts")
            .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/economy backup - Backup economy data")
            .color(NamedTextColor.YELLOW));
    }
    
    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /economy give <player> <amount>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        BigDecimal amount = parseAmount(sender, args[2]);
        if (amount == null) return;
        
        economyAPI.deposit(target.getUniqueId(), amount, "Admin deposit by " + sender.getName())
            .thenAccept(transaction -> {
                if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                    sender.sendMessage(Component.text("✅ Gave " + formatCurrency(amount) + " to " + target.getName())
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Failed to give money")
                        .color(NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleTake(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /economy take <player> <amount>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        BigDecimal amount = parseAmount(sender, args[2]);
        if (amount == null) return;
        
        economyAPI.withdraw(target.getUniqueId(), amount, "Admin withdrawal by " + sender.getName())
            .thenAccept(transaction -> {
                if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                    sender.sendMessage(Component.text("✅ Took " + formatCurrency(amount) + " from " + target.getName())
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Failed to take money")
                        .color(NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /economy set <player> <amount>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        BigDecimal amount = parseAmount(sender, args[2]);
        if (amount == null) return;
        
        economyAPI.setBalance(target.getUniqueId(), amount, "Balance set by " + sender.getName())
            .thenAccept(transaction -> {
                if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                    sender.sendMessage(Component.text("✅ Set " + target.getName() + "'s balance to " + formatCurrency(amount))
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Failed to set balance")
                        .color(NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /economy balance <player>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        
        economyAPI.getBalance(target.getUniqueId())
            .thenAccept(balance -> {
                sender.sendMessage(Component.text(target.getName() + "'s balance: " + formatCurrency(balance))
                    .color(NamedTextColor.GREEN));
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleFreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /economy freeze <player>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        
        economyAPI.freezeAccount(target.getUniqueId(), "Frozen by " + sender.getName())
            .thenAccept(success -> {
                if (success) {
                    sender.sendMessage(Component.text("✅ Froze " + target.getName() + "'s account")
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Failed to freeze account")
                        .color(NamedTextColor.RED));
                }
            });
    }
    
    private void handleUnfreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /economy unfreeze <player>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        
        economyAPI.unfreezeAccount(target.getUniqueId(), "Unfrozen by " + sender.getName())
            .thenAccept(success -> {
                if (success) {
                    sender.sendMessage(Component.text("✅ Unfroze " + target.getName() + "'s account")
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Failed to unfreeze account")
                        .color(NamedTextColor.RED));
                }
            });
    }
    
    private void handleStats(CommandSender sender) {
        economyAPI.getEconomyStats()
            .thenAccept(stats -> {
                sender.sendMessage(Component.text("=== Economy Statistics ===")
                    .color(NamedTextColor.GOLD));
                sender.sendMessage(Component.text("Total Circulation: " + formatCurrency(stats.getTotalCirculation()))
                    .color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Total Accounts: " + stats.getTotalAccounts())
                    .color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Active Accounts: " + stats.getActiveAccounts())
                    .color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Average Balance: " + formatCurrency(stats.getAverageBalance()))
                    .color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Total Transactions: " + stats.getTotalTransactions())
                    .color(NamedTextColor.YELLOW));
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleTop(CommandSender sender, String[] args) {
        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.min(Math.max(limit, 1), 50); // Clamp between 1-50
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid number: " + args[1])
                    .color(NamedTextColor.RED));
                return;
            }
        }
        
        final int finalLimit = limit;
        economyAPI.getTopPlayers(finalLimit)
            .thenAccept(accounts -> {
                sender.sendMessage(Component.text("=== Top " + finalLimit + " Richest Players ===")
                    .color(NamedTextColor.GOLD));
                
                for (int i = 0; i < accounts.size(); i++) {
                    EconomyAccount account = accounts.get(i);
                    OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(account.getPlayerId());
                    String rank = (i + 1) + ".";
                    String balance = formatCurrency(account.getBalance());
                    
                    sender.sendMessage(Component.text(rank + " " + player.getName() + " - " + balance)
                        .color(NamedTextColor.YELLOW));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /economy history <player> [limit]")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        int limit = 10;
        
        if (args.length > 2) {
            try {
                limit = Integer.parseInt(args[2]);
                limit = Math.min(Math.max(limit, 1), 50);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid number: " + args[2])
                    .color(NamedTextColor.RED));
                return;
            }
        }
        
        economyAPI.getTransactionHistory(target.getUniqueId(), limit)
            .thenAccept(transactions -> {
                sender.sendMessage(Component.text("=== " + target.getName() + "'s Transaction History ===")
                    .color(NamedTextColor.GOLD));
                
                for (Transaction tx : transactions) {
                    String type = tx.getType().toString();
                    String amount = formatCurrency(tx.getAmount());
                    String desc = tx.getDescription();
                    
                    sender.sendMessage(Component.text(type + " " + amount + " - " + desc)
                        .color(NamedTextColor.YELLOW));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handlePurge(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /economy purge <days>")
                .color(NamedTextColor.YELLOW));
            return;
        }
        
        int days;
        try {
            days = Integer.parseInt(args[1]);
            if (days < 1) {
                sender.sendMessage(Component.text("Days must be positive!")
                    .color(NamedTextColor.RED));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid number: " + args[1])
                .color(NamedTextColor.RED));
            return;
        }
        
        economyAPI.purgeInactiveAccounts(days)
            .thenAccept(purged -> {
                sender.sendMessage(Component.text("✅ Purged " + purged + " inactive accounts")
                    .color(NamedTextColor.GREEN));
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private void handleBackup(CommandSender sender) {
        economyAPI.backupEconomyData()
            .thenAccept(success -> {
                if (success) {
                    sender.sendMessage(Component.text("✅ Economy data backup completed")
                        .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("❌ Backup failed")
                        .color(NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                sender.sendMessage(Component.text("❌ Error: " + throwable.getMessage())
                    .color(NamedTextColor.RED));
                return null;
            });
    }
    
    private BigDecimal parseAmount(CommandSender sender, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                sender.sendMessage(Component.text("Amount cannot be negative!")
                    .color(NamedTextColor.RED));
                return null;
            }
            return BigDecimal.valueOf(amount);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid amount: " + amountStr)
                .color(NamedTextColor.RED));
            return null;
        }
    }
    
    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount.doubleValue());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = List.of("give", "take", "set", "balance", "freeze", "unfreeze", 
                                             "stats", "top", "history", "purge", "backup");
            String partial = args[0].toLowerCase();
            completions = subcommands.stream()
                .filter(cmd -> cmd.startsWith(partial))
                .collect(Collectors.toList());
        } else if (args.length == 2 && !args[0].equals("stats") && !args[0].equals("backup")) {
            // Complete player names for most commands
            String partial = args[1].toLowerCase();
            completions = org.bukkit.Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}