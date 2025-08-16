package com.zientis.display.api;

import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayUpdateType;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 賽恩堤斯島嶼展示系統核心API
 * 
 * 此API提供了創建、管理和更新3D島嶼微縮展示模型的功能
 * 支持1:8比例縮放、即時更新和互動功能
 */
public interface ZientisDisplayAPI {

    /**
     * 創建島嶼展示模型
     * 
     * @param islandId 島嶼UUID
     * @param center 展示中心位置
     * @return 異步返回創建的展示模型
     */
    CompletableFuture<DisplayModel> createIslandDisplay(UUID islandId, Location center);

    /**
     * 更新現有展示模型
     * 
     * @param islandId 島嶼UUID
     * @param updateType 更新類型
     * @return 異步返回更新後的展示模型
     */
    CompletableFuture<DisplayModel> updateDisplayModel(UUID islandId, DisplayUpdateType updateType);

    /**
     * 移除島嶼展示模型
     * 
     * @param islandId 島嶼UUID
     * @return 異步返回移除是否成功
     */
    CompletableFuture<Boolean> removeDisplay(UUID islandId);

    /**
     * 獲取附近的展示模型
     * 
     * @param center 中心位置
     * @param radius 搜索半徑
     * @return 範圍內的展示模型列表
     */
    List<DisplayModel> getNearbyDisplays(Location center, int radius);

    /**
     * 根據島嶼ID獲取展示模型
     * 
     * @param islandId 島嶼UUID
     * @return 展示模型，如果不存在則返回null
     */
    DisplayModel getDisplayModel(UUID islandId);

    /**
     * 獲取所有活躍的展示模型
     * 
     * @return 所有展示模型的列表
     */
    List<DisplayModel> getAllDisplays();

    /**
     * 重新載入指定島嶼的展示模型
     * 
     * @param islandId 島嶼UUID
     * @return 異步返回重載是否成功
     */
    CompletableFuture<Boolean> reloadDisplay(UUID islandId);

    /**
     * 批量更新多個展示模型
     * 
     * @param islandIds 島嶼UUID列表
     * @param updateType 更新類型
     * @return 異步返回更新結果統計
     */
    CompletableFuture<Integer> batchUpdateDisplays(List<UUID> islandIds, DisplayUpdateType updateType);

    /**
     * 設置展示區域的自動更新間隔
     * 
     * @param region 區域名稱
     * @param intervalSeconds 更新間隔（秒）
     */
    void setAutoUpdateInterval(String region, int intervalSeconds);

    /**
     * 獲取展示系統的統計信息
     * 
     * @return 展示系統統計數據
     */
    DisplaySystemStats getSystemStats();

    /**
     * 展示系統統計數據內部類
     */
    class DisplaySystemStats {
        private final int totalDisplays;
        private final int activeDisplays;
        private final long memoryUsage;
        private final double averageUpdateTime;
        
        public DisplaySystemStats(int totalDisplays, int activeDisplays, long memoryUsage, double averageUpdateTime) {
            this.totalDisplays = totalDisplays;
            this.activeDisplays = activeDisplays;
            this.memoryUsage = memoryUsage;
            this.averageUpdateTime = averageUpdateTime;
        }
        
        public int getTotalDisplays() { return totalDisplays; }
        public int getActiveDisplays() { return activeDisplays; }
        public long getMemoryUsage() { return memoryUsage; }
        public double getAverageUpdateTime() { return averageUpdateTime; }
    }
    
    // ============ Discord Bot API ============
    
    /**
     * Get Discord-formatted display data for an island
     * @param islandId Island UUID
     * @return CompletableFuture containing Discord display data
     */
    CompletableFuture<com.zientis.display.discord.DiscordDisplayData> getDiscordDisplayData(UUID islandId);
    
    /**
     * Get Discord-formatted display data by Discord user ID
     * @param discordUserId Discord user ID
     * @return CompletableFuture containing Discord display data
     */
    CompletableFuture<com.zientis.display.discord.DiscordDisplayData> getDiscordDisplayDataByDiscordUser(String discordUserId);
    
    /**
     * Get Discord-formatted display ranking
     * @param criteria Ranking criteria (popularity, quality, views, clicks, blocks)
     * @param limit Number of top displays to return
     * @return CompletableFuture containing list of Discord display data
     */
    CompletableFuture<List<com.zientis.display.discord.DiscordDisplayData>> getDiscordDisplayRanking(String criteria, int limit);
    
    /**
     * Get Discord-formatted list of displays needing attention
     * @return CompletableFuture containing list of displays with issues
     */
    CompletableFuture<List<com.zientis.display.discord.DiscordDisplayData>> getDiscordDisplaysNeedingAttention();
    
    /**
     * Handle Discord command for display system
     * @param command Command name
     * @param args Command arguments
     * @param discordUserId Discord user ID
     * @return CompletableFuture containing command execution result
     */
    CompletableFuture<String> handleDiscordDisplayCommand(String command, String[] args, String discordUserId);
    
    /**
     * Send Discord webhook notification for display events
     * @param eventType Event type (display_created, tier_upgraded, etc.)
     * @param islandId Related island ID
     * @param message Notification message
     * @return CompletableFuture containing success status
     */
    CompletableFuture<Boolean> sendDiscordDisplayNotification(String eventType, UUID islandId, String message);
    
    /**
     * Get Discord-formatted display system statistics
     * @return CompletableFuture containing Discord-formatted display stats
     */
    CompletableFuture<String> getDiscordDisplayStats();
}