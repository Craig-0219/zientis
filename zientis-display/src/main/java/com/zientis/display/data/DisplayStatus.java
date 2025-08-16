package com.zientis.display.data;

/**
 * 展示狀態枚舉
 * 
 * 表示展示模型當前的狀態
 */
public enum DisplayStatus {
    
    /**
     * 創建中
     * - 正在掃描島嶼
     * - 正在生成微縮模型
     * - 尚未完成展示
     */
    CREATING("創建中", "正在生成展示模型"),
    
    /**
     * 活躍狀態
     * - 展示正常工作
     * - 可以被玩家看到和互動
     * - 定期自動更新
     */
    ACTIVE("活躍", "展示正常運行中"),
    
    /**
     * 更新中
     * - 正在更新展示內容
     * - 可能暫時不可見
     * - 即將恢復正常
     */
    UPDATING("更新中", "正在更新展示內容"),
    
    /**
     * 暫停狀態
     * - 展示被手動暫停
     * - 不進行自動更新
     * - 仍然可見但靜態
     */
    PAUSED("已暫停", "展示已被暫停"),
    
    /**
     * 錯誤狀態
     * - 展示出現錯誤
     * - 需要管理員介入
     * - 可能不完整或不正確
     */
    ERROR("錯誤", "展示出現錯誤"),
    
    /**
     * 隱藏狀態
     * - 展示被隱藏
     * - 玩家無法看到
     * - 仍保留在記憶體中
     */
    HIDDEN("已隱藏", "展示已被隱藏"),
    
    /**
     * 移除中
     * - 正在移除展示
     * - 清理相關資源
     * - 即將從系統中刪除
     */
    REMOVING("移除中", "正在移除展示"),
    
    /**
     * 已移除
     * - 展示已完全移除
     * - 資源已清理
     * - 不再存在於系統中
     */
    REMOVED("已移除", "展示已被移除");

    private final String displayName;
    private final String description;

    DisplayStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 檢查是否為可見狀態
     */
    public boolean isVisible() {
        return this == ACTIVE || this == UPDATING || this == PAUSED;
    }

    /**
     * 檢查是否為可互動狀態
     */
    public boolean isInteractable() {
        return this == ACTIVE || this == PAUSED;
    }

    /**
     * 檢查是否為處理中狀態
     */
    public boolean isProcessing() {
        return this == CREATING || this == UPDATING || this == REMOVING;
    }

    /**
     * 檢查是否為正常運行狀態
     */
    public boolean isOperational() {
        return this == ACTIVE || this == PAUSED;
    }

    /**
     * 檢查是否需要管理員注意
     */
    public boolean needsAttention() {
        return this == ERROR;
    }

    /**
     * 檢查是否可以更新
     */
    public boolean canUpdate() {
        return this == ACTIVE || this == PAUSED || this == ERROR;
    }

    /**
     * 檢查是否可以移除
     */
    public boolean canRemove() {
        return this != REMOVING && this != REMOVED;
    }

    /**
     * 獲取狀態顏色碼 (用於顯示)
     */
    public String getColorCode() {
        switch (this) {
            case ACTIVE: return "§a"; // 綠色
            case CREATING: return "§e"; // 黃色
            case UPDATING: return "§6"; // 橙色
            case PAUSED: return "§7"; // 灰色
            case ERROR: return "§c"; // 紅色
            case HIDDEN: return "§8"; // 深灰色
            case REMOVING: return "§c"; // 紅色
            case REMOVED: return "§4"; // 深紅色
            default: return "§f"; // 白色
        }
    }

    /**
     * 獲取下一個合理的狀態轉換
     */
    public DisplayStatus[] getValidTransitions() {
        switch (this) {
            case CREATING:
                return new DisplayStatus[]{ACTIVE, ERROR, REMOVING};
            case ACTIVE:
                return new DisplayStatus[]{UPDATING, PAUSED, HIDDEN, REMOVING};
            case UPDATING:
                return new DisplayStatus[]{ACTIVE, ERROR, REMOVING};
            case PAUSED:
                return new DisplayStatus[]{ACTIVE, HIDDEN, REMOVING};
            case ERROR:
                return new DisplayStatus[]{UPDATING, REMOVING};
            case HIDDEN:
                return new DisplayStatus[]{ACTIVE, REMOVING};
            case REMOVING:
                return new DisplayStatus[]{REMOVED};
            case REMOVED:
                return new DisplayStatus[]{}; // 無法轉換
            default:
                return new DisplayStatus[]{};
        }
    }

    // === Getters ===
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * 獲取帶顏色的顯示名稱
     */
    public String getColoredDisplayName() {
        return getColorCode() + displayName;
    }

    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
}