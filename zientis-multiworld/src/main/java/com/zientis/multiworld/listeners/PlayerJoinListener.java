package com.zientis.multiworld.listeners;

import com.zientis.core.data.Island;
import com.zientis.multiworld.manager.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

/**
 * Handles player join events for island management
 * Can auto-create islands and provide helpful messages
 */
public class PlayerJoinListener implements Listener {
    
    private final WorldManager worldManager;
    
    public PlayerJoinListener(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an island
        Island island = worldManager.getPlayerIsland(player.getUniqueId());
        
        if (island == null) {
            // New player - welcome message
            sendWelcomeMessage(player);
        } else {
            // Existing player - welcome back message
            sendWelcomeBackMessage(player, island);
        }
    }
    
    private void sendWelcomeMessage(Player player) {
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
        player.sendMessage(Component.text("🌌 Welcome to Zientis!", NamedTextColor.GOLD));
        player.sendMessage(Component.text("The revolutionary Minecraft skyblock MMO server", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
        player.sendMessage(Component.text("🏝️ You don't have an island yet!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Use /island create to start your adventure!", NamedTextColor.GREEN));
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
    }
    
    private void sendWelcomeBackMessage(Player player, Island island) {
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
        player.sendMessage(Component.text("🌌 Welcome back to Zientis, " + player.getName() + "!", NamedTextColor.GOLD));
        player.sendMessage(Component.text("🏝️ Your island (Level " + island.getLevel() + ") awaits!", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Use /island home to return to your island", NamedTextColor.GREEN));
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
    }
}