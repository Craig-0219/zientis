package com.zientis.multiworld.discord;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Discord Bot API 島嶼數據傳輸對象
 * 
 * 用於與Discord Bot進行多世界系統數據交換
 */
public class DiscordIslandData {
    
    @JsonProperty("island_id")
    private final UUID islandId;
    
    @JsonProperty("island_name")
    private final String islandName;
    
    @JsonProperty("owner_id")
    private final UUID ownerId;
    
    @JsonProperty("owner_name")
    private final String ownerName;
    
    @JsonProperty("tier")
    private final String tier;
    
    @JsonProperty("tier_display")
    private final String tierDisplay;
    
    @JsonProperty("size")
    private final String size;
    
    @JsonProperty("world_name")
    private final String worldName;
    
    @JsonProperty("created_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdDate;
    
    @JsonProperty("last_visit")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastVisit;
    
    @JsonProperty("visitor_count")
    private final int visitorCount;
    
    @JsonProperty("backup_count")
    private final int backupCount;
    
    @JsonProperty("last_backup")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastBackup;
    
    @JsonProperty("storage_size_mb")
    private final double storageSizeMB;
    
    @JsonProperty("status")
    private final String status;
    
    @JsonProperty("is_public")
    private final boolean isPublic;
    
    @JsonProperty("has_pvp")
    private final boolean hasPvp;
    
    @JsonProperty("difficulty")
    private final String difficulty;
    
    @JsonProperty("spawn_protection")
    private final boolean spawnProtection;
    
    public DiscordIslandData(UUID islandId, String islandName, UUID ownerId, String ownerName,
                           String tier, String tierDisplay, String size, String worldName,
                           LocalDateTime createdDate, LocalDateTime lastVisit, int visitorCount,
                           int backupCount, LocalDateTime lastBackup, double storageSizeMB,
                           String status, boolean isPublic, boolean hasPvp, String difficulty,
                           boolean spawnProtection) {
        this.islandId = islandId;
        this.islandName = islandName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.tier = tier;
        this.tierDisplay = tierDisplay;
        this.size = size;
        this.worldName = worldName;
        this.createdDate = createdDate;
        this.lastVisit = lastVisit;
        this.visitorCount = visitorCount;
        this.backupCount = backupCount;
        this.lastBackup = lastBackup;
        this.storageSizeMB = storageSizeMB;
        this.status = status;
        this.isPublic = isPublic;
        this.hasPvp = hasPvp;
        this.difficulty = difficulty;
        this.spawnProtection = spawnProtection;
    }
    
    /**
     * 生成Discord嵌入式消息的標題
     */
    public String getDiscordTitle() {
        return String.format("%s %s", getStatusEmoji(), islandName);
    }
    
    /**
     * 生成Discord嵌入式消息的描述
     */
    public String getDiscordDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append("**").append(tierDisplay).append("**\n\n");
        
        desc.append("📊 **基本信息**\n");
        desc.append("👑 島主: ").append(ownerName).append("\n");
        desc.append("📏 大小: ").append(size).append("\n");
        desc.append("🌍 世界: ").append(worldName).append("\n");
        desc.append("📅 創建: ").append(createdDate.toLocalDate()).append("\n");
        desc.append("👥 訪客數: ").append(visitorCount).append("\n\n");
        
        desc.append("⚙️ **設置**\n");
        desc.append("🌐 公開: ").append(isPublic ? "是" : "否").append("\n");
        desc.append("⚔️ PvP: ").append(hasPvp ? "開啟" : "關閉").append("\n");
        desc.append("😱 難度: ").append(getDifficultyEmoji()).append(" ").append(difficulty).append("\n");
        desc.append("🛡️ 出生點保護: ").append(spawnProtection ? "開啟" : "關閉").append("\n\n");
        
        desc.append("💾 **備份信息**\n");
        desc.append("📦 備份數量: ").append(backupCount).append("\n");
        desc.append("💿 儲存大小: ").append(String.format("%.2f MB", storageSizeMB)).append("\n");
        
        if (lastBackup != null) {
            desc.append("🕒 最後備份: ").append(lastBackup.toLocalDate()).append("\n");
        }
        
        if (lastVisit != null) {
            desc.append("👁️ 最後訪問: ").append(lastVisit.toLocalDate());
        }
        
        return desc.toString();
    }
    
    /**
     * 生成簡化的Discord展示
     */
    public String getDiscordSummary() {
        return String.format("%s **%s** - %s\n👑 %s | 📏 %s | 👥 %d 訪客",
            getStatusEmoji(), islandName, tierDisplay, ownerName, size, visitorCount);
    }
    
    /**
     * 獲取狀態表情符號
     */
    public String getStatusEmoji() {
        switch (status.toLowerCase()) {
            case "active": return "✅";
            case "inactive": return "😴";
            case "maintenance": return "🔧";
            case "suspended": return "⛔";
            case "archived": return "📦";
            default: return "⚪";
        }
    }
    
    /**
     * 獲取難度表情符號
     */
    public String getDifficultyEmoji() {
        switch (difficulty.toLowerCase()) {
            case "peaceful": return "😊";
            case "easy": return "🙂";
            case "normal": return "😐";
            case "hard": return "😰";
            default: return "❓";
        }
    }
    
    /**
     * 獲取等級表情符號
     */
    public String getTierEmoji() {
        switch (tier.toLowerCase()) {
            case "basic": return "🥉";
            case "premium": return "🥈";
            case "ultimate": return "🥇";
            case "vip": return "👑";
            default: return "⭐";
        }
    }
    
    /**
     * 獲取Discord顏色代碼 (十六進制)
     */
    public int getDiscordColor() {
        switch (status.toLowerCase()) {
            case "active": 
                switch (tier.toLowerCase()) {
                    case "vip": return 0xFFD700; // 金色
                    case "ultimate": return 0x9932CC; // 紫色
                    case "premium": return 0x1E90FF; // 藍色
                    default: return 0x32CD32; // 綠色
                }
            case "inactive": return 0x808080; // 灰色
            case "maintenance": return 0xFFA500; // 橙色
            case "suspended": return 0xFF4500; // 紅色
            case "archived": return 0x696969; // 暗灰色
            default: return 0xFFFFFF; // 白色
        }
    }
    
    /**
     * 生成島嶼排行榜條目
     */
    public String getRankingEntry(int rank, String criteria) {
        String medal = "";
        switch (rank) {
            case 1: medal = "🥇"; break;
            case 2: medal = "🥈"; break;
            case 3: medal = "🥉"; break;
            default: medal = String.format("#%d", rank); break;
        }
        
        String value = "";
        switch (criteria.toLowerCase()) {
            case "visitors":
                value = visitorCount + " 訪客";
                break;
            case "size":
                value = String.format("%.1f MB", storageSizeMB);
                break;
            case "age":
                long days = ChronoUnit.DAYS.between(createdDate.toLocalDate(), java.time.LocalDate.now());
                value = days + " 天";
                break;
            default:
                value = tierDisplay;
                break;
        }
        
        return String.format("%s **%s** - %s\n%s 👑 %s | %s %s",
            medal, islandName, value, getTierEmoji(), ownerName, getStatusEmoji(), status);
    }
    
    /**
     * 檢查島嶼是否需要關注 (警告狀態)
     */
    public boolean needsAttention() {
        // 超過30天未訪問
        if (lastVisit != null && lastVisit.isBefore(LocalDateTime.now().minusDays(30))) {
            return true;
        }
        
        // 儲存空間過大 (超過500MB)
        if (storageSizeMB > 500) {
            return true;
        }
        
        // 狀態異常
        return status.equals("suspended") || status.equals("maintenance");
    }
    
    /**
     * 獲取注意事項
     */
    public String getAttentionMessage() {
        if (!needsAttention()) {
            return null;
        }
        
        StringBuilder msg = new StringBuilder("⚠️ **需要關注**: ");
        
        if (lastVisit != null && lastVisit.isBefore(LocalDateTime.now().minusDays(30))) {
            long days = ChronoUnit.DAYS.between(lastVisit.toLocalDate(), java.time.LocalDate.now());
            msg.append("已 ").append(days).append(" 天未訪問；");
        }
        
        if (storageSizeMB > 500) {
            msg.append("儲存空間過大 (").append(String.format("%.1f MB", storageSizeMB)).append(")；");
        }
        
        if (status.equals("suspended")) {
            msg.append("島嶼已被暫停；");
        } else if (status.equals("maintenance")) {
            msg.append("島嶼正在維護中；");
        }
        
        return msg.toString();
    }
    
    // === Getter 方法 ===
    
    public UUID getIslandId() { return islandId; }
    public String getIslandName() { return islandName; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getTier() { return tier; }
    public String getTierDisplay() { return tierDisplay; }
    public String getSize() { return size; }
    public String getWorldName() { return worldName; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastVisit() { return lastVisit; }
    public int getVisitorCount() { return visitorCount; }
    public int getBackupCount() { return backupCount; }
    public LocalDateTime getLastBackup() { return lastBackup; }
    public double getStorageSizeMB() { return storageSizeMB; }
    public String getStatus() { return status; }
    public boolean isPublic() { return isPublic; }
    public boolean hasPvp() { return hasPvp; }
    public String getDifficulty() { return difficulty; }
    public boolean isSpawnProtection() { return spawnProtection; }
}