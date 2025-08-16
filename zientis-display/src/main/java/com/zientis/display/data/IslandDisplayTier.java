package com.zientis.display.data;

/**
 * 島嶼展示等級枚舉
 * 
 * 根據島嶼等級決定展示效果的複雜度和視覺品質
 */
public enum IslandDisplayTier {
    
    /**
     * 基礎等級 (島嶼等級 1-10)
     * - 簡單方塊材質
     * - 無特殊效果
     * - 基礎全息圖
     */
    BASIC(1, 10, "基礎", "簡單方塊材質，基礎全息效果"),
    
    /**
     * 增強等級 (島嶼等級 11-30)
     * - 增強材質效果
     * - 基礎粒子效果
     * - 詳細全息圖
     */
    ENHANCED(11, 30, "增強", "增強材質效果，基礎粒子效果"),
    
    /**
     * 進階等級 (島嶼等級 31-50)
     * - 複雜結構展示
     * - 豐富粒子效果
     * - 動態全息圖
     */
    ADVANCED(31, 50, "進階", "複雜結構展示，豐富粒子效果"),
    
    /**
     * 頂級等級 (島嶼等級 51+)
     * - 頂級視覺效果
     * - 獨特動畫效果
     * - 互動全息圖
     */
    PREMIUM(51, Integer.MAX_VALUE, "頂級", "頂級視覺效果，獨特動畫效果");

    private final int minLevel;
    private final int maxLevel;
    private final String displayName;
    private final String description;

    IslandDisplayTier(int minLevel, int maxLevel, String displayName, String description) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根據島嶼等級獲取對應的展示等級
     */
    public static IslandDisplayTier fromIslandLevel(int islandLevel) {
        for (IslandDisplayTier tier : values()) {
            if (islandLevel >= tier.minLevel && islandLevel <= tier.maxLevel) {
                return tier;
            }
        }
        return BASIC; // 默認返回基礎等級
    }

    /**
     * 檢查島嶼等級是否屬於此展示等級
     */
    public boolean containsLevel(int islandLevel) {
        return islandLevel >= minLevel && islandLevel <= maxLevel;
    }

    /**
     * 獲取最大同時渲染方塊數
     */
    public int getMaxRenderBlocks() {
        switch (this) {
            case BASIC: return 100;
            case ENHANCED: return 250;
            case ADVANCED: return 500;
            case PREMIUM: return 1000;
            default: return 100;
        }
    }

    /**
     * 獲取粒子效果更新頻率 (毫秒)
     */
    public long getParticleUpdateInterval() {
        switch (this) {
            case BASIC: return 5000; // 5秒
            case ENHANCED: return 3000; // 3秒
            case ADVANCED: return 2000; // 2秒
            case PREMIUM: return 1000; // 1秒
            default: return 5000;
        }
    }

    /**
     * 獲取全息圖更新頻率 (毫秒)
     */
    public long getHologramUpdateInterval() {
        switch (this) {
            case BASIC: return 30000; // 30秒
            case ENHANCED: return 20000; // 20秒
            case ADVANCED: return 15000; // 15秒
            case PREMIUM: return 10000; // 10秒
            default: return 30000;
        }
    }

    /**
     * 是否支援動態效果
     */
    public boolean supportsDynamicEffects() {
        return this == ADVANCED || this == PREMIUM;
    }

    /**
     * 是否支援互動功能
     */
    public boolean supportsInteraction() {
        return this == ENHANCED || this == ADVANCED || this == PREMIUM;
    }

    /**
     * 是否支援粒子效果
     */
    public boolean supportsParticleEffects() {
        return this != BASIC;
    }

    /**
     * 獲取全息圖行數限制
     */
    public int getMaxHologramLines() {
        switch (this) {
            case BASIC: return 3;
            case ENHANCED: return 5;
            case ADVANCED: return 7;
            case PREMIUM: return 10;
            default: return 3;
        }
    }

    // === Getters ===
    
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("%s (等級 %d-%d): %s", 
            displayName, minLevel, 
            maxLevel == Integer.MAX_VALUE ? 999 : maxLevel, 
            description);
    }
}