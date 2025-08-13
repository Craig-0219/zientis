package com.zientis.core.api;

import org.bukkit.plugin.Plugin;

/**
 * Main API entry point for Zientis Server
 * Provides access to all core systems and managers
 */
public final class ZientisAPI {
    
    private static ZientisAPI instance;
    private final Plugin plugin;
    
    private ZientisAPI(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the Zientis API
     * @param plugin The main plugin instance
     */
    public static void initialize(Plugin plugin) {
        if (instance == null) {
            instance = new ZientisAPI(plugin);
        }
    }
    
    /**
     * Get the Zientis API instance
     * @return The API instance
     * @throws IllegalStateException if not initialized
     */
    public static ZientisAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ZientisAPI not initialized! Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Get the main plugin instance
     * @return The plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }
    
    /**
     * Check if the API is initialized
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return instance != null;
    }
}