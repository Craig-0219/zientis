package com.zientis.economy.listeners;

import com.zientis.economy.api.ZientisEconomyAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Event listener for economy-related events
 * Handles player joins and account creation
 */
public class EconomyEventListener implements Listener {
    
    private final ZientisEconomyAPI economyAPI;
    private final NumberFormat currencyFormat;
    
    public EconomyEventListener(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Ensure player has an economy account
        economyAPI.getOrCreateAccount(event.getPlayer().getUniqueId())
            .thenAccept(account -> {
                // Welcome message with balance (only for new accounts)
                if (account.getBalance().compareTo(BigDecimal.valueOf(100.0)) == 0) {
                    // This is likely a new account with starting balance
                    event.getPlayer().sendMessage(Component.text("💰 Welcome! You've received a starting balance of " + 
                        formatCurrency(account.getBalance()))
                        .color(NamedTextColor.GREEN));
                }
            })
            .exceptionally(throwable -> {
                // Log error but don't bother the player
                event.getPlayer().sendMessage(Component.text("⚠️ There was an issue setting up your economy account. Please contact an administrator.")
                    .color(NamedTextColor.YELLOW));
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