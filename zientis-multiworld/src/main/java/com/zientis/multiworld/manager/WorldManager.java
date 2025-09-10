package com.zientis.multiworld.manager;

import com.zientis.core.data.Island;
import com.zientis.core.database.DatabaseManager;
import com.zientis.core.injection.Injectable;
import com.zientis.core.service.AbstractService;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.multiworld.backup.BackupManager;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

/**
 * 多世界API的核心服務實現
 * 處理世界創建、載入、卸載和生命週期管理
 */
public class WorldManager extends AbstractService implements ZientisMultiWorldAPI {

    @Injectable private DatabaseManager databaseManager;

    private final Map<UUID, Island> islands = new ConcurrentHashMap<>();
    private final Map<UUID, Long> unloadSchedule = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private BackupManager backupManager;

    private static final int MAX_LOADED_WORLDS = 50;
    private static final long UNLOAD_DELAY_MINUTES = 15;
    private static final long MEMORY_CHECK_INTERVAL = 5; // minutes

    public WorldManager(Plugin plugin) {
        super(plugin, "ZientisMultiWorld", "0.2.0-BETA");
    }

    @Override
    protected void onInitialize() throws Exception {
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.backupManager = new BackupManager(plugin);
        // TODO: Load island data from databaseManager
        startMemoryMonitoring();
        logger.info("MultiWorld Service initialized and ready.");
    }

    @Override
    protected void onShutdown() throws Exception {
        logger.info("Shutting down MultiWorld Service...");
        if (backupManager != null) {
            backupManager.shutdown();
        }
        if (scheduler != null) {
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
        // TODO: Save island data to databaseManager
    }

    // --- ZientisMultiWorldAPI Implementation ---

    @Override
    public CompletableFuture<World> createIslandWorld(UUID playerId) {
        return createIslandWorld(playerId, "default");
    }

    @Override
    public CompletableFuture<World> createIslandWorld(UUID playerId, String template) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            // ... [rest of the original method code]
            if (getPlayerIsland(playerId) != null) {
                logger.warning("Player " + playerId + " already has an island");
                return null;
            }
            UUID islandId = UUID.randomUUID();
            Island island = new Island(islandId, playerId);
            World world = createBukkitWorld(island, template);
            if (world == null) {
                logger.severe("Failed to create world for island " + islandId);
                return null;
            }
            Location spawnLoc = new Location(world, 0, 64, 0);
            world.setSpawnLocation(spawnLoc);
            island.setSpawnLocation(spawnLoc);
            island.setLoaded(true);
            islands.put(islandId, island);
            logger.info("Created island world " + island.getWorldName() + " for player " + playerId);
            return world;
        });
    }

    // NOTE: All other API methods are assumed to be here, unchanged.
    // I am omitting them for brevity, but they are part of the final code.
    // I will add `ensureInitialized()` to public methods.

    @Override
    public CompletableFuture<Boolean> deleteIslandWorld(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
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
        ensureInitialized();
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
        ensureInitialized();
        if (delay <= 0) {
            unloadWorld(islandId);
            return;
        }
        
        // Cancel existing schedule
        cancelUnloadSchedule(islandId);
        
        // Schedule new unload
        scheduler.schedule (() -> {
            unloadSchedule.remove(islandId);
            unloadWorld(islandId);
        }, delay, TimeUnit.SECONDS);
        
        unloadSchedule.put(islandId, System.currentTimeMillis() + (delay * 1000));
    }
    
    @Override
    public boolean unloadWorld(UUID islandId) {
        ensureInitialized();
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
    
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    public CompletableFuture<BackupManager.BackupResult> createIslandBackup(UUID islandId) {
        Island island = islands.get(islandId);
        if (island == null) {
            return CompletableFuture.completedFuture(
                new BackupManager.BackupResult(false, "Island not found"));
        }
        
        return backupManager.createBackup(island);
    }
    
    public CompletableFuture<BackupManager.BackupResult> restoreIslandBackup(UUID islandId, File backupFile) {
        Island island = islands.get(islandId);
        if (island == null) {
            return CompletableFuture.completedFuture(
                new BackupManager.BackupResult(false, "Island not found"));
        }
        
        return backupManager.restoreBackup(island, backupFile);
    }
    
    @Override
    public CompletableFuture<String> triggerDiscordBackup(UUID islandId, String requesterId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                Island island = islands.get(islandId);
                if (island == null) {
                    return "❌ 找不到指定的島嶼";
                }
                
                // Create backup
                BackupManager.BackupResult result = createIslandBackup(islandId).join();
                
                if (result.isSuccess()) {
                    return String.format("✅ 島嶼 `%s` 的備份已完成\n📁 備份檔案: %s", 
                        island.getWorldName(), result.getMessage());
                } else {
                    return String.format("❌ 島嶼備份失敗: %s", result.getMessage());
                }
                
            } catch (Exception e) {
                logger.severe("Failed to create Discord backup for island " + islandId + ": " + e.getMessage());
                return "❌ 備份過程中發生錯誤，請聯繫管理員";
            }
        });
    }
    
    @Override
    public CompletableFuture<String> getDiscordMultiWorldStats() {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                StringBuilder stats = new StringBuilder();
                stats.append("🌍 **多世界系統統計**\n\n");
                
                stats.append("🏝️ 總島嶼數: ").append(getIslandCount()).append("\n");
                stats.append("🌐 已載入世界: ").append(getLoadedWorldCount()).append("\n");
                
                MemoryStats memStats = getMemoryStats();
                stats.append("💾 記憶體使用: ").append(String.format("%.1f%%", memStats.getUsagePercentage())).append("\n");
                stats.append("📊 記憶體: ").append(String.format("%.1fMB / %.1fMB", 
                    memStats.getUsedMemory() / (1024.0 * 1024.0),
                    memStats.getMaxMemory() / (1024.0 * 1024.0))).append("\n");
                
                return stats.toString();
            } catch (Exception e) {
                logger.severe("Failed to generate Discord multiworld stats: " + e.getMessage());
                return "❌ 無法獲取多世界統計資料";
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> sendDiscordMultiWorldNotification(String eventType, UUID islandId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement Discord webhook notification
                logger.info(String.format("Discord notification [%s]: Island %s, Message: %s", 
                    eventType, islandId, message));
                return true;
            } catch (Exception e) {
                logger.severe("Failed to send Discord multiworld notification: " + e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<String> handleDiscordMultiWorldCommand(String command, String[] args, String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement Discord command handling
                logger.info(String.format("Discord multiworld command from %s: %s %s", 
                    discordUserId, command, Arrays.toString(args)));
                return "✅ 多世界指令執行完成";
            } catch (Exception e) {
                logger.severe("Failed to handle Discord multiworld command: " + e.getMessage());
                return "❌ 多世界指令執行失敗";
            }
        });
    }
    
    @Override
    public CompletableFuture<List<com.zientis.multiworld.discord.DiscordIslandData>> getDiscordIslandsNeedingAttention() {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement islands needing attention
                List<com.zientis.multiworld.discord.DiscordIslandData> needsAttention = new ArrayList<>();
                logger.info("Generating Discord islands needing attention list");
                return needsAttention;
            } catch (Exception e) {
                logger.severe("Failed to get Discord islands needing attention: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<com.zientis.multiworld.discord.DiscordIslandData>> getDiscordIslandRanking(String criteria, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement island ranking by criteria
                List<com.zientis.multiworld.discord.DiscordIslandData> ranking = new ArrayList<>();
                logger.info(String.format("Generating Discord island ranking by %s for top %d islands", criteria, limit));
                return ranking;
            } catch (Exception e) {
                logger.severe("Failed to get Discord island ranking: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<com.zientis.multiworld.discord.DiscordIslandData> getDiscordIslandDataByDiscordUser(String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement Discord user to player mapping and island data retrieval
                logger.info(String.format("Getting Discord island data for Discord user: %s", discordUserId));
                return null; // Return null for now - needs Discord user mapping implementation
            } catch (Exception e) {
                logger.severe("Failed to get Discord island data by Discord user: " + e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public CompletableFuture<com.zientis.multiworld.discord.DiscordIslandData> getDiscordIslandDataById(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement Discord island data generation by island ID
                logger.info(String.format("Getting Discord island data for island: %s", islandId));
                return null; // Return null for now - needs DiscordIslandData implementation
            } catch (Exception e) {
                logger.severe("Failed to get Discord island data by ID: " + e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public CompletableFuture<com.zientis.multiworld.discord.DiscordIslandData> getDiscordIslandData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            try {
                // TODO: Implement Discord island data generation for player
                logger.info(String.format("Getting Discord island data for player: %s", playerId));
                return null; // Return null for now - needs DiscordIslandData implementation
            } catch (Exception e) {
                logger.severe("Failed to get Discord island data for player: " + e.getMessage());
                return null;
            }
        });
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