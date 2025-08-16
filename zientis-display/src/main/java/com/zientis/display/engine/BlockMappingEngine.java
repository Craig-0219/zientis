package com.zientis.display.engine;

import com.zientis.display.data.BlockPosition;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.IslandDisplayTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 方塊映射引擎
 * 
 * 負責將原始島嶼的方塊結構轉換為微縮展示模型
 * 包含智能方塊識別、重要性評估和縮放處理
 */
public class BlockMappingEngine {
    
    private static final Logger logger = Logger.getLogger(BlockMappingEngine.class.getName());
    
    // 方塊重要性權重映射
    private static final Map<Material, Integer> BLOCK_IMPORTANCE = new ConcurrentHashMap<>();
    
    // 方塊替換映射 (某些方塊在微縮模型中用其他方塊替代)
    private static final Map<Material, Material> BLOCK_SUBSTITUTION = new ConcurrentHashMap<>();
    
    static {
        initializeBlockImportance();
        initializeBlockSubstitution();
    }

    /**
     * 初始化方塊重要性權重
     */
    private static void initializeBlockImportance() {
        // 結構性方塊 (最高重要性)
        BLOCK_IMPORTANCE.put(Material.BEDROCK, 100);
        BLOCK_IMPORTANCE.put(Material.OBSIDIAN, 95);
        
        // 建築方塊 (高重要性)
        BLOCK_IMPORTANCE.put(Material.STONE_BRICKS, 90);
        BLOCK_IMPORTANCE.put(Material.BRICKS, 90);
        BLOCK_IMPORTANCE.put(Material.QUARTZ_BLOCK, 85);
        BLOCK_IMPORTANCE.put(Material.PRISMARINE, 85);
        
        // 木製建築 (中高重要性)
        BLOCK_IMPORTANCE.put(Material.OAK_PLANKS, 80);
        BLOCK_IMPORTANCE.put(Material.BIRCH_PLANKS, 80);
        BLOCK_IMPORTANCE.put(Material.SPRUCE_PLANKS, 80);
        BLOCK_IMPORTANCE.put(Material.OAK_LOG, 85);
        
        // 功能性方塊 (中等重要性)
        BLOCK_IMPORTANCE.put(Material.CHEST, 75);
        BLOCK_IMPORTANCE.put(Material.FURNACE, 75);
        BLOCK_IMPORTANCE.put(Material.CRAFTING_TABLE, 70);
        BLOCK_IMPORTANCE.put(Material.ENCHANTING_TABLE, 75);
        
        // 裝飾方塊 (中低重要性)
        BLOCK_IMPORTANCE.put(Material.GLASS, 60);
        BLOCK_IMPORTANCE.put(Material.WHITE_WOOL, 55);
        BLOCK_IMPORTANCE.put(Material.TERRACOTTA, 55);
        
        // 自然方塊 (低重要性)
        BLOCK_IMPORTANCE.put(Material.GRASS_BLOCK, 40);
        BLOCK_IMPORTANCE.put(Material.DIRT, 30);
        BLOCK_IMPORTANCE.put(Material.STONE, 35);
        BLOCK_IMPORTANCE.put(Material.SAND, 25);
        
        // 液體和空氣 (最低重要性)
        BLOCK_IMPORTANCE.put(Material.WATER, 10);
        BLOCK_IMPORTANCE.put(Material.LAVA, 15);
        BLOCK_IMPORTANCE.put(Material.AIR, 0);
    }

    /**
     * 初始化方塊替換映射
     */
    private static void initializeBlockSubstitution() {
        // 複雜方塊簡化
        BLOCK_SUBSTITUTION.put(Material.CHEST, Material.BROWN_WOOL);
        BLOCK_SUBSTITUTION.put(Material.FURNACE, Material.GRAY_CONCRETE);
        BLOCK_SUBSTITUTION.put(Material.CRAFTING_TABLE, Material.OAK_PLANKS);
        
        // 透明方塊處理
        BLOCK_SUBSTITUTION.put(Material.GLASS_PANE, Material.GLASS);
        BLOCK_SUBSTITUTION.put(Material.IRON_BARS, Material.IRON_BLOCK);
        
        // 植物簡化
        BLOCK_SUBSTITUTION.put(Material.OAK_LEAVES, Material.GREEN_WOOL);
        BLOCK_SUBSTITUTION.put(Material.BIRCH_LEAVES, Material.LIME_WOOL);
        BLOCK_SUBSTITUTION.put(Material.SPRUCE_LEAVES, Material.GREEN_WOOL);
        
        // 作物簡化
        BLOCK_SUBSTITUTION.put(Material.WHEAT, Material.YELLOW_WOOL);
        BLOCK_SUBSTITUTION.put(Material.CARROTS, Material.ORANGE_WOOL);
        BLOCK_SUBSTITUTION.put(Material.POTATOES, Material.BROWN_WOOL);
    }

    /**
     * 掃描島嶼並創建映射數據
     * 
     * @param islandWorld 島嶼世界
     * @param scanArea 掃描區域 (以島嶼中心為基準的半徑)
     * @param displayTier 展示等級
     * @return 異步返回映射後的方塊數據
     */
    public CompletableFuture<Map<BlockPosition, BlockData>> scanAndMapIsland(
            World islandWorld, int scanArea, IslandDisplayTier displayTier) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("開始掃描島嶼: " + islandWorld.getName() + ", 範圍: " + scanArea);
            
            Map<BlockPosition, BlockData> mappedBlocks = new ConcurrentHashMap<>();
            
            // 獲取島嶼中心位置
            Location islandCenter = getIslandCenter(islandWorld);
            
            // 根據展示等級決定掃描密度
            int scanStep = getScanStep(displayTier);
            int maxBlocks = displayTier.getMaxRenderBlocks();
            
            Map<BlockPosition, ScanResult> scanResults = new HashMap<>();
            
            // 第一階段：掃描所有方塊
            for (int x = -scanArea; x <= scanArea; x += scanStep) {
                for (int y = 0; y <= 256; y += scanStep) {
                    for (int z = -scanArea; z <= scanArea; z += scanStep) {
                        Location blockLocation = islandCenter.clone().add(x, y, z);
                        Block block = blockLocation.getBlock();
                        
                        if (shouldIncludeBlock(block, displayTier)) {
                            BlockPosition position = new BlockPosition(x, y, z);
                            int importance = getBlockImportance(block.getType());
                            
                            scanResults.put(position, new ScanResult(block.getBlockData(), importance));
                        }
                    }
                }
            }
            
            // 第二階段：根據重要性篩選方塊
            Map<BlockPosition, ScanResult> filteredResults = filterByImportance(scanResults, maxBlocks);
            
            // 第三階段：應用方塊替換和優化
            for (Map.Entry<BlockPosition, ScanResult> entry : filteredResults.entrySet()) {
                BlockData originalData = entry.getValue().blockData;
                BlockData optimizedData = optimizeBlockData(originalData, displayTier);
                mappedBlocks.put(entry.getKey(), optimizedData);
            }
            
            logger.info("島嶼掃描完成: 掃描到 " + scanResults.size() + " 個方塊，篩選後保留 " + mappedBlocks.size() + " 個");
            
            return mappedBlocks;
        });
    }

    /**
     * 獲取島嶼中心位置
     */
    private Location getIslandCenter(World islandWorld) {
        // 簡化實現：假設島嶼中心在 (0, 100, 0)
        // 實際實現中應該從多世界管理器獲取
        return new Location(islandWorld, 0, 100, 0);
    }

    /**
     * 根據展示等級獲取掃描步長
     */
    private int getScanStep(IslandDisplayTier displayTier) {
        switch (displayTier) {
            case BASIC: return 3; // 每3格掃描一次
            case ENHANCED: return 2; // 每2格掃描一次
            case ADVANCED: return 1; // 每格都掃描
            case PREMIUM: return 1; // 每格都掃描
            default: return 3;
        }
    }

    /**
     * 檢查方塊是否應該包含在展示中
     */
    private boolean shouldIncludeBlock(Block block, IslandDisplayTier displayTier) {
        Material material = block.getType();
        
        // 空氣方塊總是排除
        if (material == Material.AIR) {
            return false;
        }
        
        // 根據展示等級過濾
        int importance = getBlockImportance(material);
        int minimumImportance = getMinimumImportance(displayTier);
        
        return importance >= minimumImportance;
    }

    /**
     * 獲取方塊重要性
     */
    private int getBlockImportance(Material material) {
        return BLOCK_IMPORTANCE.getOrDefault(material, 20); // 默認重要性
    }

    /**
     * 根據展示等級獲取最低重要性閾值
     */
    private int getMinimumImportance(IslandDisplayTier displayTier) {
        switch (displayTier) {
            case BASIC: return 50; // 只顯示重要方塊
            case ENHANCED: return 30; // 顯示大部分方塊
            case ADVANCED: return 15; // 顯示幾乎所有方塊
            case PREMIUM: return 10; // 顯示所有可見方塊
            default: return 50;
        }
    }

    /**
     * 根據重要性篩選方塊
     */
    private Map<BlockPosition, ScanResult> filterByImportance(
            Map<BlockPosition, ScanResult> scanResults, int maxBlocks) {
        
        if (scanResults.size() <= maxBlocks) {
            return scanResults;
        }
        
        // 按重要性排序並選取前N個
        return scanResults.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().importance, a.getValue().importance))
            .limit(maxBlocks)
            .collect(HashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                    HashMap::putAll);
    }

    /**
     * 優化方塊數據
     */
    private BlockData optimizeBlockData(BlockData originalData, IslandDisplayTier displayTier) {
        Material originalMaterial = originalData.getMaterial();
        
        // 檢查是否需要替換
        Material substituteMaterial = BLOCK_SUBSTITUTION.get(originalMaterial);
        if (substituteMaterial != null) {
            return substituteMaterial.createBlockData();
        }
        
        // 根據展示等級進行優化
        if (displayTier == IslandDisplayTier.BASIC) {
            // 基礎等級：簡化複雜方塊狀態
            return originalMaterial.createBlockData();
        }
        
        // 其他等級：保持原始數據
        return originalData;
    }

    /**
     * 更新現有展示模型的方塊映射
     */
    public CompletableFuture<Void> updateMappingForModel(DisplayModel model, Set<BlockPosition> changedPositions) {
        return CompletableFuture.runAsync(() -> {
            logger.info("更新展示模型映射: " + model.getIslandId() + ", 變更位置: " + changedPositions.size());
            
            // 實現增量更新邏輯
            // 這裡需要與多世界管理器協作獲取實際的島嶼世界
            
            // 暫時的實現框架
            for (BlockPosition position : changedPositions) {
                // 重新掃描指定位置的方塊
                // 更新模型中的方塊數據
                model.markAsUpdated();
            }
        });
    }

    /**
     * 獲取方塊變更檢測結果
     */
    public CompletableFuture<Set<BlockPosition>> detectChanges(DisplayModel model, World islandWorld) {
        return CompletableFuture.supplyAsync(() -> {
            // 實現方塊變更檢測邏輯
            // 比較當前島嶼狀態與模型中的數據
            
            Set<BlockPosition> changes = ConcurrentHashMap.newKeySet();
            
            // 暫時返回空集合，實際實現需要詳細的比較邏輯
            return changes;
        });
    }

    /**
     * 掃描結果內部類
     */
    private static class ScanResult {
        final BlockData blockData;
        final int importance;
        
        ScanResult(BlockData blockData, int importance) {
            this.blockData = blockData;
            this.importance = importance;
        }
    }
}