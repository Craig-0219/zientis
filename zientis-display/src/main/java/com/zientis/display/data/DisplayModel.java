package com.zientis.display.data;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class DisplayModel {

    private final UUID islandId;
    private final UUID displayId;
    private Location centerLocation;
    private IslandDisplayTier displayTier;
    private DisplayStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    private Map<BlockPosition, BlockData> miniatureBlocks;
    private HologramData hologramData;
    private ParticleEffectData particleEffect;

    public DisplayModel(UUID islandId, UUID displayId, Location centerLocation, IslandDisplayTier displayTier) {
        this.islandId = islandId;
        this.displayId = displayId;
        this.centerLocation = centerLocation;
        this.displayTier = displayTier;
        this.status = DisplayStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = this.createdAt;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public UUID getDisplayId() {
        return displayId;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public IslandDisplayTier getDisplayTier() {
        return displayTier;
    }

    public DisplayStatus getStatus() {
        return status;
    }

    public void setStatus(DisplayStatus status) {
        this.status = status;
    }

    public int getBlockCount() {
        return miniatureBlocks != null ? miniatureBlocks.size() : 0;
    }

    public Map<BlockPosition, BlockData> getMiniatureBlocks() {
        return miniatureBlocks;
    }

    public void updateMiniatureBlocks(Map<BlockPosition, BlockData> miniatureBlocks) {
        this.miniatureBlocks = miniatureBlocks;
    }

    public HologramData getHologramData() {
        return hologramData;
    }

    public void setHologramData(HologramData hologramData) {
        this.hologramData = hologramData;
    }

    public ParticleEffectData getParticleEffect() {
        return particleEffect;
    }

    public void setParticleEffect(ParticleEffectData particleEffect) {
        this.particleEffect = particleEffect;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void markAsUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateDisplayTier(IslandDisplayTier newTier) {
        this.displayTier = newTier;
    }

    public void updateCenterLocation(Location newLocation) {
        this.centerLocation = newLocation;
    }
}
