package com.zientis.core.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zientis.core.service.AbstractService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DiscordSRV風格的服務
 * 支援混合架構：直接Discord API + 經濟API
 * 提供聊天同步、伺服器狀態嵌入訊息等功能
 */
public class DiscordSRVService extends AbstractService implements Listener {
    
    private final DiscordConfig config;
    private final DiscordApiClient economyApiClient;        // 經濟API客戶端
    private final DirectDiscordApiClient directApiClient;   // 直接Discord API客戶端
    private final ObjectMapper objectMapper;
    private ScheduledExecutorService scheduler;
    
    // 伺服器狀態緩存
    private String lastServerStatusMessageId = null;
    private long lastStatusUpdate = 0;
    
    public DiscordSRVService(Plugin plugin, DiscordConfig config, DiscordApiClient economyApiClient) {
        super(plugin, "DiscordSRVService", "1.0.0");
        this.config = config;
        this.economyApiClient = economyApiClient;
        this.objectMapper = new ObjectMapper();
        
        // 初始化直接Discord API客戶端（如果啟用）
        if (config.isBotTokenEnabled()) {
            this.directApiClient = new DirectDiscordApiClient(config);
            logger.info("直接Discord API模式已啟用");
        } else {
            this.directApiClient = null;
            logger.info("直接Discord API模式已停用，僅使用經濟API");
        }
    }
    
    @Override
    protected void onInitialize() throws Exception {
        if (!config.isEnabled()) {
            logger.info("Discord SRV服務已停用");
            return;
        }
        
        // 註冊事件監聽器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // 啟動定期任務
        scheduler = Executors.newScheduledThreadPool(2);
        
        // 伺服器狀態更新任務
        if (config.isServerStatusEmbed()) {
            startServerStatusUpdateTask();
        }
        
        // 頻道話題更新任務
        if (config.isUpdateChannelTopic()) {
            startChannelTopicUpdateTask();
        }
        
        // 測試直接Discord API連接（如果啟用）
        if (directApiClient != null) {
            directApiClient.testConnection().thenAccept(success -> {
                if (success) {
                    logger.info("直接Discord API連接成功");
                } else {
                    logger.warning("直接Discord API連接失敗");
                }
            });
        }
        
        // 發送伺服器啟動訊息
        if (config.isServerStartStopMessages()) {
            sendServerStartMessage();
        }
        
        logger.info("Discord SRV服務已啟動");
    }
    
    @Override
    protected void onShutdown() throws Exception {
        if (scheduler != null) {
            // 發送伺服器關閉訊息
            if (config.isServerStartStopMessages()) {
                sendServerStopMessage().join();
            }
            
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        
        // 關閉直接Discord API客戶端
        if (directApiClient != null) {
            directApiClient.shutdown();
        }
        
        logger.info("Discord SRV服務已關閉");
    }
    
    // =============== 聊天事件處理 ===============
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || !config.isChatSync() || !config.isMinecraftToDiscord()) {
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 過濾髒話
        if (config.isFilterProfanity()) {
            message = filterProfanity(message);
        }
        
        // 創建final副本供lambda使用
        final String finalMessage = message;
        
        // 構建Discord訊息
        CompletableFuture.runAsync(() -> {
            try {
                sendChatToDiscord(player, finalMessage);
            } catch (Exception e) {
                logger.warning("發送聊天訊息到Discord失敗: " + e.getMessage());
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isJoinLeaveMessages()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendJoinMessage(player);
                updateServerStatus(); // 更新在線人數
            } catch (Exception e) {
                logger.warning("發送玩家加入訊息失敗: " + e.getMessage());
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!config.isJoinLeaveMessages()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendLeaveMessage(player);
                updateServerStatus(); // 更新在線人數
            } catch (Exception e) {
                logger.warning("發送玩家離開訊息失敗: " + e.getMessage());
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!config.isDeathMessages()) {
            return;
        }
        
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendDeathMessage(player, deathMessage);
            } catch (Exception e) {
                logger.warning("發送死亡訊息失敗: " + e.getMessage());
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (!config.isAchievementMessages()) {
            return;
        }
        
        Player player = event.getPlayer();
        String achievementKey = event.getAdvancement().getKey().getKey();
        
        // 過濾隱藏成就和配方
        if (achievementKey.startsWith("recipes/") || 
            achievementKey.contains("root") ||
            achievementKey.contains("hidden")) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                sendAchievementMessage(player, achievementKey);
            } catch (Exception e) {
                logger.warning("發送成就訊息失敗: " + e.getMessage());
            }
        });
    }
    
    // =============== Discord訊息發送方法 ===============
    
    private void sendChatToDiscord(Player player, String message) {
        if (config.getChatChannelId().isEmpty() && config.getChatWebhookUrl().isEmpty()) {
            return;
        }
        
        Map<String, Object> discordMessage = new HashMap<>();
        
        if (!config.getChatWebhookUrl().isEmpty()) {
            // 使用 Webhook 發送 (更美觀)
            discordMessage.put("content", formatChatMessage(player, message));
            discordMessage.put("username", player.getDisplayName());
            discordMessage.put("avatar_url", getPlayerAvatarUrl(player));
            
            // 優先使用經濟API（Webhook功能）
            if (economyApiClient != null) {
                economyApiClient.sendWebhookMessage(config.getChatWebhookUrl(), discordMessage);
            }
        } else {
            // 使用一般頻道發送
            discordMessage.put("content", formatChatMessage(player, message));
            
            // 優先使用直接Discord API
            if (directApiClient != null) {
                directApiClient.sendChannelMessage(config.getChatChannelId(), discordMessage);
            } else if (economyApiClient != null) {
                economyApiClient.sendChannelMessage(config.getChatChannelId(), discordMessage);
            }
        }
    }
    
    private void sendJoinMessage(Player player) {
        Map<String, Object> embed = createPlayerJoinEmbed(player);
        sendEmbedToChannel(config.getLogChannelId(), embed);
    }
    
    private void sendLeaveMessage(Player player) {
        Map<String, Object> embed = createPlayerLeaveEmbed(player);
        sendEmbedToChannel(config.getLogChannelId(), embed);
    }
    
    private void sendDeathMessage(Player player, String deathMessage) {
        Map<String, Object> embed = createDeathEmbed(player, deathMessage);
        sendEmbedToChannel(config.getLogChannelId(), embed);
    }
    
    private void sendAchievementMessage(Player player, String achievementKey) {
        Map<String, Object> embed = createAchievementEmbed(player, achievementKey);
        sendEmbedToChannel(config.getLogChannelId(), embed);
    }
    
    private CompletableFuture<Void> sendServerStartMessage() {
        return CompletableFuture.runAsync(() -> {
            Map<String, Object> embed = createServerStartEmbed();
            sendEmbedToChannel(config.getLogChannelId(), embed);
        });
    }
    
    private CompletableFuture<Void> sendServerStopMessage() {
        return CompletableFuture.runAsync(() -> {
            Map<String, Object> embed = createServerStopEmbed();
            sendEmbedToChannel(config.getLogChannelId(), embed);
        });
    }
    
    // =============== 伺服器狀態更新 ===============
    
    private void startServerStatusUpdateTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateServerStatus();
            } catch (Exception e) {
                logger.warning("更新伺服器狀態失敗: " + e.getMessage());
            }
        }, 30, config.getStatusUpdateInterval(), TimeUnit.SECONDS);
    }
    
    private void startChannelTopicUpdateTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateChannelTopic();
            } catch (Exception e) {
                logger.warning("更新頻道話題失敗: " + e.getMessage());
            }
        }, 60, 300, TimeUnit.SECONDS); // 每5分鐘更新一次
    }
    
    private void updateServerStatus() {
        if (!config.isServerStatusEmbed() || config.getStatusChannelId().isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStatusUpdate < config.getStatusUpdateInterval() * 1000) {
            return; // 防止過於頻繁的更新
        }
        
        lastStatusUpdate = currentTime;
        
        Map<String, Object> embed = createServerStatusEmbed();
        
        if (lastServerStatusMessageId != null) {
            // 編輯現有訊息
            Map<String, Object> updateData = Collections.singletonMap("embeds", Collections.singletonList(embed));
            
            if (directApiClient != null) {
                directApiClient.editChannelMessage(config.getStatusChannelId(), lastServerStatusMessageId, updateData);
            } else if (economyApiClient != null) {
                economyApiClient.editChannelMessage(config.getStatusChannelId(), lastServerStatusMessageId, updateData);
            }
        } else {
            // 發送新訊息
            Map<String, Object> message = Collections.singletonMap("embeds", Collections.singletonList(embed));
            
            if (directApiClient != null) {
                directApiClient.sendChannelMessage(config.getStatusChannelId(), message)
                    .thenAccept(response -> {
                        if (response.has("id")) {
                            lastServerStatusMessageId = response.get("id").asText();
                        }
                    });
            } else if (economyApiClient != null) {
                economyApiClient.sendChannelMessage(config.getStatusChannelId(), message)
                    .thenAccept(response -> {
                        if (response.has("id")) {
                            lastServerStatusMessageId = response.get("id").asText();
                        }
                    });
            }
        }
    }
    
    private void updateChannelTopic() {
        if (!config.isUpdateChannelTopic() || config.getChatChannelId().isEmpty()) {
            return;
        }
        
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String topic = String.format("🟢 %s | 在線: %d/%d | 最後更新: %s", 
            config.getServerName(), onlineCount, maxPlayers, 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // 優先使用直接Discord API更新頻道話題
        if (directApiClient != null) {
            Map<String, Object> channelData = Collections.singletonMap("topic", topic);
            directApiClient.updateChannel(config.getChatChannelId(), channelData);
        } else if (economyApiClient != null) {
            economyApiClient.updateChannelTopic(config.getChatChannelId(), topic);
        }
    }
    
    // =============== 嵌入訊息創建方法 ===============
    
    private Map<String, Object> createServerStatusEmbed() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int onlineCount = onlinePlayers.size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🟢 " + config.getServerName());
        embed.put("description", config.getServerDescription());
        embed.put("color", 0x00FF00); // 綠色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // 在線玩家數
        fields.add(createEmbedField("👥 在線玩家", onlineCount + "/" + maxPlayers, true));
        
        // TPS (如果可用)
        try {
            double tps = Bukkit.getServer().getTPS()[0];
            String tpsStatus = tps > 18.0 ? "🟢 優秀" : tps > 15.0 ? "🟡 良好" : "🔴 卡頓";
            fields.add(createEmbedField("⚡ TPS", String.format("%.1f %s", tps, tpsStatus), true));
        } catch (Exception e) {
            fields.add(createEmbedField("⚡ TPS", "無法獲取", true));
        }
        
        // 伺服器版本
        fields.add(createEmbedField("📋 版本", Bukkit.getVersion(), true));
        
        // 玩家列表 (如果啟用且玩家數量不多)
        if (config.isPlayerListSync() && onlineCount > 0 && onlineCount <= 20) {
            StringBuilder playerList = new StringBuilder();
            for (Player player : onlinePlayers) {
                if (playerList.length() > 0) playerList.append(", ");
                playerList.append(player.getDisplayName());
                if (playerList.length() > 800) { // Discord限制
                    playerList.append("...");
                    break;
                }
            }
            fields.add(createEmbedField("👤 在線玩家", playerList.toString(), false));
        }
        
        embed.put("fields", fields);
        
        // 伺服器圖標
        if (!config.getServerIcon().isEmpty()) {
            Map<String, Object> thumbnail = new HashMap<>();
            thumbnail.put("url", config.getServerIcon());
            embed.put("thumbnail", thumbnail);
        }
        
        // 頁腳
        Map<String, Object> footer = new HashMap<>();
        footer.put("text", "最後更新");
        embed.put("footer", footer);
        
        return embed;
    }
    
    private Map<String, Object> createPlayerJoinEmbed(Player player) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "📈 玩家加入");
        embed.put("description", String.format("**%s** 加入了伺服器", player.getDisplayName()));
        embed.put("color", 0x00FF00); // 綠色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, Object> thumbnail = new HashMap<>();
        thumbnail.put("url", getPlayerAvatarUrl(player));
        embed.put("thumbnail", thumbnail);
        
        return embed;
    }
    
    private Map<String, Object> createPlayerLeaveEmbed(Player player) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "📉 玩家離開");
        embed.put("description", String.format("**%s** 離開了伺服器", player.getDisplayName()));
        embed.put("color", 0xFF6B6B); // 紅色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, Object> thumbnail = new HashMap<>();
        thumbnail.put("url", getPlayerAvatarUrl(player));
        embed.put("thumbnail", thumbnail);
        
        return embed;
    }
    
    private Map<String, Object> createDeathEmbed(Player player, String deathMessage) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "💀 玩家死亡");
        embed.put("description", deathMessage != null ? deathMessage : player.getDisplayName() + " 死了");
        embed.put("color", 0x8B4513); // 棕色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        return embed;
    }
    
    private Map<String, Object> createAchievementEmbed(Player player, String achievementKey) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🏆 成就解鎖");
        embed.put("description", String.format("**%s** 解鎖了成就！", player.getDisplayName()));
        embed.put("color", 0xFFD700); // 金色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(createEmbedField("成就", getAchievementDisplayName(achievementKey), false));
        embed.put("fields", fields);
        
        return embed;
    }
    
    private Map<String, Object> createServerStartEmbed() {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🚀 伺服器啟動");
        embed.put("description", config.getServerName() + " 已成功啟動！");
        embed.put("color", 0x00FF00); // 綠色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        return embed;
    }
    
    private Map<String, Object> createServerStopEmbed() {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🛑 伺服器關閉");
        embed.put("description", config.getServerName() + " 正在關閉...");
        embed.put("color", 0xFF0000); // 紅色
        embed.put("timestamp", LocalDateTime.now().toString());
        
        return embed;
    }
    
    // =============== 輔助方法 ===============
    
    private Map<String, Object> createEmbedField(String name, String value, boolean inline) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", inline);
        return field;
    }
    
    private void sendEmbedToChannel(String channelId, Map<String, Object> embed) {
        if (channelId.isEmpty()) return;
        
        Map<String, Object> message = new HashMap<>();
        message.put("embeds", Collections.singletonList(embed));
        
        // 優先使用直接Discord API
        if (directApiClient != null) {
            directApiClient.sendChannelMessage(channelId, message)
                .exceptionally(throwable -> {
                    logger.warning("直接Discord API發送失敗，回退到經濟API: " + throwable.getMessage());
                    // 回退到經濟API
                    if (economyApiClient != null) {
                        economyApiClient.sendChannelMessage(channelId, message);
                    }
                    return null;
                });
        } else if (economyApiClient != null) {
            // 使用經濟API
            economyApiClient.sendChannelMessage(channelId, message);
        }
    }
    
    private String formatChatMessage(Player player, String message) {
        String formatted = config.getMessagePrefix() + "**" + player.getDisplayName() + "**: " + message + config.getMessageSuffix();
        
        // 限制訊息長度
        if (formatted.length() > config.getMaxMessageLength()) {
            formatted = formatted.substring(0, config.getMaxMessageLength() - 3) + "...";
        }
        
        return formatted;
    }
    
    private String filterProfanity(String message) {
        String filtered = message;
        for (String word : config.getBlockedWords()) {
            if (!word.isEmpty()) {
                filtered = filtered.replaceAll("(?i)" + word, "*".repeat(word.length()));
            }
        }
        return filtered;
    }
    
    private String getPlayerAvatarUrl(Player player) {
        return "https://crafatar.com/avatars/" + player.getUniqueId() + "?size=64";
    }
    
    private String getAchievementDisplayName(String achievementKey) {
        // 簡化的成就名稱轉換
        switch (achievementKey) {
            case "story/mine_stone": return "挖到了石頭";
            case "story/upgrade_tools": return "升級工具";
            case "story/smelt_iron": return "冶煉鐵錠";
            case "story/obtain_armor": return "獲得護甲";
            case "story/lava_bucket": return "熔岩桶";
            case "story/iron_tools": return "鐵製工具";
            case "story/deflect_arrow": return "偏轉箭矢";
            case "story/form_obsidian": return "冰火不容";
            case "story/mine_diamond": return "鑽石！";
            case "story/enter_the_nether": return "我們需要再深入一些";
            case "story/shiny_gear": return "用鑽石裝備自己";
            case "story/enchant_item": return "附魔師";
            case "story/cure_zombie_villager": return "殭屍醫生";
            case "story/follow_ender_eye": return "循跡覓蹤";
            case "story/enter_the_end": return "故事的結局...？";
            default: return achievementKey.replaceAll("_", " ").replaceAll("/", " ");
        }
    }
    
    // =============== 公開方法 ===============
    
    /**
     * 從Discord發送訊息到Minecraft
     */
    public void sendDiscordMessageToMinecraft(String username, String message, String channelName) {
        if (!config.isChatSync() || !config.isDiscordToMinecraft()) {
            return;
        }
        
        // 過濾髒話
        if (config.isFilterProfanity()) {
            message = filterProfanity(message);
        }
        
        String formattedMessage = String.format("§9[Discord|%s]§r §b%s§r: %s", channelName, username, message);
        
        // 發送給所有在線玩家
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formattedMessage);
            }
        });
        
        // 記錄到控制台
        logger.info("[Discord] " + username + ": " + message);
    }
    
    /**
     * 手動更新伺服器狀態
     */
    public void forceUpdateServerStatus() {
        updateServerStatus();
    }
    
    /**
     * 發送自定義嵌入訊息
     */
    public void sendCustomEmbed(String channelId, Map<String, Object> embed) {
        sendEmbedToChannel(channelId, embed);
    }
}