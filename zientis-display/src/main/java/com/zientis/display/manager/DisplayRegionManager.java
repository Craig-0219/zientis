package com.zientis.display.manager;

import com.zientis.display.data.BoundingBox;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.IslandDisplayTier;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 展示區域管理器
 * 
 * 負責管理主世界中的島嶼展示區域分配和佈局
 * 根據島嶼等級動態分配展示位置
 */
public class DisplayRegionManager {
    
    private static final Logger logger = Logger.getLogger(DisplayRegionManager.class.getName());
    
    // 區域配置
    private final Map<IslandDisplayTier, RegionConfig> regionConfigs;
    
    // 已分配的展示位置
    private final Map<UUID, Location> allocatedPositions;
    
    // 區域佔用狀態
    private final Map<String, Set<GridPosition>> occupiedPositions;
    
    // 主世界引用
    private World mainWorld;

    public DisplayRegionManager() {
        this.regionConfigs = new ConcurrentHashMap<>();
        this.allocatedPositions = new ConcurrentHashMap<>();
        this.occupiedPositions = new ConcurrentHashMap<>();
        
        initializeRegionConfigs();
    }

    /**
     * 初始化區域配置
     */
    private void initializeRegionConfigs() {
        // 新手區域 (等級 1-10)
        regionConfigs.put(IslandDisplayTier.BASIC, new RegionConfig(
            "新手區域",
            new Location(null, 0, 64, 200),  // 中心位置
            100,  // 區域半徑
            12,   // 格子間距
            64    // Y軸高度
        ));
        
        // 進階區域 (等級 11-30)
        regionConfigs.put(IslandDisplayTier.ENHANCED, new RegionConfig(
            "進階區域",
            new Location(null, 200, 64, 0),
            120,
            15,
            64
        ));
        
        // 高級區域 (等級 31-50)
        regionConfigs.put(IslandDisplayTier.ADVANCED, new RegionConfig(
            "高級區域",
            new Location(null, -200, 64, 0),
            140,
            18,
            64
        ));
        
        // 頂級區域 (等級 51+)
        regionConfigs.put(IslandDisplayTier.PREMIUM, new RegionConfig(
            "頂級區域",
            new Location(null, 0, 64, -200),
            160,
            25,
            64
        ));
    }

    /**
     * 設置主世界
     */
    public void setMainWorld(World mainWorld) {
        this.mainWorld = mainWorld;
        
        // 更新所有區域配置的世界引用
        for (RegionConfig config : regionConfigs.values()) {
            config.centerLocation.setWorld(mainWorld);
        }
    }

    /**
     * 為島嶼分配展示位置
     * 
     * @param islandId 島嶼ID
     * @param displayTier 展示等級
     * @return 異步返回分配的位置
     */
    public CompletableFuture<Location> allocateDisplayPosition(UUID islandId, IslandDisplayTier displayTier) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("為島嶼分配展示位置: " + islandId + ", 等級: " + displayTier);
            
            // 檢查是否已經分配過位置
            Location existingPosition = allocatedPositions.get(islandId);
            if (existingPosition != null) {
                logger.info("島嶼已有分配位置: " + existingPosition);
                return existingPosition;
            }
            
            RegionConfig config = regionConfigs.get(displayTier);
            if (config == null) {
                throw new IllegalArgumentException("未找到展示等級配置: " + displayTier);
            }
            
            // 查找可用位置
            Optional<GridPosition> availableGrid = findAvailablePosition(config);
            if (!availableGrid.isPresent()) {
                throw new RuntimeException("區域 " + config.name + " 已滿，無法分配新位置");
            }
            
            GridPosition gridPos = availableGrid.get();
            Location worldPosition = gridToWorldPosition(gridPos, config);
            
            // 標記位置為已佔用
            occupiedPositions.computeIfAbsent(config.name, k -> ConcurrentHashMap.newKeySet()).add(gridPos);
            allocatedPositions.put(islandId, worldPosition);
            
            logger.info("成功分配位置: " + worldPosition + " (格子: " + gridPos + ")");
            return worldPosition;
        });
    }

    /**
     * 釋放島嶼的展示位置
     * 
     * @param islandId 島嶼ID
     * @return 異步返回是否成功釋放
     */
    public CompletableFuture<Boolean> deallocateDisplayPosition(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            Location position = allocatedPositions.remove(islandId);
            if (position == null) {
                return false;
            }
            
            // 找到對應的區域和格子位置
            for (Map.Entry<IslandDisplayTier, RegionConfig> entry : regionConfigs.entrySet()) {
                RegionConfig config = entry.getValue();
                GridPosition gridPos = worldToGridPosition(position, config);
                
                if (isPositionInRegion(gridPos, config)) {
                    Set<GridPosition> occupied = occupiedPositions.get(config.name);
                    if (occupied != null) {
                        occupied.remove(gridPos);
                    }
                    
                    logger.info("釋放展示位置: " + position + " (島嶼: " + islandId + ")");
                    return true;
                }
            }
            
            return false;
        });
    }

    /**
     * 處理島嶼等級升級 (可能需要遷移到新區域)
     * 
     * @param islandId 島嶼ID
     * @param oldTier 舊等級
     * @param newTier 新等級
     * @return 異步返回新的展示位置
     */
    public CompletableFuture<Location> handleTierUpgrade(UUID islandId, IslandDisplayTier oldTier, IslandDisplayTier newTier) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("處理等級升級: " + islandId + " (" + oldTier + " -> " + newTier + ")");
            
            // 如果等級沒有實際改變，保持原位置
            if (oldTier == newTier) {
                return allocatedPositions.get(islandId);
            }
            
            // 釋放舊位置
            deallocateDisplayPosition(islandId).join();
            
            // 分配新位置
            return allocateDisplayPosition(islandId, newTier).join();
        });
    }

    /**
     * 查找可用的格子位置
     */
    private Optional<GridPosition> findAvailablePosition(RegionConfig config) {
        Set<GridPosition> occupied = occupiedPositions.getOrDefault(config.name, Collections.emptySet());
        
        int maxGridRadius = config.radius / config.gridSpacing;
        
        // 螺旋搜索算法，從中心向外擴散
        for (int radius = 0; radius <= maxGridRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // 只檢查邊界上的點 (螺旋搜索)
                    if (Math.abs(x) != radius && Math.abs(z) != radius && radius > 0) {
                        continue;
                    }
                    
                    GridPosition candidate = new GridPosition(x, z);
                    
                    if (!occupied.contains(candidate) && isPositionInRegion(candidate, config)) {
                        return Optional.of(candidate);
                    }
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * 檢查格子位置是否在區域範圍內
     */
    private boolean isPositionInRegion(GridPosition gridPos, RegionConfig config) {
        double distance = Math.sqrt(gridPos.x * gridPos.x + gridPos.z * gridPos.z) * config.gridSpacing;
        return distance <= config.radius;
    }

    /**
     * 將格子位置轉換為世界坐標
     */
    private Location gridToWorldPosition(GridPosition gridPos, RegionConfig config) {
        double worldX = config.centerLocation.getX() + (gridPos.x * config.gridSpacing);
        double worldY = config.yLevel;
        double worldZ = config.centerLocation.getZ() + (gridPos.z * config.gridSpacing);
        
        return new Location(mainWorld, worldX, worldY, worldZ);
    }

    /**
     * 將世界坐標轉換為格子位置
     */
    private GridPosition worldToGridPosition(Location worldPos, RegionConfig config) {
        int gridX = (int) Math.round((worldPos.getX() - config.centerLocation.getX()) / config.gridSpacing);
        int gridZ = (int) Math.round((worldPos.getZ() - config.centerLocation.getZ()) / config.gridSpacing);
        
        return new GridPosition(gridX, gridZ);
    }

    /**
     * 獲取島嶼當前的展示位置
     */
    public Location getDisplayPosition(UUID islandId) {
        return allocatedPositions.get(islandId);
    }

    /**
     * 獲取指定區域內的所有展示
     */
    public List<UUID> getDisplaysInRegion(IslandDisplayTier tier) {
        RegionConfig config = regionConfigs.get(tier);
        if (config == null) {
            return Collections.emptyList();
        }
        
        List<UUID> displays = new ArrayList<>();
        for (Map.Entry<UUID, Location> entry : allocatedPositions.entrySet()) {
            Location pos = entry.getValue();
            double distance = pos.distance(config.centerLocation);
            
            if (distance <= config.radius) {
                displays.add(entry.getKey());
            }
        }
        
        return displays;
    }

    /**
     * 獲取區域統計信息
     */
    public RegionStats getRegionStats(IslandDisplayTier tier) {
        RegionConfig config = regionConfigs.get(tier);
        if (config == null) {
            return new RegionStats(0, 0, 0);
        }
        
        Set<GridPosition> occupied = occupiedPositions.getOrDefault(config.name, Collections.emptySet());
        int maxCapacity = calculateMaxCapacity(config);
        int usedSlots = occupied.size();
        int availableSlots = maxCapacity - usedSlots;
        
        return new RegionStats(maxCapacity, usedSlots, availableSlots);
    }

    /**
     * 計算區域最大容量
     */
    private int calculateMaxCapacity(RegionConfig config) {
        int gridRadius = config.radius / config.gridSpacing;
        
        // 圓形區域內的格子數量估算
        int capacity = 0;
        for (int x = -gridRadius; x <= gridRadius; x++) {
            for (int z = -gridRadius; z <= gridRadius; z++) {
                double distance = Math.sqrt(x * x + z * z) * config.gridSpacing;
                if (distance <= config.radius) {
                    capacity++;
                }
            }
        }
        
        return capacity;
    }

    // === 數據類 ===

    /**
     * 區域配置類
     */
    private static class RegionConfig {
        final String name;
        final Location centerLocation;
        final int radius;
        final int gridSpacing;
        final int yLevel;
        
        RegionConfig(String name, Location centerLocation, int radius, int gridSpacing, int yLevel) {
            this.name = name;
            this.centerLocation = centerLocation;
            this.radius = radius;
            this.gridSpacing = gridSpacing;
            this.yLevel = yLevel;
        }
    }

    /**
     * 格子位置類
     */
    private static class GridPosition {
        final int x, z;
        
        GridPosition(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GridPosition other = (GridPosition) obj;
            return x == other.x && z == other.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + z + ")";
        }
    }

    /**
     * 區域統計類
     */
    public static class RegionStats {
        private final int maxCapacity;
        private final int usedSlots;
        private final int availableSlots;
        
        public RegionStats(int maxCapacity, int usedSlots, int availableSlots) {
            this.maxCapacity = maxCapacity;
            this.usedSlots = usedSlots;
            this.availableSlots = availableSlots;
        }
        
        public int getMaxCapacity() { return maxCapacity; }
        public int getUsedSlots() { return usedSlots; }
        public int getAvailableSlots() { return availableSlots; }
        public double getUsagePercentage() { return maxCapacity > 0 ? (double) usedSlots / maxCapacity * 100 : 0; }
        
        @Override
        public String toString() {
            return String.format("RegionStats{used=%d, available=%d, capacity=%d, usage=%.1f%%}", 
                usedSlots, availableSlots, maxCapacity, getUsagePercentage());
        }
    }
}