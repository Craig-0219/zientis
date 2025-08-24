package com.zientis.core.discord;

import com.zientis.core.service.AbstractService;
// import com.zientis.discord.service.VoiceChannelStatusService; // 語音頻道服務將在 Discord API 模組中實現
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Discord整合服務
 * 管理與Discord Bot的所有互動和資料同步
 * 包含DiscordSRV風格的伺服器同步功能
 */
public class DiscordIntegrationService extends AbstractService {
    
    private DiscordApiClient apiClient;
    private ScheduledExecutorService scheduler;
    private DiscordConfig discordConfig;
    private DiscordSRVService discordSRVService;
    // 語音頻道狀態服務 (將通過 API 整合實現)
    private boolean voiceChannelStatusEnabled = false;
    
    public DiscordIntegrationService(Plugin plugin, DiscordConfig config) {
        super(plugin, "DiscordIntegrationService", "1.0.0");
        this.discordConfig = config;
    }
    
    @Override
    protected void onInitialize() throws Exception {
        if (!discordConfig.isEnabled()) {
            logger.info("Discord整合已停用");
            return;
        }
        
        // 初始化API客戶端
        apiClient = new DiscordApiClient(discordConfig);
        
        // 測試連接
        boolean connected = apiClient.testConnection().join();
        if (!connected) {
            throw new RuntimeException("無法連接到Discord Bot API");
        }
        
        // 啟動定期同步任務
        if (discordConfig.getSyncInterval() > 0) {
            scheduler = Executors.newScheduledThreadPool(2);
            startSyncTasks();
        }
        
        // 初始化DiscordSRV服務
        if (discordConfig.isChatSync() || discordConfig.isServerStatusEmbed() || 
            discordConfig.isJoinLeaveMessages()) {
            discordSRVService = new DiscordSRVService(plugin, discordConfig, apiClient);
            discordSRVService.initialize();
            logger.info("DiscordSRV風格服務已啟動");
        }
        
        // 初始化語音頻道狀態更新服務配置
        Map<String, Object> voiceChannelConfig = discordConfig.getVoiceChannelConfig();
        if (voiceChannelConfig != null && (Boolean) voiceChannelConfig.getOrDefault("enabled", false)) {
            try {
                // 發送語音頻道配置到 Discord API 服務
                sendVoiceChannelConfig(voiceChannelConfig);
                voiceChannelStatusEnabled = true;
                logger.info("語音頻道狀態更新服務配置已發送到 Discord API");
            } catch (Exception e) {
                logger.warning("語音頻道狀態更新服務配置失敗: " + e.getMessage());
            }
        }
        
        logger.info("Discord整合服務初始化完成");
    }
    
    @Override
    protected void onShutdown() throws Exception {
        // 停用語音頻道狀態更新服務
        if (voiceChannelStatusEnabled) {
            try {
                disableVoiceChannelUpdates();
                logger.info("語音頻道狀態更新服務已停用");
            } catch (Exception e) {
                logger.warning("停用語音頻道狀態更新服務時發生錯誤: " + e.getMessage());
            }
        }
        
        // 關閉DiscordSRV服務
        if (discordSRVService != null) {
            try {
                discordSRVService.shutdown();
                logger.info("DiscordSRV風格服務已關閉");
            } catch (Exception e) {
                logger.warning("關閉DiscordSRV服務時發生錯誤: " + e.getMessage());
            }
        }
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (apiClient != null) {
            apiClient.shutdown();
        }
        
        logger.info("Discord整合服務關閉完成");
    }
    
    /**
     * 啟動定期同步任務
     */
    private void startSyncTasks() {
        long interval = discordConfig.getSyncInterval();
        
        // 經濟資料同步任務
        if (discordConfig.isEconomySync()) {
            scheduler.scheduleAtFixedRate(() -> {
                safeExecute("經濟資料同步", this::syncAllEconomyData);
            }, interval, interval, TimeUnit.SECONDS);
        }
        
        // 成就資料同步任務
        if (discordConfig.isAchievementSync()) {
            scheduler.scheduleAtFixedRate(() -> {
                safeExecute("成就資料同步", this::syncAllAchievementData);
            }, interval + 30, interval, TimeUnit.SECONDS);
        }
        
        logger.info("定期同步任務已啟動，間隔: " + interval + "秒");
    }
    
    /**
     * 同步玩家經濟資料到Discord
     */
    public CompletableFuture<Boolean> syncPlayerEconomyData(UUID playerUuid, 
            Map<String, Object> economyData) {
        
        ensureInitialized();
        
        if (!discordConfig.isEnabled() || !discordConfig.isEconomySync()) {
            return CompletableFuture.completedFuture(true);
        }
        
        return apiClient.syncPlayerEconomy(playerUuid.toString(), economyData)
                .exceptionally(throwable -> {
                    logger.warning("玩家經濟資料同步失敗: " + playerUuid + " - " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 同步玩家成就資料到Discord
     */
    public CompletableFuture<Boolean> syncPlayerAchievementData(UUID playerUuid, 
            Map<String, Object> achievementData) {
        
        ensureInitialized();
        
        if (!discordConfig.isEnabled() || !discordConfig.isAchievementSync()) {
            return CompletableFuture.completedFuture(true);
        }
        
        return apiClient.syncPlayerAchievements(playerUuid.toString(), achievementData)
                .exceptionally(throwable -> {
                    logger.warning("玩家成就資料同步失敗: " + playerUuid + " - " + throwable.getMessage());
                    return false;
                });
    }
    
    /**
     * 發送遊戲事件通知到Discord
     */
    public CompletableFuture<Boolean> sendGameEvent(String eventType, Map<String, Object> eventData) {
        ensureInitialized();
        
        if (!discordConfig.isEnabled()) {
            return CompletableFuture.completedFuture(true);
        }
        
        eventData.put("event_type", eventType);
        eventData.put("server_name", plugin.getServer().getName());
        eventData.put("timestamp", System.currentTimeMillis());
        
        return apiClient.post("/minecraft/events", eventData)
                .thenApply(response -> {
                    boolean success = response.has("success") && response.get("success").asBoolean();
                    if (success) {
                        logger.fine("遊戲事件發送成功: " + eventType);
                    } else {
                        logger.warning("遊戲事件發送失敗: " + eventType);
                    }
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.warning("遊戲事件發送失敗: " + eventType + " - " + throwable.getMessage());
                    return false;
                });
    }
    
    /**
     * 獲取Discord用戶資料
     */
    public CompletableFuture<Map<String, Object>> getDiscordUserData(UUID minecraftUuid) {
        ensureInitialized();
        
        return apiClient.get("/cross-platform/users/" + minecraftUuid.toString())
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    if (response.has("data")) {
                        // 這裡需要實際解析JSON到Map，先返回空Map
                        // TODO: 實現完整的JSON到Map轉換
                        result.put("raw_data", response.get("data").asText());
                    }
                    return result;
                })
                .exceptionally(throwable -> {
                    logger.warning("獲取Discord用戶資料失敗: " + minecraftUuid + " - " + throwable.getMessage());
                    return new HashMap<>();
                });
    }
    
    /**
     * 檢查Discord服務狀態
     */
    public CompletableFuture<Boolean> checkDiscordServiceHealth() {
        if (!discordConfig.isEnabled() || apiClient == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return apiClient.testConnection();
    }
    
    /**
     * 同步所有經濟資料
     */
    private void syncAllEconomyData() {
        // 這裡會從經濟系統獲取需要同步的資料
        // 實際實現時會與EconomyManager整合
        logger.fine("執行定期經濟資料同步");
    }
    
    /**
     * 同步所有成就資料
     */
    private void syncAllAchievementData() {
        // 這裡會從成就系統獲取需要同步的資料
        // 實際實現時會與AchievementManager整合
        logger.fine("執行定期成就資料同步");
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(DiscordConfig newConfig) {
        this.discordConfig = newConfig;
        
        if (apiClient != null) {
            apiClient.shutdown();
        }
        
        if (isRunning() && newConfig.isEnabled()) {
            try {
                apiClient = new DiscordApiClient(newConfig);
                logger.info("Discord配置已更新並重新連接");
            } catch (Exception e) {
                logger.severe("Discord配置更新失敗: " + e.getMessage());
            }
        }
    }
    
    /**
     * 獲取當前配置
     */
    public DiscordConfig getConfig() {
        return discordConfig;
    }
    
    /**
     * 獲取DiscordSRV服務實例
     */
    public DiscordSRVService getDiscordSRVService() {
        return discordSRVService;
    }
    
    /**
     * 手動更新指定語音頻道狀態
     */
    public CompletableFuture<Boolean> updateVoiceChannelStatus(String channelType) {
        if (!voiceChannelStatusEnabled || !discordConfig.isEnabled()) {
            return CompletableFuture.completedFuture(false);
        }
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "update_channel");
        requestData.put("channel_type", channelType);
        
        return apiClient.post("/discord/voice-channels/update", requestData)
                .thenApply(response -> {
                    boolean success = response.has("success") && response.get("success").asBoolean();
                    if (success) {
                        logger.fine("語音頻道 " + channelType + " 手動更新成功");
                    } else {
                        logger.warning("語音頻道 " + channelType + " 手動更新失敗");
                    }
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.warning("語音頻道 " + channelType + " 手動更新請求失敗: " + throwable.getMessage());
                    return false;
                });
    }
    
    /**
     * 獲取所有語音頻道狀態
     */
    public CompletableFuture<Map<String, Object>> getVoiceChannelStatuses() {
        if (!voiceChannelStatusEnabled || !discordConfig.isEnabled()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        return apiClient.get("/discord/voice-channels/status")
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    if (response.has("data")) {
                        // TODO: 實現 JSON 到 Map 的轉換
                        result.put("raw_data", response.get("data").asText());
                    }
                    return result;
                })
                .exceptionally(throwable -> {
                    logger.warning("獲取語音頻道狀態失敗: " + throwable.getMessage());
                    return new HashMap<>();
                });
    }
    
    /**
     * 重置語音頻道失敗計數器
     */
    public CompletableFuture<Boolean> resetVoiceChannelFailureCount(String channelType) {
        if (!voiceChannelStatusEnabled || !discordConfig.isEnabled()) {
            return CompletableFuture.completedFuture(false);
        }
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "reset_failures");
        requestData.put("channel_type", channelType);
        
        return apiClient.post("/discord/voice-channels/reset", requestData)
                .thenApply(response -> {
                    boolean success = response.has("success") && response.get("success").asBoolean();
                    if (success) {
                        logger.info("語音頻道 " + channelType + " 失敗計數器已重置");
                    } else {
                        logger.warning("重置語音頻道 " + channelType + " 失敗計數器失敗");
                    }
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.warning("重置語音頻道失敗計數器請求失敗: " + throwable.getMessage());
                    return false;
                });
    }
    
    /**
     * 發送語音頻道配置到 Discord API 服務
     */
    private void sendVoiceChannelConfig(Map<String, Object> config) {
        if (apiClient != null) {
            apiClient.post("/discord/voice-channels/config", config)
                    .thenAccept(response -> {
                        boolean success = response.has("success") && response.get("success").asBoolean();
                        if (success) {
                            logger.info("語音頻道配置同步成功");
                        } else {
                            logger.warning("語音頻道配置同步失敗");
                        }
                    })
                    .exceptionally(throwable -> {
                        logger.warning("發送語音頻道配置失敗: " + throwable.getMessage());
                        return null;
                    });
        }
    }
    
    /**
     * 停用語音頻道更新
     */
    private void disableVoiceChannelUpdates() {
        if (apiClient != null) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "disable");
            
            apiClient.post("/discord/voice-channels/disable", requestData)
                    .thenAccept(response -> {
                        logger.info("語音頻道狀態更新已停用");
                    })
                    .exceptionally(throwable -> {
                        logger.warning("停用語音頻道更新失敗: " + throwable.getMessage());
                        return null;
                    });
        }
        voiceChannelStatusEnabled = false;
    }
    
    /**
     * 從Discord發送訊息到Minecraft
     * 提供給外部系統調用的接口
     */
    public void sendDiscordMessageToMinecraft(String username, String message, String channelName) {
        if (discordSRVService != null) {
            discordSRVService.sendDiscordMessageToMinecraft(username, message, channelName);
        }
    }
    
    /**
     * 手動觸發伺服器狀態更新
     */
    public void updateServerStatus() {
        if (discordSRVService != null) {
            discordSRVService.forceUpdateServerStatus();
        }
    }
    
    /**
     * 發送自定義嵌入訊息到Discord頻道
     */
    public void sendCustomEmbed(String channelId, Map<String, Object> embed) {
        if (discordSRVService != null) {
            discordSRVService.sendCustomEmbed(channelId, embed);
        }
    }
    
    @Override
    public String getStatus() {
        if (!discordConfig.isEnabled()) {
            return "已停用";
        }
        
        if (!isRunning()) {
            return "已停止";
        }
        
        // 檢查API連接狀態
        try {
            boolean healthy = checkDiscordServiceHealth().get(5, TimeUnit.SECONDS);
            return healthy ? "運行中 (已連接)" : "運行中 (連接異常)";
        } catch (Exception e) {
            return "運行中 (狀態未知)";
        }
    }
}