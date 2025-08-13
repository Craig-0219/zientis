package com.zientis.multiworld.commands;

import com.zientis.core.data.Island;
import com.zientis.multiworld.manager.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main command handler for island management
 * Provides create, delete, home, info, and list functionality
 */
public class IslandCommand implements CommandExecutor, TabCompleter {
    
    private final WorldManager worldManager;
    
    public IslandCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(player);
            case "delete", "remove" -> handleDelete(player);
            case "home", "h" -> handleHome(player);
            case "info", "i" -> handleInfo(player);
            case "list", "l" -> handleList(player);
            case "help" -> sendUsage(player);
            default -> {
                player.sendMessage(Component.text("Unknown subcommand: " + subCommand, NamedTextColor.RED));
                sendUsage(player);
            }
        }
        
        return true;
    }
    
    private void handleCreate(Player player) {
        if (!player.hasPermission("zientis.island.create")) {
            player.sendMessage(Component.text("You don't have permission to create an island!", NamedTextColor.RED));
            return;
        }
        
        // Check if player already has an island
        Island existingIsland = worldManager.getPlayerIsland(player.getUniqueId());
        if (existingIsland != null) {
            player.sendMessage(Component.text("You already have an island!", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("Creating your island...", NamedTextColor.YELLOW));
        
        worldManager.createIslandWorld(player.getUniqueId()).thenAccept(world -> {
            if (world != null) {
                // Teleport player to their new island
                player.teleport(world.getSpawnLocation());
                player.sendMessage(Component.text("✓ Island created successfully! Welcome to your new home!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("✗ Failed to create island. Please try again or contact an admin.", NamedTextColor.RED));
            }
        });
    }
    
    private void handleDelete(Player player) {
        if (!player.hasPermission("zientis.island.delete")) {
            player.sendMessage(Component.text("You don't have permission to delete an island!", NamedTextColor.RED));
            return;
        }
        
        Island island = worldManager.getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("You don't have an island to delete!", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("Deleting your island...", NamedTextColor.YELLOW));
        
        worldManager.deleteIslandWorld(island.getIslandId()).thenAccept(success -> {
            if (success) {
                player.sendMessage(Component.text("✓ Island deleted successfully!", NamedTextColor.GREEN));
                
                // Teleport player to spawn if they were on their island
                World currentWorld = player.getWorld();
                if (currentWorld.getName().equals(island.getWorldName())) {
                    player.teleport(player.getServer().getWorlds().get(0).getSpawnLocation());
                }
            } else {
                player.sendMessage(Component.text("✗ Failed to delete island. Please try again or contact an admin.", NamedTextColor.RED));
            }
        });
    }
    
    private void handleHome(Player player) {
        if (!player.hasPermission("zientis.island.home")) {
            player.sendMessage(Component.text("You don't have permission to use island home!", NamedTextColor.RED));
            return;
        }
        
        Island island = worldManager.getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("You don't have an island! Use /island create to create one.", NamedTextColor.RED));
            return;
        }
        
        World world = worldManager.getOrLoadWorld(island.getIslandId());
        if (world == null) {
            player.sendMessage(Component.text("Failed to load your island world. Please contact an admin.", NamedTextColor.RED));
            return;
        }
        
        player.teleport(island.getSpawnLocation() != null ? island.getSpawnLocation() : world.getSpawnLocation());
        player.sendMessage(Component.text("Welcome back to your island!", NamedTextColor.GREEN));
    }
    
    private void handleInfo(Player player) {
        if (!player.hasPermission("zientis.island.info")) {
            player.sendMessage(Component.text("You don't have permission to view island info!", NamedTextColor.RED));
            return;
        }
        
        Island island = worldManager.getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("You don't have an island! Use /island create to create one.", NamedTextColor.RED));
            return;
        }
        
        // Build info message
        player.sendMessage(Component.text("=== Island Information ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Island ID: " + island.getIslandId().toString().substring(0, 8) + "...", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Level: " + island.getLevel(), NamedTextColor.AQUA));
        player.sendMessage(Component.text("Created: " + island.getCreatedAt().toString(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("World: " + island.getWorldName(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Status: " + (island.isLoaded() ? "Loaded" : "Unloaded"), 
                island.isLoaded() ? NamedTextColor.GREEN : NamedTextColor.RED));
    }
    
    private void handleList(Player player) {
        if (!player.hasPermission("zientis.island.list")) {
            player.sendMessage(Component.text("You don't have permission to list islands!", NamedTextColor.RED));
            return;
        }
        
        int totalIslands = worldManager.getIslandCount();
        int loadedIslands = worldManager.getLoadedWorldCount();
        
        player.sendMessage(Component.text("=== Island Statistics ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Total Islands: " + totalIslands, NamedTextColor.AQUA));
        player.sendMessage(Component.text("Loaded Islands: " + loadedIslands, NamedTextColor.GREEN));
        
        if (player.hasPermission("zientis.admin.list")) {
            var memStats = worldManager.getMemoryStats();
            player.sendMessage(Component.text("Memory Usage: " + String.format("%.1f%%", memStats.getUsagePercentage()), 
                    memStats.getUsagePercentage() > 80 ? NamedTextColor.RED : NamedTextColor.GREEN));
        }
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(Component.text("=== Island Commands ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/island create - Create a new island", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/island delete - Delete your island", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/island home - Teleport to your island", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/island info - View island information", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/island list - View island statistics", NamedTextColor.GRAY));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            
            for (String subCmd : Arrays.asList("create", "delete", "home", "info", "list", "help")) {
                if (subCmd.startsWith(partial)) {
                    completions.add(subCmd);
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}