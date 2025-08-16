package com.zientis.display.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 島嶼展示模型數據結構
 * 
 * 包含微縮島嶼的所有視覺元素和元數據
 */
public class DisplayModel {
    
    private final UUID islandId;
    private final UUID ownerId;
    private final Location centerLocation;
    private final IslandDisplayTier displayTier;
    
    // 微縮方塊數據 (相對位置 -> 方塊數據)
    private final Map<BlockPosition, BlockData> miniatureBlocks;
    
    // 全息圖數據
    private HologramData hologramData;
    
    // 粒子效果數據
    private ParticleEffectData particleEffect;
    
    // 展示狀態
    private DisplayStatus status;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    
    // 互動統計
    private int viewCount;
    private int clickCount;
    
    public DisplayModel(UUID islandId, UUID ownerId, Location centerLocation, IslandDisplayTier displayTier) {
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.centerLocation = centerLocation.clone();
        this.displayTier = displayTier;
        this.miniatureBlocks = new ConcurrentHashMap<>();
        this.status = DisplayStatus.CREATING;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.viewCount = 0;
        this.clickCount = 0;
    }

    // === 核心方法 ===
    
    /**
     * 添加微縮方塊到展示模型
     */
    public void addMiniatureBlock(BlockPosition position, BlockData blockData) {
        miniatureBlocks.put(position, blockData);
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 移除微縮方塊
     */
    public void removeMiniatureBlock(BlockPosition position) {
        miniatureBlocks.remove(position);
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 清空所有微縮方塊
     */
    public void clearMiniatureBlocks() {
        miniatureBlocks.clear();
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 獲取指定位置的方塊數據
     */
    public BlockData getBlockAt(BlockPosition position) {
        return miniatureBlocks.get(position);
    }
    
    /**
     * 檢查是否包含指定位置的方塊
     */
    public boolean hasBlockAt(BlockPosition position) {
        return miniatureBlocks.containsKey(position);
    }
    
    /**
     * 獲取所有方塊位置
     */
    public java.util.Set<BlockPosition> getAllBlockPositions() {
        return miniatureBlocks.keySet();
    }
    
    /**
     * 獲取方塊總數
     */
    public int getBlockCount() {
        return miniatureBlocks.size();
    }
    
    /**
     * 更新全息圖數據
     */
    public void updateHologram(HologramData newHologramData) {
        this.hologramData = newHologramData;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新粒子效果
     */
    public void updateParticleEffect(ParticleEffectData newEffect) {
        this.particleEffect = newEffect;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 增加觀看次數
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    /**
     * 增加點擊次數
     */
    public void incrementClickCount() {
        this.clickCount++;
    }
    
    /**
     * 標記為已更新
     */
    public void markAsUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    // === Getters ===
    
    public UUID getIslandId() { return islandId; }
    public UUID getOwnerId() { return ownerId; }
    public Location getCenterLocation() { return centerLocation.clone(); }
    public IslandDisplayTier getDisplayTier() { return displayTier; }
    public Map<BlockPosition, BlockData> getMiniatureBlocks() { return new ConcurrentHashMap<>(miniatureBlocks); }
    public HologramData getHologramData() { return hologramData; }
    public ParticleEffectData getParticleEffect() { return particleEffect; }
    public DisplayStatus getStatus() { return status; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getViewCount() { return viewCount; }
    public int getClickCount() { return clickCount; }
    
    // === Setters ===
    
    public void setStatus(DisplayStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void setHologramData(HologramData hologramData) {
        this.hologramData = hologramData;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void setParticleEffect(ParticleEffectData particleEffect) {
        this.particleEffect = particleEffect;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新微縮方塊數據
     */
    public void updateMiniatureBlocks(Map<BlockPosition, BlockData> blocks) {
        this.miniatureBlocks.clear();
        this.miniatureBlocks.putAll(blocks);
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新展示等級
     */
    public void updateDisplayTier(IslandDisplayTier newTier) {
        // 注意：這個方法可能需要重新分配展示位置
        // 實際使用時應該通過API來處理等級升級
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新中心位置
     */
    public void updateCenterLocation(Location newLocation) {
        if (newLocation != null && newLocation.getWorld() != null) {
            this.centerLocation.setWorld(newLocation.getWorld());
            this.centerLocation.setX(newLocation.getX());
            this.centerLocation.setY(newLocation.getY());
            this.centerLocation.setZ(newLocation.getZ());
            this.lastUpdated = LocalDateTime.now();
        }
    }

    // === 計算方法 ===
    
    /**
     * 計算展示模型的邊界框
     */
    public BoundingBox getBoundingBox() {
        if (miniatureBlocks.isEmpty()) {
            return new BoundingBox(centerLocation, centerLocation);
        }
        
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPosition pos : miniatureBlocks.keySet()) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        Location min = centerLocation.clone().add(minX, minY, minZ);
        Location max = centerLocation.clone().add(maxX, maxY, maxZ);
        
        return new BoundingBox(min, max);
    }
    
    /**
     * 檢查展示模型是否在指定位置範圍內
     */
    public boolean isWithinRange(Location location, double range) {
        return centerLocation.distance(location) <= range;
    }
    
    /**
     * 獲取展示模型中最常見的方塊類型
     */
    public Material getMostCommonBlock() {
        Map<Material, Integer> blockCounts = new ConcurrentHashMap<>();
        
        for (BlockData blockData : miniatureBlocks.values()) {
            Material material = blockData.getMaterial();
            blockCounts.put(material, blockCounts.getOrDefault(material, 0) + 1);
        }
        
        return blockCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Material.STONE);
    }
    
    @Override
    public String toString() {
        return String.format("DisplayModel{islandId=%s, owner=%s, center=%s, tier=%s, blocks=%d, status=%s}", 
            islandId, ownerId, centerLocation, displayTier, miniatureBlocks.size(), status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DisplayModel other = (DisplayModel) obj;
        return islandId.equals(other.islandId);
    }
    
    @Override
    public int hashCode() {
        return islandId.hashCode();
    }
}