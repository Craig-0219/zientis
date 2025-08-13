package com.zientis.core.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an island in the Zientis server
 * Each island has its own dedicated world
 */
public class Island {
    
    private final UUID islandId;
    private final UUID ownerId;
    private String worldName;
    private int level;
    private LocalDateTime createdAt;
    private LocalDateTime lastVisited;
    private boolean isLoaded;
    private Location spawnLocation;
    
    /**
     * Create a new Island
     * @param islandId Unique identifier for the island
     * @param ownerId UUID of the island owner
     */
    public Island(UUID islandId, UUID ownerId) {
        this.islandId = Objects.requireNonNull(islandId, "Island ID cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null");
        this.worldName = "island_" + islandId.toString().replace("-", "");
        this.level = 1;
        this.createdAt = LocalDateTime.now();
        this.lastVisited = LocalDateTime.now();
        this.isLoaded = false;
    }
    
    /**
     * Get the unique island ID
     * @return Island UUID
     */
    public UUID getIslandId() {
        return islandId;
    }
    
    /**
     * Get the owner's UUID
     * @return Owner UUID
     */
    public UUID getOwnerId() {
        return ownerId;
    }
    
    /**
     * Get the world name for this island
     * @return World name
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Set the world name for this island
     * @param worldName The world name
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    
    /**
     * Get the island level
     * @return Current level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Set the island level
     * @param level New level (must be positive)
     */
    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be positive");
        }
        this.level = level;
    }
    
    /**
     * Get when the island was created
     * @return Creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set when the island was created
     * @param createdAt Creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get when the island was last visited
     * @return Last visit timestamp
     */
    public LocalDateTime getLastVisited() {
        return lastVisited;
    }
    
    /**
     * Update the last visited timestamp to now
     */
    public void updateLastVisited() {
        this.lastVisited = LocalDateTime.now();
    }
    
    /**
     * Check if the island's world is currently loaded
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded() {
        return isLoaded;
    }
    
    /**
     * Set the loaded state of the island
     * @param loaded true if loaded, false otherwise
     */
    public void setLoaded(boolean loaded) {
        this.isLoaded = loaded;
    }
    
    /**
     * Get the spawn location for this island
     * @return Spawn location, or null if not set
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    /**
     * Set the spawn location for this island
     * @param spawnLocation The spawn location
     */
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Island island = (Island) obj;
        return Objects.equals(islandId, island.islandId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(islandId);
    }
    
    @Override
    public String toString() {
        return "Island{" +
                "islandId=" + islandId +
                ", ownerId=" + ownerId +
                ", worldName='" + worldName + '\'' +
                ", level=" + level +
                ", isLoaded=" + isLoaded +
                '}';
    }
}