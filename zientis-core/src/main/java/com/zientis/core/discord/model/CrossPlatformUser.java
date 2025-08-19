package com.zientis.core.discord.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 跨平台用戶資料模型
 * 對應Discord Bot中的CrossPlatformUser
 */
public class CrossPlatformUser {
    
    private Long discordId;
    private UUID minecraftUuid;
    private String username;
    private int totalCoins;
    private int totalGems;
    private int totalExperience;
    private int level;
    private Map<String, Boolean> achievements;
    private LocalDateTime lastSync;
    private boolean syncEnabled;
    
    public CrossPlatformUser() {
        this.achievements = new HashMap<>();
        this.syncEnabled = true;
    }
    
    public CrossPlatformUser(Long discordId, UUID minecraftUuid, String username) {
        this();
        this.discordId = discordId;
        this.minecraftUuid = minecraftUuid;
        this.username = username;
    }
    
    /**
     * 建立經濟資料Map用於API傳輸
     */
    public Map<String, Object> getEconomyData() {
        Map<String, Object> data = new HashMap<>();
        data.put("total_coins", totalCoins);
        data.put("total_gems", totalGems);
        data.put("total_experience", totalExperience);
        data.put("level", level);
        data.put("last_sync", lastSync);
        return data;
    }
    
    /**
     * 建立成就資料Map用於API傳輸
     */
    public Map<String, Object> getAchievementData() {
        Map<String, Object> data = new HashMap<>();
        data.put("achievements", achievements);
        data.put("total_achievements", achievements.size());
        data.put("completed_achievements", achievements.values().stream()
                .mapToInt(completed -> completed ? 1 : 0)
                .sum());
        data.put("last_sync", lastSync);
        return data;
    }
    
    /**
     * 從API回應更新經濟資料
     */
    public void updateFromEconomyData(Map<String, Object> data) {
        if (data.containsKey("total_coins")) {
            this.totalCoins = ((Number) data.get("total_coins")).intValue();
        }
        if (data.containsKey("total_gems")) {
            this.totalGems = ((Number) data.get("total_gems")).intValue();
        }
        if (data.containsKey("total_experience")) {
            this.totalExperience = ((Number) data.get("total_experience")).intValue();
        }
        if (data.containsKey("level")) {
            this.level = ((Number) data.get("level")).intValue();
        }
        this.lastSync = LocalDateTime.now();
    }
    
    /**
     * 從API回應更新成就資料
     */
    @SuppressWarnings("unchecked")
    public void updateFromAchievementData(Map<String, Object> data) {
        if (data.containsKey("achievements") && data.get("achievements") instanceof Map) {
            Map<String, Boolean> newAchievements = (Map<String, Boolean>) data.get("achievements");
            this.achievements.putAll(newAchievements);
        }
        this.lastSync = LocalDateTime.now();
    }
    
    /**
     * 計算等級經驗需求
     */
    public int getExperienceForLevel(int level) {
        return level * level * 100;
    }
    
    /**
     * 計算當前等級進度百分比
     */
    public double getLevelProgress() {
        if (level <= 1) return 0.0;
        
        int currentLevelExp = getExperienceForLevel(level);
        int nextLevelExp = getExperienceForLevel(level + 1);
        int currentExp = totalExperience - currentLevelExp;
        int neededExp = nextLevelExp - currentLevelExp;
        
        return Math.min(100.0, Math.max(0.0, (double) currentExp / neededExp * 100.0));
    }
    
    // Getters and Setters
    
    public Long getDiscordId() {
        return discordId;
    }
    
    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }
    
    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }
    
    public void setMinecraftUuid(UUID minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getTotalCoins() {
        return totalCoins;
    }
    
    public void setTotalCoins(int totalCoins) {
        this.totalCoins = totalCoins;
    }
    
    public int getTotalGems() {
        return totalGems;
    }
    
    public void setTotalGems(int totalGems) {
        this.totalGems = totalGems;
    }
    
    public int getTotalExperience() {
        return totalExperience;
    }
    
    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public Map<String, Boolean> getAchievements() {
        return achievements;
    }
    
    public void setAchievements(Map<String, Boolean> achievements) {
        this.achievements = achievements;
    }
    
    public LocalDateTime getLastSync() {
        return lastSync;
    }
    
    public void setLastSync(LocalDateTime lastSync) {
        this.lastSync = lastSync;
    }
    
    public boolean isSyncEnabled() {
        return syncEnabled;
    }
    
    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
    
    @Override
    public String toString() {
        return String.format("CrossPlatformUser{discordId=%d, minecraftUuid=%s, username='%s', " +
                "coins=%d, gems=%d, experience=%d, level=%d, achievements=%d}", 
                discordId, minecraftUuid, username, totalCoins, totalGems, 
                totalExperience, level, achievements.size());
    }
}