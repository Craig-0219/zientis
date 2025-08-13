package com.zientis.multiworld;

import com.zientis.core.api.ZientisAPI;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.multiworld.commands.IslandCommand;
import com.zientis.multiworld.listeners.PlayerJoinListener;
import com.zientis.multiworld.manager.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Zientis Multi-World System
 * Handles plugin lifecycle and core system initialization
 */
public class ZientisMultiWorldPlugin extends JavaPlugin {
    
    private WorldManager worldManager;
    private static ZientisMultiWorldAPI api;
    
    @Override
    public void onEnable() {
        getLogger().info("Starting Zientis Multi-World System v" + getDescription().getVersion());
        
        // Initialize core API if not already done
        if (!ZientisAPI.isInitialized()) {
            ZientisAPI.initialize(this);
        }
        
        // Initialize world manager
        worldManager = new WorldManager(this);
        api = worldManager;
        
        // Register commands
        registerCommands();
        
        // Register event listeners
        registerListeners();
        
        // Save default configuration
        saveDefaultConfig();
        
        getLogger().info("Zientis Multi-World System enabled successfully!");
        getLogger().info("System ready to manage individual island worlds");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Shutting down Zientis Multi-World System...");
        
        if (worldManager != null) {
            worldManager.shutdown();
        }
        
        getLogger().info("Zientis Multi-World System disabled.");
    }
    
    /**
     * Get the Multi-World API instance
     * @return The API instance
     */
    public static ZientisMultiWorldAPI getAPI() {
        return api;
    }
    
    /**
     * Get the World Manager instance
     * @return The world manager
     */
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    private void registerCommands() {
        // Register island command
        IslandCommand islandCommand = new IslandCommand(worldManager);
        getCommand("island").setExecutor(islandCommand);
        getCommand("island").setTabCompleter(islandCommand);
        
        getLogger().info("Commands registered: /island");
    }
    
    private void registerListeners() {
        // Register player join listener for island management
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(worldManager), this);
        
        getLogger().info("Event listeners registered");
    }
}