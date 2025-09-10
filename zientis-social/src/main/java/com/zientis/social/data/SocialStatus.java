package com.zientis.social.data;

/**
 * 社交狀態枚舉
 */
public enum SocialStatus {
    ONLINE("在線", "§a●", true),
    AWAY("暫時離開", "§e●", true),
    BUSY("忙碌", "§c●", false),
    DO_NOT_DISTURB("請勿打擾", "§4●", false),
    INVISIBLE("隱身", "§8●", false),
    OFFLINE("離線", "§7●", false);
    
    private final String displayName;
    private final String indicator;
    private final boolean acceptsMessages;
    
    SocialStatus(String displayName, String indicator, boolean acceptsMessages) {
        this.displayName = displayName;
        this.indicator = indicator;
        this.acceptsMessages = acceptsMessages;
    }
    
    /**
     * 獲取顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 獲取狀態指示器
     */
    public String getIndicator() {
        return indicator;
    }
    
    /**
     * 檢查是否接受訊息
     */
    public boolean acceptsMessages() {
        return acceptsMessages;
    }
    
    /**
     * 檢查是否對好友可見
     */
    public boolean isVisibleToFriends() {
        return this != INVISIBLE;
    }
    
    /**
     * 檢查是否為在線狀態
     */
    public boolean isOnline() {
        return this == ONLINE || this == AWAY || this == BUSY || this == DO_NOT_DISTURB;
    }
    
    /**
     * 獲取帶指示器的完整顯示文字
     */
    public String getFullDisplay() {
        return indicator + " " + displayName;
    }
    
    /**
     * 從字符串解析社交狀態
     */
    public static SocialStatus fromString(String statusStr) {
        if (statusStr == null) {
            return OFFLINE;
        }
        
        try {
            return valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OFFLINE;
        }
    }
    
    /**
     * 獲取所有可用的狀態（供玩家選擇）
     */
    public static SocialStatus[] getAvailableStatuses() {
        return new SocialStatus[]{ONLINE, AWAY, BUSY, DO_NOT_DISTURB, INVISIBLE};
    }
}