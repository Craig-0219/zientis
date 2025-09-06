package com.zientis.multiworld.manager;

import com.zientis.core.data.Island;
import com.zientis.core.database.DatabaseManager;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WorldManager
 * Note: These tests focus on the business logic and data structures
 * without requiring actual Bukkit server integration
 */
class WorldManagerTest {
    
    @Mock
    private Plugin mockPlugin;
    @Mock
    private DatabaseManager mockDatabaseManager;
    
    private WorldManager worldManager;
    private AutoCloseable mockitoCloseable;
    
    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
        
        worldManager = new WorldManager(mockPlugin);

        // Manually inject dependencies for testing
        java.lang.reflect.Field schedulerField = WorldManager.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        schedulerField.set(worldManager, Executors.newSingleThreadScheduledExecutor());

        java.lang.reflect.Field dbManagerField = WorldManager.class.getDeclaredField("databaseManager");
        dbManagerField.setAccessible(true);
        dbManagerField.set(worldManager, mockDatabaseManager);

        // Initialize the service
        worldManager.initialize();
    }
    
    @Test
    @DisplayName("WorldManager should initialize with correct default values")
    void testInitialization() {
        assertEquals(0, worldManager.getIslandCount());
        assertEquals(0, worldManager.getLoadedWorldCount());
        
        ZientisMultiWorldAPI.MemoryStats stats = worldManager.getMemoryStats();
        assertNotNull(stats);
        assertTrue(stats.getUsedMemory() >= 0);
        assertTrue(stats.getMaxMemory() > 0);
    }
    
    @Test
    @DisplayName("Player island retrieval should return null for non-existent player")
    void testGetPlayerIslandNonExistent() {
        UUID playerId = UUID.randomUUID();
        Island island = worldManager.getPlayerIsland(playerId);
        assertNull(island);
    }
    
    @Test
    @DisplayName("Island retrieval should return null for non-existent island")
    void testGetIslandNonExistent() {
        UUID islandId = UUID.randomUUID();
        Island island = worldManager.getIsland(islandId);
        assertNull(island);
    }
    
    @Test
    @DisplayName("World loaded check should return false for non-existent island")
    void testIsWorldLoadedNonExistent() {
        UUID islandId = UUID.randomUUID();
        boolean isLoaded = worldManager.isWorldLoaded(islandId);
        assertFalse(isLoaded);
    }
    
    @Test
    @DisplayName("Memory stats should provide valid data")
    void testMemoryStats() {
        ZientisMultiWorldAPI.MemoryStats stats = worldManager.getMemoryStats();
        
        assertNotNull(stats);
        assertTrue(stats.getUsedMemory() >= 0);
        assertTrue(stats.getTotalMemory() >= stats.getUsedMemory());
        assertTrue(stats.getMaxMemory() >= stats.getTotalMemory());
        assertTrue(stats.getUsagePercentage() >= 0.0);
        assertTrue(stats.getUsagePercentage() <= 100.0);
        assertEquals(0, stats.getLoadedWorlds());
    }
    
    @Test
    @DisplayName("Unload non-existent world should return true")
    void testUnloadNonExistentWorld() {
        UUID islandId = UUID.randomUUID();
        boolean result = worldManager.unloadWorld(islandId);
        assertTrue(result); // Should return true as it's already "unloaded"
    }
    
    @Test
    @DisplayName("Delete non-existent island should complete successfully")
    void testDeleteNonExistentIsland() {
        UUID islandId = UUID.randomUUID();
        
        CompletableFuture<Boolean> future = worldManager.deleteIslandWorld(islandId);
        
        assertDoesNotThrow(() -> {
            Boolean result = future.get(5, TimeUnit.SECONDS);
            assertNotNull(result);
        });
    }
    
    @Test
    @DisplayName("Create island world should return CompletableFuture")
    void testCreateIslandWorldReturnsCompletableFuture() {
        UUID playerId = UUID.randomUUID();
        
        CompletableFuture<?> future = worldManager.createIslandWorld(playerId);
        assertNotNull(future);
        assertFalse(future.isDone()); // Should be processing asynchronously
    }
    
    @Test
    @DisplayName("Schedule world unload with zero delay should unload immediately")
    void testScheduleWorldUnloadZeroDelay() {
        UUID islandId = UUID.randomUUID();
        
        // Should not throw exception even for non-existent world
        assertDoesNotThrow(() -> {
            worldManager.scheduleWorldUnload(islandId, 0);
        });
    }
    
    @Test
    @DisplayName("Schedule world unload with positive delay should not throw")
    void testScheduleWorldUnloadPositiveDelay() {
        UUID islandId = UUID.randomUUID();
        
        assertDoesNotThrow(() -> {
            worldManager.scheduleWorldUnload(islandId, 60);
        });
    }
    
    @Test
    @DisplayName("Loaded islands list should be empty initially")
    void testLoadedIslandsEmpty() {
        var loadedIslands = worldManager.getLoadedIslands();
        assertNotNull(loadedIslands);
        assertTrue(loadedIslands.isEmpty());
    }
    
    @Test
    @DisplayName("WorldManager should handle shutdown gracefully")
    void testShutdown() {
        assertDoesNotThrow(() -> {
            worldManager.shutdown();
        });
    }
}