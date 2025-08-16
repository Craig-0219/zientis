package com.zientis.nations.data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 國家數據模型
 * 
 * 代表一個國家的完整信息，包含成員、領土、經濟和外交關係
 */
public class Nation {
    
    private final UUID nationId;
    private final String nationName;
    private final UUID founderId;
    private final LocalDateTime foundedDate;
    
    // 基本信息
    private String description;
    private String motd; // Message of the Day
    private NationFlag flag;
    private NationLevel level;
    
    // 成員管理
    private final Set<UUID> members;
    private final Map<UUID, NationRole> memberRoles;
    private int maxMembers;
    
    // 領土系統
    private final Set<UUID> territories; // 島嶼ID集合
    private UUID capitalIsland;
    
    // 經濟系統
    private double treasury;
    private double dailyTax;
    private double memberTax;
    
    // 外交關係
    private final Map<UUID, DiplomaticRelation> diplomaticRelations;
    
    // 戰爭系統
    private final Set<UUID> activeWars;
    private final Set<UUID> warHistory;
    
    // 統計數據
    private NationStats stats;
    private LocalDateTime lastActivity;
    
    // Discord整合 (預留)
    private String discordGuildId;
    private String discordRoleId;
    private boolean discordIntegrationEnabled;

    public Nation(String nationName, UUID founderId) {
        this.nationId = UUID.randomUUID();
        this.nationName = nationName;
        this.founderId = founderId;
        this.foundedDate = LocalDateTime.now();
        
        // 初始化集合
        this.members = ConcurrentHashMap.newKeySet();
        this.memberRoles = new ConcurrentHashMap<>();
        this.territories = ConcurrentHashMap.newKeySet();
        this.diplomaticRelations = new ConcurrentHashMap<>();
        this.activeWars = ConcurrentHashMap.newKeySet();
        this.warHistory = ConcurrentHashMap.newKeySet();
        
        // 設置默認值
        this.level = NationLevel.SETTLEMENT;
        this.maxMembers = 5; // 起始成員限制
        this.treasury = 0.0;
        this.dailyTax = 0.0;
        this.memberTax = 0.0;
        this.stats = new NationStats();
        this.lastActivity = LocalDateTime.now();
        this.discordIntegrationEnabled = false;
        
        // 添加創始人為國王
        this.members.add(founderId);
        this.memberRoles.put(founderId, NationRole.KING);
    }

    // === 成員管理方法 ===
    
    /**
     * 添加成員到國家
     */
    public boolean addMember(UUID playerId, NationRole role) {
        if (members.size() >= maxMembers) {
            return false;
        }
        
        members.add(playerId);
        memberRoles.put(playerId, role != null ? role : NationRole.CITIZEN);
        updateLastActivity();
        
        return true;
    }
    
    /**
     * 移除國家成員
     */
    public boolean removeMember(UUID playerId) {
        if (playerId.equals(founderId)) {
            return false; // 不能移除創始人
        }
        
        boolean removed = members.remove(playerId);
        if (removed) {
            memberRoles.remove(playerId);
            updateLastActivity();
        }
        
        return removed;
    }
    
    /**
     * 設置成員角色
     */
    public boolean setMemberRole(UUID playerId, NationRole role) {
        if (!members.contains(playerId) || playerId.equals(founderId)) {
            return false; // 創始人角色不可更改
        }
        
        memberRoles.put(playerId, role);
        updateLastActivity();
        return true;
    }
    
    /**
     * 檢查玩家是否有特定權限
     */
    public boolean hasPermission(UUID playerId, NationPermission permission) {
        NationRole role = memberRoles.get(playerId);
        return role != null && role.hasPermission(permission);
    }
    
    // === 領土管理方法 ===
    
    /**
     * 添加領土島嶼
     */
    public boolean addTerritory(UUID islandId) {
        boolean added = territories.add(islandId);
        if (added) {
            updateLastActivity();
            // 如果是第一個領土，設為首都
            if (capitalIsland == null) {
                capitalIsland = islandId;
            }
        }
        return added;
    }
    
    /**
     * 移除領土島嶼
     */
    public boolean removeTerritory(UUID islandId) {
        boolean removed = territories.remove(islandId);
        if (removed) {
            updateLastActivity();
            // 如果移除的是首都，需要重新選擇首都
            if (islandId.equals(capitalIsland)) {
                capitalIsland = territories.isEmpty() ? null : territories.iterator().next();
            }
        }
        return removed;
    }
    
    /**
     * 設置首都島嶼
     */
    public boolean setCapital(UUID islandId) {
        if (!territories.contains(islandId)) {
            return false;
        }
        
        this.capitalIsland = islandId;
        updateLastActivity();
        return true;
    }
    
    // === 經濟管理方法 ===
    
    /**
     * 存入國庫
     */
    public boolean depositToTreasury(double amount) {
        if (amount <= 0) {
            return false;
        }
        
        treasury += amount;
        stats.recordTransaction(amount);
        updateLastActivity();
        return true;
    }
    
    /**
     * 從國庫提取
     */
    public boolean withdrawFromTreasury(double amount) {
        if (amount <= 0 || amount > treasury) {
            return false;
        }
        
        treasury -= amount;
        stats.recordTransaction(-amount);
        updateLastActivity();
        return true;
    }
    
    /**
     * 收取稅收
     */
    public void collectTaxes(double totalAmount) {
        treasury += totalAmount;
        stats.recordTaxCollection(totalAmount);
        updateLastActivity();
    }
    
    // === 外交關係方法 ===
    
    /**
     * 設置與其他國家的外交關係
     */
    public void setDiplomaticRelation(UUID otherNationId, DiplomaticRelation relation) {
        diplomaticRelations.put(otherNationId, relation);
        updateLastActivity();
    }
    
    /**
     * 獲取與特定國家的外交關係
     */
    public DiplomaticRelation getDiplomaticRelation(UUID otherNationId) {
        return diplomaticRelations.getOrDefault(otherNationId, DiplomaticRelation.NEUTRAL);
    }
    
    /**
     * 檢查是否可以對特定國家宣戰
     */
    public boolean canDeclareWarOn(UUID otherNationId) {
        DiplomaticRelation relation = getDiplomaticRelation(otherNationId);
        return relation != DiplomaticRelation.ALLIANCE && 
               !activeWars.contains(otherNationId);
    }
    
    // === 戰爭系統方法 ===
    
    /**
     * 對其他國家宣戰
     */
    public boolean declareWar(UUID targetNationId) {
        if (!canDeclareWarOn(targetNationId)) {
            return false;
        }
        
        activeWars.add(targetNationId);
        setDiplomaticRelation(targetNationId, DiplomaticRelation.WAR);
        stats.recordWarDeclaration();
        updateLastActivity();
        
        return true;
    }
    
    /**
     * 結束戰爭
     */
    public boolean endWar(UUID targetNationId, boolean victory) {
        boolean removed = activeWars.remove(targetNationId);
        if (removed) {
            warHistory.add(targetNationId);
            setDiplomaticRelation(targetNationId, DiplomaticRelation.NEUTRAL);
            stats.recordWarEnd(victory);
            updateLastActivity();
        }
        return removed;
    }
    
    // === 國家升級系統 ===
    
    /**
     * 檢查是否可以升級國家等級
     */
    public boolean canUpgrade() {
        return level.canUpgradeTo(getNextLevel()) && 
               members.size() >= level.getRequiredMembers() &&
               territories.size() >= level.getRequiredTerritories() &&
               treasury >= level.getUpgradeCost();
    }
    
    /**
     * 升級國家等級
     */
    public boolean upgrade() {
        if (!canUpgrade()) {
            return false;
        }
        
        NationLevel nextLevel = getNextLevel();
        double upgradeCost = level.getUpgradeCost();
        
        if (withdrawFromTreasury(upgradeCost)) {
            this.level = nextLevel;
            this.maxMembers = nextLevel.getMaxMembers();
            stats.recordUpgrade();
            updateLastActivity();
            return true;
        }
        
        return false;
    }
    
    private NationLevel getNextLevel() {
        return level.getNextLevel();
    }
    
    // === Discord 整合方法 (預留) ===
    
    /**
     * 啟用Discord整合
     */
    public boolean enableDiscordIntegration(String guildId, String roleId) {
        if (guildId == null || roleId == null) {
            return false;
        }
        
        this.discordGuildId = guildId;
        this.discordRoleId = roleId;
        this.discordIntegrationEnabled = true;
        updateLastActivity();
        
        return true;
    }
    
    /**
     * 停用Discord整合
     */
    public void disableDiscordIntegration() {
        this.discordIntegrationEnabled = false;
        this.discordGuildId = null;
        this.discordRoleId = null;
        updateLastActivity();
    }
    
    /**
     * 獲取Discord Bot API數據
     */
    public DiscordNationData getDiscordData() {
        return new DiscordNationData(
            nationId,
            nationName,
            description,
            level,
            members.size(),
            territories.size(),
            treasury,
            foundedDate,
            lastActivity,
            stats.getWarWins(),
            stats.getWarLosses(),
            discordIntegrationEnabled
        );
    }
    
    // === 輔助方法 ===
    
    private void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * 檢查國家是否活躍
     */
    public boolean isActive() {
        return lastActivity.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 獲取國家權力評分
     */
    public double getPowerScore() {
        double memberScore = members.size() * 10;
        double territoryScore = territories.size() * 50;
        double economyScore = treasury * 0.1;
        double militaryScore = stats.getWarWins() * 100 - stats.getWarLosses() * 50;
        
        return memberScore + territoryScore + economyScore + militaryScore;
    }
    
    // === Getter/Setter 方法 ===
    
    public UUID getNationId() { return nationId; }
    public String getNationName() { return nationName; }
    public UUID getFounderId() { return founderId; }
    public LocalDateTime getFoundedDate() { return foundedDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        updateLastActivity();
    }
    
    public String getMotd() { return motd; }
    public void setMotd(String motd) { 
        this.motd = motd; 
        updateLastActivity();
    }
    
    public NationFlag getFlag() { return flag; }
    public void setFlag(NationFlag flag) { 
        this.flag = flag; 
        updateLastActivity();
    }
    
    public NationLevel getLevel() { return level; }
    public Set<UUID> getMembers() { return Set.copyOf(members); }
    public Map<UUID, NationRole> getMemberRoles() { return Map.copyOf(memberRoles); }
    public int getMaxMembers() { return maxMembers; }
    
    public Set<UUID> getTerritories() { return Set.copyOf(territories); }
    public UUID getCapitalIsland() { return capitalIsland; }
    
    public double getTreasury() { return treasury; }
    public double getDailyTax() { return dailyTax; }
    public void setDailyTax(double dailyTax) { 
        this.dailyTax = Math.max(0, dailyTax); 
        updateLastActivity();
    }
    
    public double getMemberTax() { return memberTax; }
    public void setMemberTax(double memberTax) { 
        this.memberTax = Math.max(0, memberTax); 
        updateLastActivity();
    }
    
    public Map<UUID, DiplomaticRelation> getDiplomaticRelations() { 
        return Map.copyOf(diplomaticRelations); 
    }
    
    public Set<UUID> getActiveWars() { return Set.copyOf(activeWars); }
    public Set<UUID> getWarHistory() { return Set.copyOf(warHistory); }
    
    public NationStats getStats() { return stats; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    
    public String getDiscordGuildId() { return discordGuildId; }
    public String getDiscordRoleId() { return discordRoleId; }
    public boolean isDiscordIntegrationEnabled() { return discordIntegrationEnabled; }
    
    @Override
    public String toString() {
        return String.format("Nation{id=%s, name=%s, level=%s, members=%d, territories=%d, treasury=%.2f}", 
            nationId, nationName, level, members.size(), territories.size(), treasury);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Nation nation = (Nation) obj;
        return nationId.equals(nation.nationId);
    }
    
    @Override
    public int hashCode() {
        return nationId.hashCode();
    }
}