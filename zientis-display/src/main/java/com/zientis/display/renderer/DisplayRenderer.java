package com.zientis.display.renderer;

import com.zientis.display.data.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 展示渲染器
 * 
 * 負責將展示模型渲染到主世界中
 * 包含LOD (Level of Detail) 渲染和性能優化
 */
public class DisplayRenderer {
    
    private static final Logger logger = Logger.getLogger(DisplayRenderer.class.getName());
    
    // 渲染距離配置
    private static final double HIGH_DETAIL_DISTANCE = 32.0;
    private static final double MEDIUM_DETAIL_DISTANCE = 64.0;
    private static final double LOW_DETAIL_DISTANCE = 128.0;
    
    // 渲染批次大小
    private static final int RENDER_BATCH_SIZE = 50;
    
    // 當前渲染的展示模型
    private final Map<UUID, RenderedDisplay> renderedDisplays;
    
    // 渲染任務佇列
    private final Queue<RenderTask> renderQueue;
    
    // 全息圖渲染器
    private final HologramRenderer hologramRenderer;
    
    // 粒子效果渲染器
    private final ParticleRenderer particleRenderer;

    public DisplayRenderer() {
        this.renderedDisplays = new ConcurrentHashMap<>();
        this.renderQueue = new LinkedList<>();
        this.hologramRenderer = new HologramRenderer();
        this.particleRenderer = new ParticleRenderer();
    }

    /**
     * 渲染展示模型到世界中
     * 
     * @param displayModel 展示模型
     * @return 異步返回渲染是否成功
     */
    public CompletableFuture<Boolean> renderDisplay(DisplayModel displayModel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("開始渲染展示: " + displayModel.getIslandId());
                
                // 檢查是否已經渲染
                if (renderedDisplays.containsKey(displayModel.getIslandId())) {
                    logger.warning("展示已經被渲染: " + displayModel.getIslandId());
                    return false;
                }
                
                displayModel.setStatus(DisplayStatus.UPDATING);
                
                // 創建渲染實例
                RenderedDisplay rendered = new RenderedDisplay(displayModel);
                
                // 第一階段：渲染方塊結構
                boolean blocksRendered = renderBlocks(rendered);
                if (!blocksRendered) {
                    logger.warning("方塊渲染失敗: " + displayModel.getIslandId());
                    return false;
                }
                
                // 第二階段：渲染全息圖
                boolean hologramRendered = renderHologram(rendered);
                if (!hologramRendered) {
                    logger.warning("全息圖渲染失敗: " + displayModel.getIslandId());
                }
                
                // 第三階段：渲染粒子效果
                boolean particlesRendered = renderParticles(rendered);
                if (!particlesRendered) {
                    logger.warning("粒子效果渲染失敗: " + displayModel.getIslandId());
                }
                
                // 保存渲染實例
                renderedDisplays.put(displayModel.getIslandId(), rendered);
                displayModel.setStatus(DisplayStatus.ACTIVE);
                
                logger.info("展示渲染完成: " + displayModel.getIslandId() + 
                           " (方塊: " + displayModel.getBlockCount() + ")");
                
                return true;
                
            } catch (Exception e) {
                logger.severe("渲染展示時發生錯誤: " + e.getMessage());
                displayModel.setStatus(DisplayStatus.ERROR);
                return false;
            }
        });
    }

    /**
     * 移除展示渲染
     * 
     * @param islandId 島嶼ID
     * @return 異步返回是否成功移除
     */
    public CompletableFuture<Boolean> removeDisplay(UUID islandId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("移除展示渲染: " + islandId);
            
            RenderedDisplay rendered = renderedDisplays.remove(islandId);
            if (rendered == null) {
                return false;
            }
            
            try {
                // 清理方塊
                clearBlocks(rendered);
                
                // 清理全息圖
                hologramRenderer.removeHologram(rendered.getHologramId());
                
                // 清理粒子效果
                particleRenderer.stopParticleEffect(rendered.getParticleEffectId());
                
                return true;
                
            } catch (Exception e) {
                logger.severe("移除展示渲染時發生錯誤: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * 更新展示渲染
     * 
     * @param displayModel 更新後的展示模型
     * @param updateType 更新類型
     * @return 異步返回是否成功更新
     */
    public CompletableFuture<Boolean> updateDisplay(DisplayModel displayModel, DisplayUpdateType updateType) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("更新展示渲染: " + displayModel.getIslandId() + ", 類型: " + updateType);
            
            RenderedDisplay rendered = renderedDisplays.get(displayModel.getIslandId());
            if (rendered == null) {
                // 如果未渲染，直接進行完整渲染
                return renderDisplay(displayModel).join();
            }
            
            try {
                displayModel.setStatus(DisplayStatus.UPDATING);
                
                boolean success = true;
                
                // 根據更新類型執行相應操作
                switch (updateType) {
                    case FULL_REBUILD:
                        success = performFullRebuild(rendered, displayModel);
                        break;
                        
                    case INCREMENTAL:
                        success = performIncrementalUpdate(rendered, displayModel);
                        break;
                        
                    case HOLOGRAM_ONLY:
                        success = updateHologramOnly(rendered, displayModel);
                        break;
                        
                    case PARTICLE_ONLY:
                        success = updateParticlesOnly(rendered, displayModel);
                        break;
                        
                    case POSITION_UPDATE:
                        success = updatePosition(rendered, displayModel);
                        break;
                        
                    case TIER_UPGRADE:
                        success = performTierUpgrade(rendered, displayModel);
                        break;
                        
                    case FORCE_REFRESH:
                        success = performForceRefresh(rendered, displayModel);
                        break;
                        
                    default:
                        logger.warning("未支援的更新類型: " + updateType);
                        success = false;
                }
                
                if (success) {
                    rendered.updateModel(displayModel);
                    displayModel.setStatus(DisplayStatus.ACTIVE);
                } else {
                    displayModel.setStatus(DisplayStatus.ERROR);
                }
                
                return success;
                
            } catch (Exception e) {
                logger.severe("更新展示渲染時發生錯誤: " + e.getMessage());
                displayModel.setStatus(DisplayStatus.ERROR);
                return false;
            }
        });
    }

    /**
     * 渲染方塊結構
     */
    private boolean renderBlocks(RenderedDisplay rendered) {
        DisplayModel model = rendered.getDisplayModel();
        Location center = model.getCenterLocation();
        
        List<Location> renderedBlockLocations = new ArrayList<>();
        
        try {
            // 批次渲染方塊
            Map<BlockPosition, BlockData> blocks = model.getMiniatureBlocks();
            int count = 0;
            
            for (Map.Entry<BlockPosition, BlockData> entry : blocks.entrySet()) {
                BlockPosition relativePos = entry.getKey();
                BlockData blockData = entry.getValue();
                
                // 計算世界位置
                Location worldPos = center.clone().add(relativePos.getX(), relativePos.getY(), relativePos.getZ());
                
                // 設置方塊
                Block block = worldPos.getBlock();
                block.setBlockData(blockData);
                
                renderedBlockLocations.add(worldPos);
                count++;
                
                // 批次處理以避免過度卡頓
                if (count % RENDER_BATCH_SIZE == 0) {
                    Thread.sleep(1); // 短暫暫停
                }
            }
            
            rendered.setRenderedBlocks(renderedBlockLocations);
            logger.info("成功渲染 " + count + " 個方塊");
            
            return true;
            
        } catch (Exception e) {
            logger.severe("渲染方塊時發生錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 清理方塊
     */
    private void clearBlocks(RenderedDisplay rendered) {
        List<Location> blockLocations = rendered.getRenderedBlocks();
        
        for (Location location : blockLocations) {
            Block block = location.getBlock();
            block.setType(Material.AIR);
        }
        
        logger.info("清理了 " + blockLocations.size() + " 個方塊");
    }

    /**
     * 渲染全息圖
     */
    private boolean renderHologram(RenderedDisplay rendered) {
        DisplayModel model = rendered.getDisplayModel();
        HologramData hologramData = model.getHologramData();
        
        if (hologramData == null || hologramData.isEmpty()) {
            return true; // 沒有全息圖不算錯誤
        }
        
        String hologramId = hologramRenderer.createHologram(hologramData);
        if (hologramId != null) {
            rendered.setHologramId(hologramId);
            return true;
        }
        
        return false;
    }

    /**
     * 渲染粒子效果
     */
    private boolean renderParticles(RenderedDisplay rendered) {
        DisplayModel model = rendered.getDisplayModel();
        ParticleEffectData particleData = model.getParticleEffect();
        
        if (particleData == null || !particleData.shouldDisplay()) {
            return true; // 沒有粒子效果不算錯誤
        }
        
        String effectId = particleRenderer.startParticleEffect(particleData);
        if (effectId != null) {
            rendered.setParticleEffectId(effectId);
            return true;
        }
        
        return false;
    }

    /**
     * 執行完全重建
     */
    private boolean performFullRebuild(RenderedDisplay rendered, DisplayModel newModel) {
        // 清理舊內容
        clearBlocks(rendered);
        hologramRenderer.removeHologram(rendered.getHologramId());
        particleRenderer.stopParticleEffect(rendered.getParticleEffectId());
        
        // 重新渲染
        return renderBlocks(rendered) && renderHologram(rendered) && renderParticles(rendered);
    }

    /**
     * 執行增量更新
     */
    private boolean performIncrementalUpdate(RenderedDisplay rendered, DisplayModel newModel) {
        // 比較新舊模型，只更新變化的部分
        DisplayModel oldModel = rendered.getDisplayModel();
        
        // 方塊差異更新
        Map<BlockPosition, BlockData> oldBlocks = oldModel.getMiniatureBlocks();
        Map<BlockPosition, BlockData> newBlocks = newModel.getMiniatureBlocks();
        
        // 找出需要更新的位置
        Set<BlockPosition> toUpdate = new HashSet<>();
        
        // 檢查新增和修改的方塊
        for (Map.Entry<BlockPosition, BlockData> entry : newBlocks.entrySet()) {
            BlockPosition pos = entry.getKey();
            BlockData newData = entry.getValue();
            BlockData oldData = oldBlocks.get(pos);
            
            if (oldData == null || !oldData.equals(newData)) {
                toUpdate.add(pos);
            }
        }
        
        // 檢查移除的方塊
        for (BlockPosition pos : oldBlocks.keySet()) {
            if (!newBlocks.containsKey(pos)) {
                toUpdate.add(pos);
            }
        }
        
        // 更新變化的方塊
        Location center = newModel.getCenterLocation();
        for (BlockPosition pos : toUpdate) {
            Location worldPos = center.clone().add(pos.getX(), pos.getY(), pos.getZ());
            Block block = worldPos.getBlock();
            
            BlockData newData = newBlocks.get(pos);
            if (newData != null) {
                block.setBlockData(newData);
            } else {
                block.setType(Material.AIR);
            }
        }
        
        logger.info("增量更新完成: 更新了 " + toUpdate.size() + " 個方塊");
        return true;
    }

    /**
     * 僅更新全息圖
     */
    private boolean updateHologramOnly(RenderedDisplay rendered, DisplayModel newModel) {
        // 移除舊全息圖
        hologramRenderer.removeHologram(rendered.getHologramId());
        
        // 創建新全息圖
        return renderHologram(rendered);
    }

    /**
     * 僅更新粒子效果
     */
    private boolean updateParticlesOnly(RenderedDisplay rendered, DisplayModel newModel) {
        // 停止舊粒子效果
        particleRenderer.stopParticleEffect(rendered.getParticleEffectId());
        
        // 開始新粒子效果
        return renderParticles(rendered);
    }

    /**
     * 更新位置
     */
    private boolean updatePosition(RenderedDisplay rendered, DisplayModel newModel) {
        // 重新渲染到新位置
        return performFullRebuild(rendered, newModel);
    }

    /**
     * 執行等級升級
     */
    private boolean performTierUpgrade(RenderedDisplay rendered, DisplayModel newModel) {
        // 等級升級通常需要完全重建
        return performFullRebuild(rendered, newModel);
    }

    /**
     * 執行強制刷新
     */
    private boolean performForceRefresh(RenderedDisplay rendered, DisplayModel newModel) {
        // 強制刷新等同於完全重建
        return performFullRebuild(rendered, newModel);
    }

    /**
     * 獲取渲染統計信息
     */
    public RenderStats getRenderStats() {
        int totalDisplays = renderedDisplays.size();
        int activeDisplays = (int) renderedDisplays.values().stream()
            .filter(r -> r.getDisplayModel().getStatus() == DisplayStatus.ACTIVE)
            .count();
        
        long totalBlocks = renderedDisplays.values().stream()
            .mapToLong(r -> r.getRenderedBlocks().size())
            .sum();
        
        return new RenderStats(totalDisplays, activeDisplays, totalBlocks);
    }

    /**
     * 根據玩家位置進行LOD渲染
     */
    public void updateLODForPlayer(Player player) {
        Location playerLocation = player.getLocation();
        
        for (RenderedDisplay rendered : renderedDisplays.values()) {
            DisplayModel model = rendered.getDisplayModel();
            double distance = playerLocation.distance(model.getCenterLocation());
            
            // 根據距離調整渲染細節
            RenderDetail detail = calculateLOD(distance);
            updateRenderDetail(rendered, detail);
        }
    }

    /**
     * 計算LOD等級
     */
    private RenderDetail calculateLOD(double distance) {
        if (distance <= HIGH_DETAIL_DISTANCE) {
            return RenderDetail.HIGH;
        } else if (distance <= MEDIUM_DETAIL_DISTANCE) {
            return RenderDetail.MEDIUM;
        } else if (distance <= LOW_DETAIL_DISTANCE) {
            return RenderDetail.LOW;
        } else {
            return RenderDetail.HIDDEN;
        }
    }

    /**
     * 更新渲染細節
     */
    private void updateRenderDetail(RenderedDisplay rendered, RenderDetail detail) {
        // 根據細節等級調整渲染
        switch (detail) {
            case HIGH:
                // 顯示所有細節
                break;
            case MEDIUM:
                // 減少粒子效果頻率
                break;
            case LOW:
                // 只顯示基本結構
                break;
            case HIDDEN:
                // 隱藏展示
                break;
        }
    }

    // === 枚舉類 ===

    /**
     * 渲染細節等級
     */
    private enum RenderDetail {
        HIGH, MEDIUM, LOW, HIDDEN
    }

    /**
     * 渲染任務
     */
    private static class RenderTask {
        final UUID islandId;
        final DisplayUpdateType updateType;
        final DisplayModel model;
        
        RenderTask(UUID islandId, DisplayUpdateType updateType, DisplayModel model) {
            this.islandId = islandId;
            this.updateType = updateType;
            this.model = model;
        }
    }

    /**
     * 渲染統計類
     */
    public static class RenderStats {
        private final int totalDisplays;
        private final int activeDisplays;
        private final long totalBlocks;
        
        public RenderStats(int totalDisplays, int activeDisplays, long totalBlocks) {
            this.totalDisplays = totalDisplays;
            this.activeDisplays = activeDisplays;
            this.totalBlocks = totalBlocks;
        }
        
        public int getTotalDisplays() { return totalDisplays; }
        public int getActiveDisplays() { return activeDisplays; }
        public long getTotalBlocks() { return totalBlocks; }
        
        @Override
        public String toString() {
            return String.format("RenderStats{displays=%d, active=%d, blocks=%d}", 
                totalDisplays, activeDisplays, totalBlocks);
        }
    }
}