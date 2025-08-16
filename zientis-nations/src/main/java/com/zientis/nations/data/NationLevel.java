package com.zientis.nations.data;

/**
 * 國家等級枚舉
 * 
 * 定義國家的不同發展階段和相應的能力
 */
public enum NationLevel {
    
    SETTLEMENT("定居點", 1, 5, 1, 1000.0, 
        "剛剛建立的小型定居點"),
    
    VILLAGE("村莊", 2, 10, 2, 5000.0,
        "小型村莊社區，開始有基本的自治能力"),
    
    TOWN("城鎮", 3, 20, 4, 15000.0,
        "發展中的城鎮，具備商業和行政功能"),
    
    CITY("城市", 4, 35, 6, 35000.0,
        "繁榮的城市，擁有完善的基礎設施"),
    
    KINGDOM("王國", 5, 50, 10, 75000.0,
        "強大的王國，擁有廣闊領土和強大軍力"),
    
    EMPIRE("帝國", 6, 100, 15, 150000.0,
        "龐大的帝國，統治著廣闊的疆域");
    
    private final String displayName;
    private final int level;
    private final int maxMembers;
    private final int requiredTerritories;
    private final double upgradeCost;
    private final String description;
    
    NationLevel(String displayName, int level, int maxMembers, 
               int requiredTerritories, double upgradeCost, String description) {
        this.displayName = displayName;
        this.level = level;
        this.maxMembers = maxMembers;
        this.requiredTerritories = requiredTerritories;
        this.upgradeCost = upgradeCost;
        this.description = description;
    }
    
    /**
     * 獲取顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 獲取等級數值
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * 獲取最大成員數
     */
    public int getMaxMembers() {
        return maxMembers;
    }
    
    /**
     * 獲取升級所需的領土數量
     */
    public int getRequiredTerritories() {
        return requiredTerritories;
    }
    
    /**
     * 獲取升級到下一等級的成本
     */
    public double getUpgradeCost() {
        return upgradeCost;
    }
    
    /**
     * 獲取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 獲取升級所需的最少成員數 (通常為最大成員數的70%)
     */
    public int getRequiredMembers() {
        return Math.max(1, (int)(maxMembers * 0.7));
    }
    
    /**
     * 獲取下一個等級
     */
    public NationLevel getNextLevel() {
        switch (this) {
            case SETTLEMENT: return VILLAGE;
            case VILLAGE: return TOWN;
            case TOWN: return CITY;
            case CITY: return KINGDOM;
            case KINGDOM: return EMPIRE;
            default: return this; // 已經是最高等級
        }
    }
    
    /**
     * 檢查是否可以升級到目標等級
     */
    public boolean canUpgradeTo(NationLevel targetLevel) {
        return targetLevel == getNextLevel();
    }
    
    /**
     * 檢查是否是最高等級
     */
    public boolean isMaxLevel() {
        return this == EMPIRE;
    }
    
    /**
     * 獲取等級顏色代碼
     */
    public String getColorCode() {
        switch (this) {
            case SETTLEMENT: return "§7"; // 灰色
            case VILLAGE: return "§a"; // 綠色
            case TOWN: return "§e"; // 黃色
            case CITY: return "§b"; // 青色
            case KINGDOM: return "§d"; // 粉色
            case EMPIRE: return "§6"; // 金色
            default: return "§f"; // 白色
        }
    }
    
    /**
     * 獲取等級圖標
     */
    public String getIcon() {
        switch (this) {
            case SETTLEMENT: return "🏘️";
            case VILLAGE: return "🏠";
            case TOWN: return "🏘️";
            case CITY: return "🏙️";
            case KINGDOM: return "🏰";
            case EMPIRE: return "👑";
            default: return "🏛️";
        }
    }
    
    /**
     * 獲取每日維護費用
     */
    public double getDailyMaintenanceCost() {
        return maxMembers * 10.0 + requiredTerritories * 50.0;
    }
    
    /**
     * 獲取戰爭宣告的冷卻時間 (小時)
     */
    public int getWarCooldownHours() {
        switch (this) {
            case SETTLEMENT: return 168; // 7天
            case VILLAGE: return 120; // 5天
            case TOWN: return 96; // 4天
            case CITY: return 72; // 3天
            case KINGDOM: return 48; // 2天
            case EMPIRE: return 24; // 1天
            default: return 168;
        }
    }
    
    /**
     * 獲取可同時進行的戰爭數量
     */
    public int getMaxSimultaneousWars() {
        switch (this) {
            case SETTLEMENT: return 1;
            case VILLAGE: return 1;
            case TOWN: return 2;
            case CITY: return 2;
            case KINGDOM: return 3;
            case EMPIRE: return 5;
            default: return 1;
        }
    }
    
    /**
     * 獲取外交關係槽位數量
     */
    public int getDiplomaticSlots() {
        return level * 2 + 3;
    }
    
    /**
     * 從字符串獲取等級
     */
    public static NationLevel fromString(String name) {
        for (NationLevel level : values()) {
            if (level.name().equalsIgnoreCase(name) || 
                level.displayName.equals(name)) {
                return level;
            }
        }
        return SETTLEMENT; // 默認等級
    }
    
    /**
     * 從等級數值獲取等級
     */
    public static NationLevel fromLevel(int level) {
        for (NationLevel nationLevel : values()) {
            if (nationLevel.level == level) {
                return nationLevel;
            }
        }
        return SETTLEMENT;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (等級 %d)", getIcon(), displayName, level);
    }
}