package com.zientis.display.api;

import com.zientis.core.api.ZientisAPI;
import com.zientis.display.data.*;
import com.zientis.display.engine.BlockMappingEngine;
import com.zientis.display.engine.ScalingEngine;
import com.zientis.display.manager.DisplayRegionManager;
import com.zientis.display.renderer.DisplayRenderer;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 賽恩堤斯島嶼展示系統API實現
 * 
 * 整合方塊映射、縮放引擎、區域管理和渲染器
 * 提供完整的島嶼展示功能
 */
public class ZientisDisplayAPIImpl implements ZientisDisplayAPI {
    
    private static final Logger logger = Logger.getLogger(ZientisDisplayAPIImpl.class.getName());
    
    // 核心組件
    private final BlockMappingEngine blockMappingEngine;
    private final ScalingEngine scalingEngine;
    private final DisplayRegionManager regionManager;
    private final DisplayRenderer displayRenderer;
    
    // 外部API依賴
    private ZientisAPI coreAPI;
    private ZientisMultiWorldAPI multiWorldAPI;
    
    // 展示模型存儲
    private final Map<UUID, DisplayModel> displayModels;
    
    // 性能統計
    private final Map<UUID, Long> updateTimes;
    private long totalMemoryUsage = 0;

    public ZientisDisplayAPIImpl() {
        this.blockMappingEngine = new BlockMappingEngine();
        this.scalingEngine = new ScalingEngine();
        this.regionManager = new DisplayRegionManager();
        this.displayRenderer = new DisplayRenderer();
        
        this.displayModels = new ConcurrentHashMap<>();
        this.updateTimes = new ConcurrentHashMap<>();
        
        logger.info("賽恩堤斯展示系統 API 已初始化");
    }

    /**
     * 設置外部API依賴
     */
    public void setCoreAPI(ZientisAPI coreAPI) {
        this.coreAPI = coreAPI;
    }

    public void setMultiWorldAPI(ZientisMultiWorldAPI multiWorldAPI) {
        this.multiWorldAPI = multiWorldAPI;
    }

    /**
     * 設置主世界
     */
    public void setMainWorld(World mainWorld) {
        regionManager.setMainWorld(mainWorld);
    }

    @Override
    public CompletableFuture<DisplayModel> createIslandDisplay(UUID islandId, Location center) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            logger.info("創建島嶼展示: " + islandId);
            
            try {
                // 檢查是否已存在
                if (displayModels.containsKey(islandId)) {
                    logger.warning("島嶼展示已存在: " + islandId);
                    return displayModels.get(islandId);
                }
                
                // 獲取島嶼世界
                World islandWorld = getIslandWorld(islandId);
                if (islandWorld == null) {
                    throw new IllegalArgumentException("找不到島嶼世界: " + islandId);
                }
                
                // 確定展示等級
                IslandDisplayTier displayTier = determineDisplayTier(islandId);
                
                // 分配展示位置
                Location displayLocation = center != null ? center : 
                    regionManager.allocateDisplayPosition(islandId, displayTier).join();
                
                // 創建展示模型
                DisplayModel displayModel = new DisplayModel(islandId, UUID.randomUUID(), displayLocation, displayTier);
                
                // 第一階段：掃描和映射島嶼方塊
                Map<BlockPosition, BlockData> mappedBlocks = blockMappingEngine
                    .scanAndMapIsland(islandWorld, 64, displayTier).join();
                
                if (mappedBlocks.isEmpty()) {
                    logger.warning("島嶼掃描結果為空: " + islandId);
                }
                
                // 第二階段：縮放處理
                ScalingEngine.ScalingResult scalingResult = scalingEngine
                    .scaleToMiniature(mappedBlocks, displayLocation, displayTier).join();
                
                // 設置展示模型數據
                displayModel.updateMiniatureBlocks(scalingResult.getScaledBlocks());
                // displayModel.setCenterLocation(scalingResult.getCenterLocation()); // 位置在構造函數中已設定
                
                // 第三階段：創建全息圖數據
                HologramData hologramData = createHologramData(islandId, displayTier, displayLocation);
                displayModel.setHologramData(hologramData);
                
                // 第四階段：設置粒子效果
                ParticleEffectData particleData = ParticleEffectData.createForTier(displayLocation, displayTier);
                displayModel.setParticleEffect(particleData);
                
                // 第五階段：渲染到世界
                boolean renderSuccess = displayRenderer.renderDisplay(displayModel).join();
                if (!renderSuccess) {
                    throw new RuntimeException("渲染展示失敗: " + islandId);
                }
                
                // 保存模型
                displayModels.put(islandId, displayModel);
                
                // 記錄性能統計
                long duration = System.currentTimeMillis() - startTime;
                updateTimes.put(islandId, duration);
                
                logger.info("島嶼展示創建完成: " + islandId + " (耗時: " + duration + "ms, 方塊數: " + 
                           displayModel.getBlockCount() + ")");
                
                return displayModel;
                
            } catch (Exception e) {
                logger.severe("創建島嶼展示失敗: " + islandId + " - " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("創建展示失敗", e);
            }
        });
    }

    @Override
    public CompletableFuture<DisplayModel> updateDisplayModel(UUID islandId, DisplayUpdateType updateType) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            logger.info("更新島嶼展示: " + islandId + ", 類型: " + updateType);
            
            DisplayModel existingModel = displayModels.get(islandId);
            if (existingModel == null) {
                logger.warning("找不到要更新的展示: " + islandId);
                return null;
            }
            
            try {
                DisplayModel updatedModel = null;
                
                switch (updateType) {
                    case FULL_REBUILD:
                        // 完全重建：重新掃描島嶼
                        updatedModel = performFullRebuild(islandId, existingModel);
                        break;
                        
                    case INCREMENTAL:
                        // 增量更新：只更新變化的部分
                        updatedModel = performIncrementalUpdate(islandId, existingModel);
                        break;
                        
                    case HOLOGRAM_ONLY:
                        // 僅更新全息圖
                        updatedModel = updateHologramOnly(islandId, existingModel);
                        break;
                        
                    case PARTICLE_ONLY:
                        // 僅更新粒子效果
                        updatedModel = updateParticlesOnly(islandId, existingModel);
                        break;
                        
                    case TIER_UPGRADE:
                        // 等級升級
                        updatedModel = performTierUpgrade(islandId, existingModel);
                        break;
                        
                    case POSITION_UPDATE:
                        // 位置更新
                        updatedModel = updatePosition(islandId, existingModel);
                        break;
                        
                    case FORCE_REFRESH:
                        // 強制刷新
                        updatedModel = performForceRefresh(islandId, existingModel);
                        break;
                        
                    default:
                        logger.warning("不支持的更新類型: " + updateType);
                        return existingModel;
                }
                
                if (updatedModel != null) {
                    // 更新渲染
                    displayRenderer.updateDisplay(updatedModel, updateType).join();
                    
                    // 保存更新後的模型
                    displayModels.put(islandId, updatedModel);
                    
                    // 記錄性能統計
                    long duration = System.currentTimeMillis() - startTime;
                    updateTimes.put(islandId, duration);
                    
                    logger.info("展示更新完成: " + islandId + " (耗時: " + duration + "ms)");
                }
                
                return updatedModel;
                
            } catch (Exception e) {
                logger.severe("更新島嶼展示失敗: " + islandId + " - " + e.getMessage());
                return existingModel;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeDisplay(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("移除島嶼展示: " + islandId);
            
            DisplayModel model = displayModels.remove(islandId);
            if (model == null) {
                return false;
            }
            
            try {
                // 移除渲染
                boolean renderRemoved = displayRenderer.removeDisplay(islandId).join();
                
                // 釋放展示位置
                boolean positionReleased = regionManager.deallocateDisplayPosition(islandId).join();
                
                // 清理統計數據
                updateTimes.remove(islandId);
                
                logger.info("展示移除完成: " + islandId + " (渲染移除: " + renderRemoved + 
                           ", 位置釋放: " + positionReleased + ")");
                
                return renderRemoved && positionReleased;
                
            } catch (Exception e) {
                logger.severe("移除島嶼展示失敗: " + islandId + " - " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public List<DisplayModel> getNearbyDisplays(Location center, int radius) {
        return displayModels.values().stream()
            .filter(model -> model.getCenterLocation().distance(center) <= radius)
            .collect(Collectors.toList());
    }

    @Override
    public DisplayModel getDisplayModel(UUID islandId) {
        return displayModels.get(islandId);
    }

    @Override
    public List<DisplayModel> getAllDisplays() {
        return new ArrayList<>(displayModels.values());
    }

    @Override
    public CompletableFuture<Boolean> reloadDisplay(UUID islandId) {
        return updateDisplayModel(islandId, DisplayUpdateType.FULL_REBUILD)
            .thenApply(model -> model != null);
    }

    @Override
    public CompletableFuture<Integer> batchUpdateDisplays(List<UUID> islandIds, DisplayUpdateType updateType) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("批量更新展示: " + islandIds.size() + " 個島嶼, 類型: " + updateType);
            
            List<CompletableFuture<DisplayModel>> updateFutures = islandIds.stream()
                .map(id -> updateDisplayModel(id, updateType))
                .collect(Collectors.toList());
            
            // 等待所有更新完成
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).join();
            
            // 統計成功更新的數量
            int successCount = (int) updateFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .count();
            
            logger.info("批量更新完成: " + successCount + "/" + islandIds.size() + " 成功");
            return successCount;
        });
    }

    @Override
    public void setAutoUpdateInterval(String region, int intervalSeconds) {
        // 實現自動更新邏輯
        logger.info("設置自動更新間隔: " + region + " - " + intervalSeconds + "秒");
        // TODO: 實現定時任務
    }

    @Override
    public DisplaySystemStats getSystemStats() {
        int totalDisplays = displayModels.size();
        int activeDisplays = (int) displayModels.values().stream()
            .filter(model -> model.getStatus() == DisplayStatus.ACTIVE)
            .count();
        
        double avgUpdateTime = updateTimes.isEmpty() ? 0.0 : 
            updateTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        return new DisplaySystemStats(totalDisplays, activeDisplays, totalMemoryUsage, avgUpdateTime);
    }

    // === 私有輔助方法 ===

    /**
     * 獲取島嶼世界
     */
    private World getIslandWorld(UUID islandId) {
        if (multiWorldAPI != null) {
            return multiWorldAPI.getOrLoadWorld(islandId);
        }
        logger.warning("多世界API未設置，無法獲取島嶼世界: " + islandId);
        return null;
    }

    /**
     * 確定展示等級
     */
    private IslandDisplayTier determineDisplayTier(UUID islandId) {
        // 簡化實現：基於島嶼等級或其他指標
        // TODO: 與核心系統整合獲取真實等級
        return IslandDisplayTier.BASIC;
    }

    /**
     * 創建全息圖數據
     */
    private HologramData createHologramData(UUID islandId, IslandDisplayTier tier, Location location) {
        List<String> lines = new ArrayList<>();
        
        // 基礎信息
        lines.add("§e島嶼 ID: " + islandId.toString().substring(0, 8));
        lines.add("§7等級: §f" + tier.name());
        lines.add("§a點擊訪問");
        
        return new HologramData(location.clone().add(0, 3, 0), lines);
    }

    /**
     * 執行完全重建
     */
    private DisplayModel performFullRebuild(UUID islandId, DisplayModel existingModel) {
        // 移除舊展示
        removeDisplay(islandId).join();
        
        // 重新創建
        return createIslandDisplay(islandId, existingModel.getCenterLocation()).join();
    }

    /**
     * 執行增量更新
     */
    private DisplayModel performIncrementalUpdate(UUID islandId, DisplayModel existingModel) {
        // 重新掃描島嶼
        World islandWorld = getIslandWorld(islandId);
        if (islandWorld == null) {
            return existingModel;
        }
        
        Map<BlockPosition, BlockData> newBlocks = blockMappingEngine
            .scanAndMapIsland(islandWorld, 64, existingModel.getDisplayTier()).join();
        
        // 縮放新數據
        ScalingEngine.ScalingResult scalingResult = scalingEngine
            .scaleToMiniature(newBlocks, existingModel.getCenterLocation(), existingModel.getDisplayTier()).join();
        
        // 更新模型
        existingModel.updateMiniatureBlocks(scalingResult.getScaledBlocks());
        existingModel.markAsUpdated();
        
        return existingModel;
    }

    /**
     * 僅更新全息圖
     */
    private DisplayModel updateHologramOnly(UUID islandId, DisplayModel existingModel) {
        HologramData newHologram = createHologramData(islandId, existingModel.getDisplayTier(), 
                                                     existingModel.getCenterLocation());
        existingModel.setHologramData(newHologram);
        existingModel.markAsUpdated();
        return existingModel;
    }

    /**
     * 僅更新粒子效果
     */
    private DisplayModel updateParticlesOnly(UUID islandId, DisplayModel existingModel) {
        ParticleEffectData newParticles = ParticleEffectData.createForTier(
            existingModel.getCenterLocation(), existingModel.getDisplayTier());
        existingModel.setParticleEffect(newParticles);
        existingModel.markAsUpdated();
        return existingModel;
    }

    /**
     * 執行等級升級
     */
    private DisplayModel performTierUpgrade(UUID islandId, DisplayModel existingModel) {
        // 重新確定等級
        IslandDisplayTier newTier = determineDisplayTier(islandId);
        
        if (newTier == existingModel.getDisplayTier()) {
            return existingModel; // 沒有變化
        }
        
        // 處理位置遷移
        Location newLocation = regionManager.handleTierUpgrade(
            islandId, existingModel.getDisplayTier(), newTier).join();
        
        // 更新模型等級和位置
        existingModel.updateDisplayTier(newTier);
        existingModel.updateCenterLocation(newLocation);
        
        // 重新執行完全重建
        return performFullRebuild(islandId, existingModel);
    }

    /**
     * 更新位置
     */
    private DisplayModel updatePosition(UUID islandId, DisplayModel existingModel) {
        // 簡化實現：保持當前位置
        existingModel.markAsUpdated();
        return existingModel;
    }

    /**
     * 執行強制刷新
     */
    private DisplayModel performForceRefresh(UUID islandId, DisplayModel existingModel) {
        return performFullRebuild(islandId, existingModel);
    }
}