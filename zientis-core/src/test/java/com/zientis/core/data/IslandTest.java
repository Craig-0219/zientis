package com.zientis.core.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Island class
 */
class IslandTest {
    
    private UUID islandId;
    private UUID ownerId;
    private Island island;
    
    @BeforeEach
    void setUp() {
        islandId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        island = new Island(islandId, ownerId);
    }
    
    @Test
    @DisplayName("Island creation should set basic properties correctly")
    void testIslandCreation() {
        assertEquals(islandId, island.getIslandId());
        assertEquals(ownerId, island.getOwnerId());
        assertEquals(1, island.getLevel());
        assertEquals("island_" + islandId.toString().replace("-", ""), island.getWorldName());
        assertFalse(island.isLoaded());
        assertNotNull(island.getCreatedAt());
        assertNotNull(island.getLastVisited());
    }
    
    @Test
    @DisplayName("Island ID and Owner ID should not be null")
    void testNullValidation() {
        assertThrows(NullPointerException.class, () -> new Island(null, ownerId));
        assertThrows(NullPointerException.class, () -> new Island(islandId, null));
    }
    
    @Test
    @DisplayName("Island level should be positive")
    void testLevelValidation() {
        // Valid levels
        island.setLevel(1);
        assertEquals(1, island.getLevel());
        
        island.setLevel(50);
        assertEquals(50, island.getLevel());
        
        // Invalid levels
        assertThrows(IllegalArgumentException.class, () -> island.setLevel(0));
        assertThrows(IllegalArgumentException.class, () -> island.setLevel(-1));
    }
    
    @Test
    @DisplayName("World name should be modifiable")
    void testWorldNameModification() {
        String newWorldName = "custom_world_name";
        island.setWorldName(newWorldName);
        assertEquals(newWorldName, island.getWorldName());
    }
    
    @Test
    @DisplayName("Last visited timestamp should be updatable")
    void testLastVisitedUpdate() {
        LocalDateTime beforeUpdate = island.getLastVisited();
        
        // Wait a small amount to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        island.updateLastVisited();
        LocalDateTime afterUpdate = island.getLastVisited();
        
        assertTrue(afterUpdate.isAfter(beforeUpdate));
    }
    
    @Test
    @DisplayName("Loaded state should be modifiable")
    void testLoadedState() {
        assertFalse(island.isLoaded());
        
        island.setLoaded(true);
        assertTrue(island.isLoaded());
        
        island.setLoaded(false);
        assertFalse(island.isLoaded());
    }
    
    @Test
    @DisplayName("Islands with same ID should be equal")
    void testEquality() {
        Island anotherIsland = new Island(islandId, UUID.randomUUID());
        assertEquals(island, anotherIsland);
        assertEquals(island.hashCode(), anotherIsland.hashCode());
    }
    
    @Test
    @DisplayName("Islands with different IDs should not be equal")
    void testInequality() {
        Island differentIsland = new Island(UUID.randomUUID(), ownerId);
        assertNotEquals(island, differentIsland);
    }
    
    @Test
    @DisplayName("toString should contain essential information")
    void testToString() {
        String toStringResult = island.toString();
        assertTrue(toStringResult.contains(islandId.toString()));
        assertTrue(toStringResult.contains(ownerId.toString()));
        assertTrue(toStringResult.contains("level=1"));
        assertTrue(toStringResult.contains("isLoaded=false"));
    }
}