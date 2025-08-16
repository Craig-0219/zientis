package com.zientis.display.discord;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Discord Bot API 展示數據傳輸對象
 * 
 * 用於與Discord Bot進行展示系統數據交換
 */
public class DiscordDisplayData {
    
    @JsonProperty("island_id")
    private final UUID islandId;
    
    @JsonProperty("owner_id")
    private final UUID ownerId;
    
    @JsonProperty("owner_name")
    private final String ownerName;
    
    @JsonProperty("display_tier")
    private final String displayTier;
    
    @JsonProperty("tier_display")
    private final String tierDisplay;
    
    @JsonProperty("status")
    private final String status;
    
    @JsonProperty("block_count")
    private final int blockCount;
    
    @JsonProperty("view_count")
    private final int viewCount;
    
    @JsonProperty("click_count")
    private final int clickCount;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    
    @JsonProperty("last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastUpdated;
    
    @JsonProperty("center_location")
    private final String centerLocation;
    
    @JsonProperty("has_hologram")
    private final boolean hasHologram;
    
    @JsonProperty("has_particles")
    private final boolean hasParticles;
    
    @JsonProperty("popularity_score")
    private final double popularityScore;
    
    @JsonProperty("quality_rating")
    private final double qualityRating;
    
    public DiscordDisplayData(UUID islandId, UUID ownerId, String ownerName, 
                            String displayTier, String tierDisplay, String status,
                            int blockCount, int viewCount, int clickCount,
                            LocalDateTime createdAt, LocalDateTime lastUpdated,
                            String centerLocation, boolean hasHologram, boolean hasParticles,
                            double popularityScore, double qualityRating) {
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.displayTier = displayTier;
        this.tierDisplay = tierDisplay;
        this.status = status;
        this.blockCount = blockCount;
        this.viewCount = viewCount;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.centerLocation = centerLocation;
        this.hasHologram = hasHologram;
        this.hasParticles = hasParticles;
        this.popularityScore = popularityScore;
        this.qualityRating = qualityRating;
    }
    
    /**
     * 生成Discord嵌入式消息的標題
     */
    public String getDiscordTitle() {
        return String.format("%s %s的島嶼展示", getStatusEmoji(), ownerName);
    }
    
    /**
     * 生成Discord嵌入式消息的描述
     */
    public String getDiscordDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append("**").append(tierDisplay).append("**\n\n");
        
        desc.append("📊 **展示信息**\n");
        desc.append("👑 島主: ").append(ownerName).append("\n");
        desc.append("🧱 方塊數: ").append(blockCount).append("\n");
        desc.append("📍 位置: ").append(centerLocation).append("\n");
        desc.append("📅 創建: ").append(createdAt.toLocalDate()).append("\n");
        desc.append("🔄 更新: ").append(lastUpdated.toLocalDate()).append("\n\n");
        
        desc.append("👁️ **互動統計**\n");
        desc.append("👀 瀏覽次數: ").append(viewCount).append("\n");
        desc.append("👆 點擊次數: ").append(clickCount).append("\n");
        desc.append("🌟 人氣評分: ").append(String.format("%.1f", popularityScore)).append("\n");
        desc.append("⭐ 品質評分: ").append(String.format("%.1f", qualityRating)).append("\n\n");
        
        desc.append("✨ **特效**\n");
        desc.append("🏷️ 全息標籤: ").append(hasHologram ? "✅ 已啟用" : "❌ 未啟用").append("\n");
        desc.append("💫 粒子效果: ").append(hasParticles ? "✅ 已啟用" : "❌ 未啟用").append("\n");
        
        return desc.toString();
    }
    
    /**
     * 生成簡化的Discord展示
     */
    public String getDiscordSummary() {
        return String.format("%s **%s** - %s\n🧱 %d 方塊 | 👀 %d 瀏覽 | ⭐ %.1f 分",
            getStatusEmoji(), ownerName, tierDisplay, blockCount, viewCount, qualityRating);
    }
    
    /**
     * 獲取狀態表情符號
     */
    public String getStatusEmoji() {
        switch (status.toLowerCase()) {
            case "active": return "✅";
            case "paused": return "⏸️";
            case "updating": return "🔄";
            case "error": return "❌";
            case "creating": return "🛠️";
            default: return "⚪";
        }
    }
    
    /**
     * 獲取等級表情符號
     */
    public String getTierEmoji() {
        switch (displayTier.toLowerCase()) {
            case "basic": return "🥉";
            case "enhanced": return "🥈";
            case "premium": return "🥇";
            case "ultimate": return "💎";
            default: return "⭐";
        }
    }
    
    /**
     * 獲取Discord顏色代碼 (十六進制)
     */
    public int getDiscordColor() {
        switch (status.toLowerCase()) {
            case "active":
                switch (displayTier.toLowerCase()) {
                    case "ultimate": return 0xFF1493; // 深粉色
                    case "premium": return 0xFFD700; // 金色
                    case "enhanced": return 0x9932CC; // 紫色
                    default: return 0x32CD32; // 綠色
                }
            case "paused": return 0xFFA500; // 橙色
            case "updating": return 0x1E90FF; // 藍色
            case "error": return 0xFF4500; // 紅色
            case "creating": return 0x87CEEB; // 天藍色
            default: return 0x808080; // 灰色
        }
    }
    
    /**
     * 生成展示排行榜條目
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
            case "popularity":
                value = String.format("%.1f 人氣", popularityScore);
                break;
            case "quality":
                value = String.format("%.1f 品質", qualityRating);
                break;
            case "views":
                value = viewCount + " 瀏覽";
                break;
            case "clicks":
                value = clickCount + " 點擊";
                break;
            case "blocks":
                value = blockCount + " 方塊";
                break;
            default:
                value = tierDisplay;
                break;
        }
        
        return String.format("%s **%s** - %s\n%s %s | %s %s",
            medal, ownerName, value, getTierEmoji(), tierDisplay, getStatusEmoji(), status);
    }
    
    /**
     * 檢查展示是否需要關注
     */
    public boolean needsAttention() {
        // 錯誤狀態
        if (status.equals("error")) {
            return true;
        }
        
        // 長期未更新 (超過30天)
        if (lastUpdated.isBefore(LocalDateTime.now().minusDays(30))) {
            return true;
        }
        
        // 品質評分過低
        if (qualityRating < 3.0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 獲取注意事項
     */
    public String getAttentionMessage() {
        if (!needsAttention()) {
            return null;
        }
        
        StringBuilder msg = new StringBuilder("⚠️ **需要關注**: ");
        
        if (status.equals("error")) {
            msg.append("展示處於錯誤狀態；");
        }
        
        if (lastUpdated.isBefore(LocalDateTime.now().minusDays(30))) {
            long days = java.time.ChronoUnit.DAYS.between(lastUpdated.toLocalDate(), java.time.LocalDate.now());
            msg.append("已 ").append(days).append(" 天未更新；");
        }
        
        if (qualityRating < 3.0) {
            msg.append("品質評分偏低 (").append(String.format("%.1f", qualityRating)).append("/10)；");
        }
        
        return msg.toString();
    }
    
    /**
     * 計算互動率
     */
    public double getInteractionRate() {
        if (viewCount == 0) return 0.0;
        return (double) clickCount / viewCount * 100;
    }
    
    /**
     * 獲取互動率描述
     */
    public String getInteractionRateDescription() {
        double rate = getInteractionRate();
        if (rate >= 20) {
            return "🔥 極高互動";
        } else if (rate >= 10) {
            return "📈 高互動";
        } else if (rate >= 5) {
            return "📊 中等互動";
        } else if (rate > 0) {
            return "📉 低互動";
        } else {
            return "💤 無互動";
        }
    }
    
    // === Getter 方法 ===
    
    public UUID getIslandId() { return islandId; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getDisplayTier() { return displayTier; }
    public String getTierDisplay() { return tierDisplay; }
    public String getStatus() { return status; }
    public int getBlockCount() { return blockCount; }
    public int getViewCount() { return viewCount; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public String getCenterLocation() { return centerLocation; }
    public boolean isHasHologram() { return hasHologram; }
    public boolean isHasParticles() { return hasParticles; }
    public double getPopularityScore() { return popularityScore; }
    public double getQualityRating() { return qualityRating; }
}