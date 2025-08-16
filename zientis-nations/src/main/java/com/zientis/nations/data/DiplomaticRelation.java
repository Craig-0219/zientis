package com.zientis.nations.data;

/**
 * 外交關係枚舉
 * 
 * 定義國家間的各種外交關係狀態
 */
public enum DiplomaticRelation {
    
    ALLIANCE("聯盟", "友好的軍事和經濟聯盟", "§a", 100),
    
    PEACE_TREATY("和平條約", "正式的和平協議", "§b", 80),
    
    TRADE_AGREEMENT("貿易協定", "經濟合作協議", "§e", 60),
    
    NON_AGGRESSION("互不侵犯", "互不侵犯協定", "§f", 40),
    
    NEUTRAL("中立", "沒有特殊關係的中立狀態", "§7", 0),
    
    TENSION("緊張", "關係緊張但未達到敵對", "§6", -20),
    
    EMBARGO("制裁", "經濟制裁和貿易禁運", "§c", -40),
    
    HOSTILITY("敵對", "公開的敵對關係", "§4", -60),
    
    WAR("戰爭", "處於戰爭狀態", "§4§l", -100);
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    private final int relationValue; // 關係值，正數為友好，負數為敵對
    
    DiplomaticRelation(String displayName, String description, String colorCode, int relationValue) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
        this.relationValue = relationValue;
    }
    
    /**
     * 獲取顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 獲取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 獲取顏色代碼
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * 獲取關係數值
     */
    public int getRelationValue() {
        return relationValue;
    }
    
    /**
     * 獲取帶顏色的顯示名稱
     */
    public String getColoredDisplayName() {
        return colorCode + displayName + "§r";
    }
    
    /**
     * 檢查是否是友好關係
     */
    public boolean isFriendly() {
        return relationValue > 0;
    }
    
    /**
     * 檢查是否是敵對關係
     */
    public boolean isHostile() {
        return relationValue < 0;
    }
    
    /**
     * 檢查是否是中立關係
     */
    public boolean isNeutral() {
        return relationValue == 0;
    }
    
    /**
     * 檢查是否處於戰爭狀態
     */
    public boolean isAtWar() {
        return this == WAR;
    }
    
    /**
     * 檢查是否可以進入對方領土
     */
    public boolean canEnterTerritory() {
        return this == ALLIANCE || this == PEACE_TREATY || this == TRADE_AGREEMENT;
    }
    
    /**
     * 檢查是否可以進行貿易
     */
    public boolean canTrade() {
        return relationValue >= 0 && this != EMBARGO;
    }
    
    /**
     * 檢查是否可以宣戰
     */
    public boolean canDeclareWar() {
        return this != ALLIANCE && this != WAR;
    }
    
    /**
     * 檢查是否可以結成聯盟
     */
    public boolean canFormAlliance() {
        return this == PEACE_TREATY || this == TRADE_AGREEMENT;
    }
    
    /**
     * 獲取貿易稅率修正 (百分比)
     */
    public double getTradeModifier() {
        switch (this) {
            case ALLIANCE: return -0.20; // 20% 折扣
            case PEACE_TREATY: return -0.10; // 10% 折扣
            case TRADE_AGREEMENT: return -0.15; // 15% 折扣
            case NON_AGGRESSION: return 0.0; // 正常價格
            case NEUTRAL: return 0.05; // 5% 額外費用
            case TENSION: return 0.15; // 15% 額外費用
            case EMBARGO: return Double.MAX_VALUE; // 無法貿易
            case HOSTILITY: return Double.MAX_VALUE; // 無法貿易
            case WAR: return Double.MAX_VALUE; // 無法貿易
            default: return 0.0;
        }
    }
    
    /**
     * 獲取外交關係的圖標
     */
    public String getIcon() {
        switch (this) {
            case ALLIANCE: return "🤝";
            case PEACE_TREATY: return "🕊️";
            case TRADE_AGREEMENT: return "💼";
            case NON_AGGRESSION: return "🤝";
            case NEUTRAL: return "⚪";
            case TENSION: return "⚠️";
            case EMBARGO: return "🚫";
            case HOSTILITY: return "💥";
            case WAR: return "⚔️";
            default: return "❓";
        }
    }
    
    /**
     * 獲取建立此關係所需的時間 (小時)
     */
    public int getEstablishmentTime() {
        switch (this) {
            case ALLIANCE: return 72; // 3天
            case PEACE_TREATY: return 48; // 2天
            case TRADE_AGREEMENT: return 24; // 1天
            case NON_AGGRESSION: return 12; // 半天
            case NEUTRAL: return 0; // 立即
            case TENSION: return 0; // 立即
            case EMBARGO: return 6; // 6小時
            case HOSTILITY: return 0; // 立即
            case WAR: return 0; // 立即
            default: return 0;
        }
    }
    
    /**
     * 獲取關係的穩定性 (0-100，越高越穩定)
     */
    public int getStability() {
        switch (this) {
            case ALLIANCE: return 90;
            case PEACE_TREATY: return 80;
            case TRADE_AGREEMENT: return 70;
            case NON_AGGRESSION: return 60;
            case NEUTRAL: return 50;
            case TENSION: return 30;
            case EMBARGO: return 20;
            case HOSTILITY: return 10;
            case WAR: return 5;
            default: return 50;
        }
    }
    
    /**
     * 獲取可以轉變到的關係列表
     */
    public DiplomaticRelation[] getPossibleTransitions() {
        switch (this) {
            case ALLIANCE:
                return new DiplomaticRelation[]{PEACE_TREATY, TRADE_AGREEMENT, NEUTRAL};
            case PEACE_TREATY:
                return new DiplomaticRelation[]{ALLIANCE, TRADE_AGREEMENT, NON_AGGRESSION, NEUTRAL};
            case TRADE_AGREEMENT:
                return new DiplomaticRelation[]{ALLIANCE, PEACE_TREATY, NON_AGGRESSION, NEUTRAL, TENSION};
            case NON_AGGRESSION:
                return new DiplomaticRelation[]{PEACE_TREATY, TRADE_AGREEMENT, NEUTRAL, TENSION};
            case NEUTRAL:
                return new DiplomaticRelation[]{NON_AGGRESSION, TRADE_AGREEMENT, TENSION, EMBARGO, HOSTILITY};
            case TENSION:
                return new DiplomaticRelation[]{NEUTRAL, EMBARGO, HOSTILITY, WAR};
            case EMBARGO:
                return new DiplomaticRelation[]{TENSION, HOSTILITY, WAR, NEUTRAL};
            case HOSTILITY:
                return new DiplomaticRelation[]{WAR, EMBARGO, TENSION};
            case WAR:
                return new DiplomaticRelation[]{PEACE_TREATY, NEUTRAL, HOSTILITY};
            default:
                return new DiplomaticRelation[]{NEUTRAL};
        }
    }
    
    /**
     * 從字符串獲取外交關係
     */
    public static DiplomaticRelation fromString(String name) {
        for (DiplomaticRelation relation : values()) {
            if (relation.name().equalsIgnoreCase(name) || 
                relation.displayName.equals(name)) {
                return relation;
            }
        }
        return NEUTRAL; // 默認關係
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (%+d)", getIcon(), displayName, relationValue);
    }
}