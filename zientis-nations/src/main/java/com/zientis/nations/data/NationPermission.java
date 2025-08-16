package com.zientis.nations.data;

/**
 * 國家權限枚舉
 * 
 * 定義國家系統中的各種操作權限
 */
public enum NationPermission {
    
    // 超級權限
    ALL_PERMISSIONS("所有權限", "擁有所有操作權限"),
    
    // 成員管理權限
    MANAGE_MEMBERS("管理成員", "邀請和踢出成員"),
    MANAGE_ROLES("管理角色", "設置和修改成員角色"),
    INVITE_MEMBERS("邀請成員", "邀請新成員加入國家"),
    KICK_MEMBERS("踢出成員", "踢出低等級成員"),
    
    // 經濟管理權限
    MANAGE_TREASURY("管理國庫", "存取國庫資金"),
    VIEW_TREASURY("查看國庫", "查看國庫餘額和交易記錄"),
    SET_TAXES("設置稅收", "設置國家稅收政策"),
    
    // 領土管理權限
    MANAGE_TERRITORY("管理領土", "添加和移除國家領土"),
    CLAIM_TERRITORY("聲稱領土", "為國家聲稱新的領土"),
    UNCLAIM_TERRITORY("放棄領土", "放棄國家領土"),
    SET_CAPITAL("設置首都", "設置國家首都島嶼"),
    
    // 建設權限
    MANAGE_BUILD("管理建設", "在國家領土內建設和破壞"),
    ACCESS_TERRITORY("訪問領土", "進入國家領土"),
    
    // 外交權限
    MANAGE_DIPLOMACY("管理外交", "設置與其他國家的外交關係"),
    DECLARE_WAR("宣戰權限", "對其他國家宣戰"),
    MAKE_PEACE("媾和權限", "與敵對國家締結和平"),
    FORM_ALLIANCE("結盟權限", "與其他國家結成聯盟"),
    
    // 軍事權限
    MANAGE_MILITARY("管理軍事", "管理國家軍事相關事務"),
    LEAD_ARMY("統帥軍隊", "在戰爭中指揮軍隊"),
    
    // 系統權限
    MANAGE_SETTINGS("管理設置", "修改國家設置和配置"),
    VIEW_INFO("查看信息", "查看國家基本信息"),
    CHAT_NATION("國家聊天", "使用國家聊天頻道"),
    
    // 特殊權限
    DISBAND_NATION("解散國家", "解散整個國家"),
    TRANSFER_LEADERSHIP("轉讓領導", "轉讓國家領導權");
    
    private final String displayName;
    private final String description;
    
    NationPermission(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 獲取權限的顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 獲取權限的描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否是管理權限
     */
    public boolean isManagementPermission() {
        return this == ALL_PERMISSIONS ||
               this == MANAGE_MEMBERS ||
               this == MANAGE_ROLES ||
               this == MANAGE_TREASURY ||
               this == MANAGE_TERRITORY ||
               this == MANAGE_DIPLOMACY ||
               this == MANAGE_SETTINGS ||
               this == DISBAND_NATION;
    }
    
    /**
     * 檢查是否是軍事權限
     */
    public boolean isMilitaryPermission() {
        return this == DECLARE_WAR ||
               this == MANAGE_MILITARY ||
               this == LEAD_ARMY;
    }
    
    /**
     * 檢查是否是經濟權限
     */
    public boolean isEconomicPermission() {
        return this == MANAGE_TREASURY ||
               this == VIEW_TREASURY ||
               this == SET_TAXES;
    }
    
    /**
     * 獲取權限的權重 (用於排序)
     */
    public int getWeight() {
        if (this == ALL_PERMISSIONS) return 1000;
        if (isManagementPermission()) return 100;
        if (isMilitaryPermission()) return 80;
        if (isEconomicPermission()) return 60;
        return 20;
    }
    
    /**
     * 從字符串獲取權限
     */
    public static NationPermission fromString(String name) {
        for (NationPermission permission : values()) {
            if (permission.name().equalsIgnoreCase(name) || 
                permission.displayName.equals(name)) {
                return permission;
            }
        }
        return null;
    }
}