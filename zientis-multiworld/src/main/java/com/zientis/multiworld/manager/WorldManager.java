package com.zientis.multiworld.manager;

import com.zientis.core.api.ZientisAPI;
import com.zientis.core.data.Island;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.multiworld.backup.BackupManager;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Core implementation of the Multi-World API
 * Handles world creation, loading, unloading and lifecycle management
 */
public class WorldManager implements ZientisMultiWorldAPI {
    
    private final Plugin plugin;
    private final Logger logger;
    private final Map<UUID, Island> islands;
    private final Map<UUID, Long> unloadSchedule;
    private final ScheduledExecutorService scheduler;
    private final BackupManager backupManager;
    
    // Configuration
    private static final int MAX_LOADED_WORLDS = 50;
    private static final long UNLOAD_DELAY_MINUTES = 15;
    private static final long MEMORY_CHECK_INTERVAL = 5; // minutes
    
    public WorldManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.islands = new ConcurrentHashMap<>();
        this.unloadSchedule = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.backupManager = new BackupManager(plugin);
        
        // Start memory monitoring
        startMemoryMonitoring();
    }
    
    @Override
    public CompletableFuture<World> createIslandWorld(UUID playerId) {
        return createIslandWorld(playerId, "default");
    }
    
    @Override
    public CompletableFuture<World> createIslandWorld(UUID playerId, String template) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if player already has an island
                if (getPlayerIsland(playerId) != null) {
                    logger.warning("Player " + playerId + " already has an island");
                    return null;
                }
                
                UUID islandId = UUID.randomUUID();
                Island island = new Island(islandId, playerId);
                
                // Create world
                World world = createBukkitWorld(island, template);
                if (world == null) {
                    logger.severe("Failed to create world for island " + islandId);
                    return null;
                }
                
                // Set spawn location
                Location spawnLoc = new Location(world, 0, 64, 0);
                world.setSpawnLocation(spawnLoc);
                island.setSpawnLocation(spawnLoc);
                island.setLoaded(true);
                
                // Store island data
                islands.put(islandId, island);
                
                logger.info("Created island world " + island.getWorldName() + " for player " + playerId);
                return world;
                
            } catch (Exception e) {
                logger.severe("Error creating island world for player " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> deleteIslandWorld(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Island island = islands.get(islandId);
                if (island == null) {
                    logger.warning("Attempted to delete non-existent island " + islandId);
                    return false;
                }
                
                // Unload world first
                boolean unloaded = unloadWorld(islandId);
                if (!unloaded) {
                    logger.warning("Failed to unload world before deletion: " + islandId);
                }
                
                // Delete world files
                File worldFolder = new File(Bukkit.getWorldContainer(), island.getWorldName());
                boolean deleted = deleteWorldFolder(worldFolder);
                
                if (deleted) {
                    islands.remove(islandId);
                    logger.info("Deleted island world " + island.getWorldName());
                    return true;
                } else {
                    logger.severe("Failed to delete world folder for island " + islandId);
                    return false;
                }
                
            } catch (Exception e) {
                logger.severe("Error deleting island world " + islandId + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    @Override
    public World getOrLoadWorld(UUID islandId) {
        Island island = islands.get(islandId);
        if (island == null) {
            return null;
        }
        
        World world = Bukkit.getWorld(island.getWorldName());
        if (world != null) {
            island.setLoaded(true);
            island.updateLastVisited();
            cancelUnloadSchedule(islandId);
            return world;
        }
        
        // World not loaded, load it
        return loadWorld(island);
    }
    
    @Override
    public void scheduleWorldUnload(UUID islandId, long delay) {
        if (delay <= 0) {
            unloadWorld(islandId);
            return;
        }
        
        // Cancel existing schedule
        cancelUnloadSchedule(islandId);
        
        // Schedule new unload
        scheduler.schedule(() -> {
            unloadSchedule.remove(islandId);
            unloadWorld(islandId);
        }, delay, TimeUnit.SECONDS);
        
        unloadSchedule.put(islandId, System.currentTimeMillis() + (delay * 1000));
    }
    
    @Override
    public boolean unloadWorld(UUID islandId) {
        Island island = islands.get(islandId);
        if (island == null || !island.isLoaded()) {
            return true; // Already unloaded or doesn't exist
        }
        
        World world = Bukkit.getWorld(island.getWorldName());
        if (world == null) {
            island.setLoaded(false);
            return true;
        }
        
        // Check if any players are in the world
        if (!world.getPlayers().isEmpty()) {
            logger.info("Cannot unload world " + island.getWorldName() + " - players still present");
            return false;
        }
        
        boolean unloaded = Bukkit.unloadWorld(world, true);
        if (unloaded) {
            island.setLoaded(false);
            cancelUnloadSchedule(islandId);
            logger.info("Unloaded world " + island.getWorldName());
        }
        
        return unloaded;
    }
    
    @Override
    public boolean isWorldLoaded(UUID islandId) {
        Island island = islands.get(islandId);
        return island != null && island.isLoaded() && Bukkit.getWorld(island.getWorldName()) != null;
    }
    
    @Override
    public Island getIsland(UUID islandId) {
        return islands.get(islandId);
    }
    
    @Override
    public Island getPlayerIsland(UUID playerId) {
        return islands.values().stream()
                .filter(island -> island.getOwnerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public List<Island> getLoadedIslands() {
        return islands.values().stream()
                .filter(Island::isLoaded)
                .toList();
    }
    
    @Override
    public int getIslandCount() {
        return islands.size();
    }
    
    @Override
    public int getLoadedWorldCount() {
        return (int) islands.values().stream().filter(Island::isLoaded).count();
    }
    
    @Override
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryStatsImpl(
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.totalMemory(),
                runtime.maxMemory(),
                getLoadedWorldCount()
        );
    }
    
    private World createBukkitWorld(Island island, String template) {
        WorldCreator creator = new WorldCreator(island.getWorldName());
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.generateStructures(false);
        
        // Set a unique seed for each island
        creator.seed(island.getIslandId().hashCode());
        
        return Bukkit.createWorld(creator);
    }
    
    private World loadWorld(Island island) {
        try {
            File worldFolder = new File(Bukkit.getWorldContainer(), island.getWorldName());
            if (!worldFolder.exists()) {
                logger.warning("World folder does not exist for island " + island.getIslandId());
                return null;
            }
            
            WorldCreator creator = new WorldCreator(island.getWorldName());
            World world = Bukkit.createWorld(creator);
            
            if (world != null) {
                island.setLoaded(true);
                island.updateLastVisited();
                logger.info("Loaded world " + island.getWorldName());
            }
            
            return world;
            
        } catch (Exception e) {
            logger.severe("Error loading world for island " + island.getIslandId() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private boolean deleteWorldFolder(File folder) {
        if (!folder.exists()) {
            return true;
        }
        
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        
        return folder.delete();
    }
    
    private void cancelUnloadSchedule(UUID islandId) {
        unloadSchedule.remove(islandId);
    }
    
    private void startMemoryMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            MemoryStats stats = getMemoryStats();
            double usagePercent = stats.getUsagePercentage();
            
            // If memory usage is high, unload inactive worlds
            if (usagePercent > 80.0) {
                logger.warning(String.format("High memory usage: %.1f%% - Starting emergency world cleanup", usagePercent));
                performEmergencyCleanup();
            } else if (usagePercent > 60.0) {
                logger.info(String.format("Memory usage: %.1f%% - Performing routine cleanup", usagePercent));
                performRoutineCleanup();
            }
            
            // Log statistics
            if (getLoadedWorldCount() > 0) {
                logger.info(String.format("Worlds: %d loaded, %d total | Memory: %.1f%% (%.1fMB/%.1fMB)", 
                        getLoadedWorldCount(), getIslandCount(), usagePercent,
                        stats.getUsedMemory() / (1024.0 * 1024.0),
                        stats.getMaxMemory() / (1024.0 * 1024.0)));
            }
            
        }, MEMORY_CHECK_INTERVAL, MEMORY_CHECK_INTERVAL, TimeUnit.MINUTES);
    }
    
    private void performEmergencyCleanup() {
        // Unload worlds with no players immediately
        getLoadedIslands().stream()
                .filter(island -> {
                    World world = Bukkit.getWorld(island.getWorldName());
                    return world != null && world.getPlayers().isEmpty();
                })
                .limit(10) // Limit to prevent too much work at once
                .forEach(island -> unloadWorld(island.getIslandId()));
    }
    
    private void performRoutineCleanup() {
        // Schedule unload for worlds that haven't been visited recently
        getLoadedIslands().stream()
                .filter(island -> {
                    World world = Bukkit.getWorld(island.getWorldName());
                    return world != null && world.getPlayers().isEmpty();
                })
                .limit(5)
                .forEach(island -> scheduleWorldUnload(island.getIslandId(), UNLOAD_DELAY_MINUTES * 60));
    }
    
    /**
     * Get the backup manager instance
     * @return The backup manager
     */
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    /**
     * Create a backup of an island world
     * @param islandId The island UUID to backup
     * @return Future containing the backup result
     */
    public CompletableFuture<BackupManager.BackupResult> createIslandBackup(UUID islandId) {
        Island island = islands.get(islandId);
        if (island == null) {
            return CompletableFuture.completedFuture(
                new BackupManager.BackupResult(false, "Island not found"));
        }
        
        return backupManager.createBackup(island);
    }
    
    /**
     * Restore an island from backup
     * @param islandId The island UUID to restore
     * @param backupFile The backup file to restore from
     * @return Future containing the restore result
     */
    public CompletableFuture<BackupManager.BackupResult> restoreIslandBackup(UUID islandId, File backupFile) {
        Island island = islands.get(islandId);
        if (island == null) {
            return CompletableFuture.completedFuture(
                new BackupManager.BackupResult(false, "Island not found"));
        }
        
        return backupManager.restoreBackup(island, backupFile);
    }
    
    public void shutdown() {
        logger.info("Shutting down WorldManager...");
        
        // Shutdown backup manager first
        if (backupManager != null) {
            backupManager.shutdown();
        }
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("WorldManager scheduler did not terminate gracefully");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class MemoryStatsImpl implements MemoryStats {
        private final long usedMemory;
        private final long totalMemory;
        private final long maxMemory;
        private final int loadedWorlds;
        
        public MemoryStatsImpl(long usedMemory, long totalMemory, long maxMemory, int loadedWorlds) {
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.maxMemory = maxMemory;
            this.loadedWorlds = loadedWorlds;
        }
        
        @Override
        public long getUsedMemory() { return usedMemory; }
        
        @Override
        public long getTotalMemory() { return totalMemory; }
        
        @Override
        public long getMaxMemory() { return maxMemory; }
        
        @Override
        public double getUsagePercentage() {
            return (usedMemory / (double) maxMemory) * 100.0;
        }
        
        @Override
        public int getLoadedWorlds() { return loadedWorlds; }
    }
}