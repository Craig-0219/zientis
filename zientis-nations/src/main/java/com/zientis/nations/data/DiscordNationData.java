package com.zientis.nations.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Discord Bot API 國家數據傳輸對象
 * 
 * 用於與Discord Bot進行數據交換的簡化國家信息
 */
public class DiscordNationData {
    
    @JsonProperty("nation_id")
    private final UUID nationId;
    
    @JsonProperty("nation_name")
    private final String nationName;
    
    @JsonProperty("description")
    private final String description;
    
    @JsonProperty("level")
    private final String level;
    
    @JsonProperty("level_display")
    private final String levelDisplay;
    
    @JsonProperty("member_count")
    private final int memberCount;
    
    @JsonProperty("territory_count")
    private final int territoryCount;
    
    @JsonProperty("treasury")
    private final double treasury;
    
    @JsonProperty("founded_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime foundedDate;
    
    @JsonProperty("last_activity")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastActivity;
    
    @JsonProperty("war_wins")
    private final int warWins;
    
    @JsonProperty("war_losses")
    private final int warLosses;
    
    @JsonProperty("win_rate")
    private final double winRate;
    
    @JsonProperty("power_score")
    private final double powerScore;
    
    @JsonProperty("is_active")
    private final boolean isActive;
    
    @JsonProperty("discord_integration")
    private final boolean discordIntegration;
    
    @JsonProperty("status")
    private final String status;
    
    @JsonProperty("status_emoji")
    private final String statusEmoji;
    
    public DiscordNationData(UUID nationId, String nationName, String description, 
                           NationLevel level, int memberCount, int territoryCount, 
                           double treasury, LocalDateTime foundedDate, LocalDateTime lastActivity,
                           int warWins, int warLosses, boolean discordIntegration) {
        this.nationId = nationId;
        this.nationName = nationName;
        this.description = description != null ? description : "這個國家還沒有描述";
        this.level = level.name();
        this.levelDisplay = level.toString();
        this.memberCount = memberCount;
        this.territoryCount = territoryCount;
        this.treasury = treasury;
        this.foundedDate = foundedDate;
        this.lastActivity = lastActivity;
        this.warWins = warWins;
        this.warLosses = warLosses;
        this.winRate = calculateWinRate(warWins, warLosses);
        this.powerScore = calculatePowerScore(memberCount, territoryCount, treasury, warWins, warLosses);
        this.isActive = isActiveNation(lastActivity);
        this.discordIntegration = discordIntegration;
        this.status = determineStatus();
        this.statusEmoji = determineStatusEmoji();
    }
    
    /**
     * 計算勝率
     */
    private double calculateWinRate(int wins, int losses) {
        int total = wins + losses;
        return total > 0 ? (double) wins / total * 100 : 0.0;
    }
    
    /**
     * 計算權力評分
     */
    private double calculatePowerScore(int members, int territories, double treasury, int wins, int losses) {
        double memberScore = members * 10;
        double territoryScore = territories * 50;
        double economyScore = treasury * 0.1;
        double militaryScore = wins * 100 - losses * 50;
        
        return memberScore + territoryScore + economyScore + militaryScore;
    }
    
    /**
     * 判斷國家是否活躍
     */
    private boolean isActiveNation(LocalDateTime lastActivity) {
        return lastActivity.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * 確定國家狀態
     */
    private String determineStatus() {
        if (!isActive) {
            return "休眠";
        } else if (warWins + warLosses > 0) {
            return "軍事活躍";
        } else if (memberCount >= 10) {
            return "繁榮發展";
        } else {
            return "發展中";
        }
    }
    
    /**
     * 確定狀態表情符號
     */
    private String determineStatusEmoji() {
        if (!isActive) {
            return "😴";
        } else if (warWins + warLosses > 0) {
            return "⚔️";
        } else if (memberCount >= 10) {
            return "🌟";
        } else {
            return "🌱";
        }
    }
    
    /**
     * 生成Discord嵌入式消息的標題
     */
    public String getDiscordTitle() {
        return String.format("%s %s", statusEmoji, nationName);
    }
    
    /**
     * 生成Discord嵌入式消息的描述
     */
    public String getDiscordDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("**").append(levelDisplay).append("**\n\n");
        
        if (description != null && !description.isEmpty()) {
            desc.append(description).append("\n\n");
        }
        
        desc.append("📊 **基本信息**\n");
        desc.append("👥 成員數量: ").append(memberCount).append("\n");
        desc.append("🏝️ 領土數量: ").append(territoryCount).append("\n");
        desc.append("💰 國庫: ").append(String.format("%.2f", treasury)).append("\n");
        desc.append("📅 建立日期: ").append(foundedDate.toLocalDate()).append("\n\n");
        
        if (warWins + warLosses > 0) {
            desc.append("⚔️ **軍事記錄**\n");
            desc.append("🏆 戰爭勝利: ").append(warWins).append("\n");
            desc.append("💥 戰爭失敗: ").append(warLosses).append("\n");
            desc.append("📈 勝率: ").append(String.format("%.1f%%", winRate)).append("\n\n");
        }
        
        desc.append("⭐ **權力評分**: ").append(String.format("%.0f", powerScore));
        
        return desc.toString();
    }
    
    /**
     * 生成簡化的Discord展示
     */
    public String getDiscordSummary() {
        return String.format("%s **%s** - %s\n👥 %d 成員 | 🏝️ %d 領土 | 💰 %.0f | ⭐ %.0f 分",
            statusEmoji, nationName, levelDisplay, memberCount, territoryCount, treasury, powerScore);
    }
    
    /**
     * 獲取Discord顏色代碼 (十六進制)
     */
    public int getDiscordColor() {
        if (!isActive) {
            return 0x808080; // 灰色
        } else if (powerScore >= 5000) {
            return 0xFFD700; // 金色
        } else if (powerScore >= 2000) {
            return 0x9932CC; // 紫色
        } else if (powerScore >= 1000) {
            return 0x1E90FF; // 藍色
        } else if (powerScore >= 500) {
            return 0x32CD32; // 綠色
        } else {
            return 0xFFA500; // 橙色
        }
    }
    
    /**
     * 生成國家排行榜條目
     */
    public String getRankingEntry(int rank) {
        String medal = "";
        switch (rank) {
            case 1: medal = "🥇"; break;
            case 2: medal = "🥈"; break;
            case 3: medal = "🥉"; break;
            default: medal = String.format("#%d", rank); break;
        }
        
        return String.format("%s **%s** - %.0f 分\n%s | 👥 %d | 🏝️ %d",
            medal, nationName, powerScore, levelDisplay, memberCount, territoryCount);
    }
    
    // === Getter 方法 ===
    
    public UUID getNationId() { return nationId; }
    public String getNationName() { return nationName; }
    public String getDescription() { return description; }
    public String getLevel() { return level; }
    public String getLevelDisplay() { return levelDisplay; }
    public int getMemberCount() { return memberCount; }
    public int getTerritoryCount() { return territoryCount; }
    public double getTreasury() { return treasury; }
    public LocalDateTime getFoundedDate() { return foundedDate; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public int getWarWins() { return warWins; }
    public int getWarLosses() { return warLosses; }
    public double getWinRate() { return winRate; }
    public double getPowerScore() { return powerScore; }
    public boolean isActive() { return isActive; }
    public boolean isDiscordIntegration() { return discordIntegration; }
    public String getStatus() { return status; }
    public String getStatusEmoji() { return statusEmoji; }
}