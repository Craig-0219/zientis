package com.zientis.display.engine;

import com.zientis.display.data.BlockPosition;
import com.zientis.display.data.IslandDisplayTier;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 縮放引擎
 * 
 * 負責將原始島嶼按1:8比例縮放到微縮展示模型
 * 包含智能縮放算法和特徵保持技術
 */
public class ScalingEngine {
    
    private static final Logger logger = Logger.getLogger(ScalingEngine.class.getName());
    
    // 標準縮放比例
    public static final int SCALE_RATIO = 8;
    
    // 最小展示尺寸 (防止過小)
    private static final int MIN_DISPLAY_SIZE = 16;
    
    // 最大展示尺寸 (防止過大)
    private static final int MAX_DISPLAY_SIZE = 64;

    /**
     * 將原始島嶼方塊映射縮放為微縮展示
     * 
     * @param originalBlocks 原始方塊映射
     * @param targetCenter 目標展示中心位置
     * @param displayTier 展示等級
     * @return 異步返回縮放後的方塊映射和位置
     */
    public CompletableFuture<ScalingResult> scaleToMiniature(
            Map<BlockPosition, BlockData> originalBlocks, 
            Location targetCenter, 
            IslandDisplayTier displayTier) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("開始縮放處理: 原始方塊數量 " + originalBlocks.size());
            
            if (originalBlocks.isEmpty()) {
                return new ScalingResult(new HashMap<>(), targetCenter);
            }
            
            // 第一階段：分析原始結構邊界
            BoundingBox originalBounds = calculateBounds(originalBlocks.keySet());
            
            // 第二階段：計算縮放參數
            ScalingParameters params = calculateScalingParameters(originalBounds, displayTier);
            
            // 第三階段：執行縮放變換
            Map<BlockPosition, BlockData> scaledBlocks = performScaling(originalBlocks, params);
            
            // 第四階段：位置對齊和優化
            Map<BlockPosition, BlockData> finalBlocks = alignAndOptimize(scaledBlocks, targetCenter, params);
            
            logger.info("縮放完成: 原始 " + originalBlocks.size() + " -> 縮放後 " + finalBlocks.size() + " 個方塊");
            
            return new ScalingResult(finalBlocks, targetCenter);
        });
    }

    /**
     * 計算原始方塊的邊界框
     */
    private BoundingBox calculateBounds(Iterable<BlockPosition> positions) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPosition pos : positions) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * 計算縮放參數
     */
    private ScalingParameters calculateScalingParameters(BoundingBox bounds, IslandDisplayTier displayTier) {
        // 原始尺寸
        int originalWidth = bounds.width();
        int originalHeight = bounds.height();
        int originalDepth = bounds.depth();
        
        // 基礎縮放尺寸
        int baseScaledWidth = Math.max(1, originalWidth / SCALE_RATIO);
        int baseScaledHeight = Math.max(1, originalHeight / SCALE_RATIO);
        int baseScaledDepth = Math.max(1, originalDepth / SCALE_RATIO);
        
        // 根據展示等級調整
        double tierMultiplier = getTierSizeMultiplier(displayTier);
        
        int finalWidth = (int) Math.round(baseScaledWidth * tierMultiplier);
        int finalHeight = (int) Math.round(baseScaledHeight * tierMultiplier);
        int finalDepth = (int) Math.round(baseScaledDepth * tierMultiplier);
        
        // 應用尺寸限制
        finalWidth = Math.max(MIN_DISPLAY_SIZE, Math.min(MAX_DISPLAY_SIZE, finalWidth));
        finalHeight = Math.max(MIN_DISPLAY_SIZE / 2, Math.min(MAX_DISPLAY_SIZE, finalHeight));
        finalDepth = Math.max(MIN_DISPLAY_SIZE, Math.min(MAX_DISPLAY_SIZE, finalDepth));
        
        // 計算實際縮放比例
        double scaleX = (double) finalWidth / originalWidth;
        double scaleY = (double) finalHeight / originalHeight;
        double scaleZ = (double) finalDepth / originalDepth;
        
        return new ScalingParameters(scaleX, scaleY, scaleZ, bounds);
    }

    /**
     * 根據展示等級獲取尺寸倍數
     */
    private double getTierSizeMultiplier(IslandDisplayTier displayTier) {
        switch (displayTier) {
            case BASIC: return 0.8; // 稍微小一點
            case ENHANCED: return 1.0; // 標準大小
            case ADVANCED: return 1.2; // 稍微大一點
            case PREMIUM: return 1.5; // 明顯大一點
            default: return 1.0;
        }
    }

    /**
     * 執行縮放變換
     */
    private Map<BlockPosition, BlockData> performScaling(
            Map<BlockPosition, BlockData> originalBlocks, 
            ScalingParameters params) {
        
        Map<BlockPosition, BlockData> scaledBlocks = new HashMap<>();
        
        for (Map.Entry<BlockPosition, BlockData> entry : originalBlocks.entrySet()) {
            BlockPosition originalPos = entry.getKey();
            BlockData blockData = entry.getValue();
            
            // 計算相對於原始邊界的位置
            int relativeX = originalPos.getX() - params.originalBounds.minX;
            int relativeY = originalPos.getY() - params.originalBounds.minY;
            int relativeZ = originalPos.getZ() - params.originalBounds.minZ;
            
            // 應用縮放
            int scaledX = (int) Math.round(relativeX * params.scaleX);
            int scaledY = (int) Math.round(relativeY * params.scaleY);
            int scaledZ = (int) Math.round(relativeZ * params.scaleZ);
            
            BlockPosition scaledPos = new BlockPosition(scaledX, scaledY, scaledZ);
            
            // 處理縮放衝突 (多個原始方塊映射到同一個縮放位置)
            if (scaledBlocks.containsKey(scaledPos)) {
                // 選擇更重要的方塊 (可以基於方塊類型的重要性)
                BlockData existingBlock = scaledBlocks.get(scaledPos);
                BlockData betterBlock = chooseBetterBlock(existingBlock, blockData);
                scaledBlocks.put(scaledPos, betterBlock);
            } else {
                scaledBlocks.put(scaledPos, blockData);
            }
        }
        
        return scaledBlocks;
    }

    /**
     * 選擇更好的方塊 (當多個方塊映射到同一位置時)
     */
    private BlockData chooseBetterBlock(BlockData block1, BlockData block2) {
        // 簡化實現：選擇非空氣且更"實體"的方塊
        if (block1.getMaterial().isSolid() && !block2.getMaterial().isSolid()) {
            return block1;
        } else if (!block1.getMaterial().isSolid() && block2.getMaterial().isSolid()) {
            return block2;
        }
        
        // 如果都是實體或都不是實體，選擇第一個
        return block1;
    }

    /**
     * 對齊和優化最終結果
     */
    private Map<BlockPosition, BlockData> alignAndOptimize(
            Map<BlockPosition, BlockData> scaledBlocks, 
            Location targetCenter, 
            ScalingParameters params) {
        
        Map<BlockPosition, BlockData> finalBlocks = new HashMap<>();
        
        // 計算縮放後的中心偏移
        BoundingBox scaledBounds = calculateBounds(scaledBlocks.keySet());
        int centerOffsetX = scaledBounds.centerX();
        int centerOffsetY = scaledBounds.centerY();
        int centerOffsetZ = scaledBounds.centerZ();
        
        // 調整位置使展示居中
        for (Map.Entry<BlockPosition, BlockData> entry : scaledBlocks.entrySet()) {
            BlockPosition originalPos = entry.getKey();
            BlockData blockData = entry.getValue();
            
            // 居中對齊
            int alignedX = originalPos.getX() - centerOffsetX;
            int alignedY = originalPos.getY(); // Y軸不居中，保持底部對齊
            int alignedZ = originalPos.getZ() - centerOffsetZ;
            
            BlockPosition alignedPos = new BlockPosition(alignedX, alignedY, alignedZ);
            finalBlocks.put(alignedPos, blockData);
        }
        
        return finalBlocks;
    }

    /**
     * 重新縮放現有展示 (當島嶼等級改變時)
     */
    public CompletableFuture<ScalingResult> rescaleForTierUpgrade(
            Map<BlockPosition, BlockData> currentBlocks,
            IslandDisplayTier newTier,
            Location currentCenter) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("等級升級縮放: 新等級 " + newTier);
            
            // 計算新的縮放參數
            BoundingBox currentBounds = calculateBounds(currentBlocks.keySet());
            ScalingParameters newParams = calculateScalingParameters(currentBounds, newTier);
            
            // 應用新的縮放
            Map<BlockPosition, BlockData> rescaledBlocks = performScaling(currentBlocks, newParams);
            Map<BlockPosition, BlockData> finalBlocks = alignAndOptimize(rescaledBlocks, currentCenter, newParams);
            
            return new ScalingResult(finalBlocks, currentCenter);
        });
    }

    // === 輔助類 ===

    /**
     * 邊界框類 (內部使用)
     */
    private static class BoundingBox {
        final int minX, minY, minZ, maxX, maxY, maxZ;
        
        BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
        
        int width() { return maxX - minX + 1; }
        int height() { return maxY - minY + 1; }
        int depth() { return maxZ - minZ + 1; }
        int centerX() { return (minX + maxX) / 2; }
        int centerY() { return (minY + maxY) / 2; }
        int centerZ() { return (minZ + maxZ) / 2; }
    }

    /**
     * 縮放參數類
     */
    private static class ScalingParameters {
        final double scaleX, scaleY, scaleZ;
        final BoundingBox originalBounds;
        
        ScalingParameters(double scaleX, double scaleY, double scaleZ, BoundingBox originalBounds) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.originalBounds = originalBounds;
        }
    }

    /**
     * 縮放結果類
     */
    public static class ScalingResult {
        private final Map<BlockPosition, BlockData> scaledBlocks;
        private final Location centerLocation;
        
        public ScalingResult(Map<BlockPosition, BlockData> scaledBlocks, Location centerLocation) {
            this.scaledBlocks = scaledBlocks;
            this.centerLocation = centerLocation;
        }
        
        public Map<BlockPosition, BlockData> getScaledBlocks() { return scaledBlocks; }
        public Location getCenterLocation() { return centerLocation; }
    }
}