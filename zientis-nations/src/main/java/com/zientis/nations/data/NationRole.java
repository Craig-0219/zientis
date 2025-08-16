package com.zientis.nations.data;

import java.util.Set;

/**
 * 國家成員角色枚舉
 * 
 * 定義不同角色的權限等級
 */
public enum NationRole {
    
    KING("國王", 100, Set.of(
        NationPermission.ALL_PERMISSIONS,
        NationPermission.MANAGE_MEMBERS,
        NationPermission.MANAGE_ROLES,
        NationPermission.MANAGE_TREASURY,
        NationPermission.MANAGE_TERRITORY,
        NationPermission.MANAGE_DIPLOMACY,
        NationPermission.DECLARE_WAR,
        NationPermission.MANAGE_SETTINGS,
        NationPermission.DISBAND_NATION
    )),
    
    MINISTER("大臣", 80, Set.of(
        NationPermission.MANAGE_MEMBERS,
        NationPermission.MANAGE_TREASURY,
        NationPermission.MANAGE_TERRITORY,
        NationPermission.MANAGE_DIPLOMACY,
        NationPermission.VIEW_TREASURY,
        NationPermission.INVITE_MEMBERS
    )),
    
    GENERAL("將軍", 60, Set.of(
        NationPermission.DECLARE_WAR,
        NationPermission.MANAGE_MILITARY,
        NationPermission.VIEW_TREASURY,
        NationPermission.KICK_MEMBERS
    )),
    
    NOBLE("貴族", 40, Set.of(
        NationPermission.INVITE_MEMBERS,
        NationPermission.MANAGE_BUILD,
        NationPermission.VIEW_TREASURY,
        NationPermission.ACCESS_TERRITORY
    )),
    
    CITIZEN("公民", 20, Set.of(
        NationPermission.ACCESS_TERRITORY,
        NationPermission.CHAT_NATION,
        NationPermission.VIEW_INFO
    )),
    
    RECRUIT("新兵", 10, Set.of(
        NationPermission.ACCESS_TERRITORY,
        NationPermission.CHAT_NATION
    ));
    
    private final String displayName;
    private final int priority;
    private final Set<NationPermission> permissions;
    
    NationRole(String displayName, int priority, Set<NationPermission> permissions) {
        this.displayName = displayName;
        this.priority = priority;
        this.permissions = permissions;
    }
    
    /**
     * 檢查角色是否擁有特定權限
     */
    public boolean hasPermission(NationPermission permission) {
        return permissions.contains(NationPermission.ALL_PERMISSIONS) || 
               permissions.contains(permission);
    }
    
    /**
     * 檢查是否可以管理目標角色
     */
    public boolean canManage(NationRole targetRole) {
        return this.priority > targetRole.priority;
    }
    
    /**
     * 獲取角色的顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 獲取角色優先級
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * 獲取角色權限集合
     */
    public Set<NationPermission> getPermissions() {
        return Set.copyOf(permissions);
    }
    
    /**
     * 檢查是否是管理層角色
     */
    public boolean isManagement() {
        return priority >= 60;
    }
    
    /**
     * 檢查是否是領導層角色
     */
    public boolean isLeadership() {
        return priority >= 80;
    }
    
    /**
     * 獲取角色顏色代碼 (用於顯示)
     */
    public String getColorCode() {
        switch (this) {
            case KING: return "§6"; // 金色
            case MINISTER: return "§5"; // 紫色
            case GENERAL: return "§c"; // 紅色
            case NOBLE: return "§9"; // 藍色
            case CITIZEN: return "§a"; // 綠色
            case RECRUIT: return "§7"; // 灰色
            default: return "§f"; // 白色
        }
    }
    
    /**
     * 從字符串獲取角色
     */
    public static NationRole fromString(String name) {
        for (NationRole role : values()) {
            if (role.name().equalsIgnoreCase(name) || 
                role.displayName.equals(name)) {
                return role;
            }
        }
        return CITIZEN; // 默認角色
    }
    
    /**
     * 獲取可升級到的下一個角色
     */
    public NationRole getNextRole() {
        switch (this) {
            case RECRUIT: return CITIZEN;
            case CITIZEN: return NOBLE;
            case NOBLE: return GENERAL;
            case GENERAL: return MINISTER;
            case MINISTER: return KING;
            default: return this;
        }
    }
    
    /**
     * 獲取可降級到的下一個角色
     */
    public NationRole getPreviousRole() {
        switch (this) {
            case KING: return MINISTER;
            case MINISTER: return GENERAL;
            case GENERAL: return NOBLE;
            case NOBLE: return CITIZEN;
            case CITIZEN: return RECRUIT;
            default: return this;
        }
    }
}