package com.zientis.multiworld.api;

import com.zientis.core.data.Island;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core API for Zientis Multi-World System
 * Manages individual world instances for each island
 */
public interface ZientisMultiWorldAPI {
    
    /**
     * Create a new island world for a player
     * @param playerId The player's UUID
     * @return Future containing the created world, or null if creation failed
     */
    CompletableFuture<World> createIslandWorld(UUID playerId);
    
    /**
     * Create a new island world with specific template
     * @param playerId The player's UUID
     * @param template The world template to use
     * @return Future containing the created world, or null if creation failed
     */
    CompletableFuture<World> createIslandWorld(UUID playerId, String template);
    
    /**
     * Delete an island world
     * @param islandId The island UUID to delete
     * @return Future containing true if deletion was successful, false otherwise
     */
    CompletableFuture<Boolean> deleteIslandWorld(UUID islandId);
    
    /**
     * Get or load an island world
     * Will load the world if it's not currently loaded
     * @param islandId The island UUID
     * @return The world instance, or null if not found
     */
    World getOrLoadWorld(UUID islandId);
    
    /**
     * Schedule a world to be unloaded after a delay
     * @param islandId The island UUID
     * @param delay Delay in seconds before unloading
     */
    void scheduleWorldUnload(UUID islandId, long delay);
    
    /**
     * Force unload a world immediately
     * @param islandId The island UUID
     * @return true if unloaded successfully, false otherwise
     */
    boolean unloadWorld(UUID islandId);
    
    /**
     * Check if a world is currently loaded
     * @param islandId The island UUID
     * @return true if loaded, false otherwise
     */
    boolean isWorldLoaded(UUID islandId);
    
    /**
     * Get an island by its UUID
     * @param islandId The island UUID
     * @return The island instance, or null if not found
     */
    Island getIsland(UUID islandId);
    
    /**
     * Get an island by player UUID
     * @param playerId The player's UUID
     * @return The player's island, or null if not found
     */
    Island getPlayerIsland(UUID playerId);
    
    /**
     * Get all loaded islands
     * @return List of loaded islands
     */
    List<Island> getLoadedIslands();
    
    /**
     * Get total count of islands
     * @return Total island count
     */
    int getIslandCount();
    
    /**
     * Get count of currently loaded worlds
     * @return Loaded world count
     */
    int getLoadedWorldCount();
    
    /**
     * Get memory usage statistics
     * @return Memory usage information
     */
    MemoryStats getMemoryStats();
    
    /**
     * Memory usage statistics
     */
    interface MemoryStats {
        long getUsedMemory();
        long getTotalMemory();
        long getMaxMemory();
        double getUsagePercentage();
        int getLoadedWorlds();
    }
}