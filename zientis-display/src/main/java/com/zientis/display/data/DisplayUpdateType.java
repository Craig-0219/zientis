package com.zientis.display.data;

/**
 * 展示更新類型枚舉
 * 
 * 定義不同類型的展示更新操作
 */
public enum DisplayUpdateType {
    
    /**
     * 完全重建展示模型
     * - 清除現有所有方塊
     * - 重新掃描島嶼
     * - 重新生成微縮模型
     */
    FULL_REBUILD("完全重建", "重新掃描島嶼並完全重建展示模型"),
    
    /**
     * 增量更新
     * - 僅更新變化的方塊
     * - 保留現有未變化的部分
     * - 效率較高
     */
    INCREMENTAL("增量更新", "僅更新變化的方塊部分"),
    
    /**
     * 僅更新全息圖
     * - 更新玩家信息
     * - 更新統計數據
     * - 不改變方塊結構
     */
    HOLOGRAM_ONLY("全息圖更新", "僅更新全息圖信息"),
    
    /**
     * 僅更新粒子效果
     * - 更新粒子效果類型
     * - 調整效果參數
     * - 不影響其他元素
     */
    PARTICLE_ONLY("粒子效果更新", "僅更新粒子效果"),
    
    /**
     * 位置調整
     * - 移動展示位置
     * - 保持模型結構不變
     * - 更新座標相關數據
     */
    POSITION_UPDATE("位置更新", "調整展示模型位置"),
    
    /**
     * 等級升級更新
     * - 島嶼等級提升時觸發
     * - 可能改變展示等級
     * - 更新視覺效果
     */
    TIER_UPGRADE("等級升級", "島嶼等級提升觸發的更新"),
    
    /**
     * 強制刷新
     * - 用於修復顯示問題
     * - 重新渲染所有元素
     * - 不重新掃描島嶼
     */
    FORCE_REFRESH("強制刷新", "強制重新渲染所有元素");

    private final String displayName;
    private final String description;

    DisplayUpdateType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 檢查是否需要重新掃描島嶼
     */
    public boolean requiresIslandScan() {
        return this == FULL_REBUILD || this == INCREMENTAL || this == TIER_UPGRADE;
    }

    /**
     * 檢查是否是結構性更新
     */
    public boolean isStructuralUpdate() {
        return this == FULL_REBUILD || this == INCREMENTAL || this == POSITION_UPDATE;
    }

    /**
     * 檢查是否是輕量級更新
     */
    public boolean isLightweightUpdate() {
        return this == HOLOGRAM_ONLY || this == PARTICLE_ONLY;
    }

    /**
     * 獲取更新優先級 (數字越小優先級越高)
     */
    public int getPriority() {
        switch (this) {
            case FORCE_REFRESH: return 1;
            case FULL_REBUILD: return 2;
            case TIER_UPGRADE: return 3;
            case POSITION_UPDATE: return 4;
            case INCREMENTAL: return 5;
            case HOLOGRAM_ONLY: return 6;
            case PARTICLE_ONLY: return 7;
            default: return 10;
        }
    }

    /**
     * 獲取預估執行時間 (毫秒)
     */
    public long getEstimatedExecutionTime() {
        switch (this) {
            case FULL_REBUILD: return 5000; // 5秒
            case TIER_UPGRADE: return 3000; // 3秒
            case INCREMENTAL: return 2000; // 2秒
            case POSITION_UPDATE: return 1000; // 1秒
            case FORCE_REFRESH: return 800; // 0.8秒
            case HOLOGRAM_ONLY: return 200; // 0.2秒
            case PARTICLE_ONLY: return 100; // 0.1秒
            default: return 1000;
        }
    }

    // === Getters ===
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
}