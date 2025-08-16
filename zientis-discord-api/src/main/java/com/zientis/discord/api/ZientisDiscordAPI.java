package com.zientis.discord.api;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 賽恩堤斯 Discord API 統一接口
 * 
 * 提供所有系統與Discord Bot整合的統一入口點
 */
public interface ZientisDiscordAPI {
    
    // === 認證與授權 ===
    
    /**
     * 綁定Discord帳號與Minecraft帳號
     * 
     * @param discordUserId Discord用戶ID
     * @param minecraftUUID Minecraft UUID
     * @param verificationCode 驗證碼
     * @return 異步返回綁定是否成功
     */
    CompletableFuture<Boolean> linkAccount(String discordUserId, UUID minecraftUUID, String verificationCode);
    
    /**
     * 解除帳號綁定
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回解除綁定是否成功
     */
    CompletableFuture<Boolean> unlinkAccount(String discordUserId);
    
    /**
     * 獲取Discord用戶對應的Minecraft UUID
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回Minecraft UUID
     */
    CompletableFuture<UUID> getLinkedMinecraftAccount(String discordUserId);
    
    /**
     * 獲取Minecraft用戶對應的Discord ID
     * 
     * @param minecraftUUID Minecraft UUID
     * @return 異步返回Discord用戶ID
     */
    CompletableFuture<String> getLinkedDiscordAccount(UUID minecraftUUID);
    
    /**
     * 生成帳號綁定驗證碼
     * 
     * @param minecraftUUID Minecraft UUID
     * @return 異步返回驗證碼
     */
    CompletableFuture<String> generateVerificationCode(UUID minecraftUUID);
    
    // === 經濟系統整合 ===
    
    /**
     * 處理Discord經濟指令
     * 
     * @param command 指令名稱
     * @param args 指令參數
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleEconomyCommand(String command, String[] args, String discordUserId);
    
    /**
     * 獲取玩家經濟數據 (Discord格式)
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回經濟數據
     */
    CompletableFuture<String> getPlayerEconomyData(String discordUserId);
    
    /**
     * 獲取財富排行榜 (Discord格式)
     * 
     * @param limit 排行榜條目數量
     * @return 異步返回排行榜數據
     */
    CompletableFuture<String> getWealthRanking(int limit);
    
    /**
     * 發送經濟系統通知
     * 
     * @param eventType 事件類型
     * @param playerId 相關玩家ID
     * @param amount 金額
     * @param message 消息內容
     * @return 異步返回發送是否成功
     */
    CompletableFuture<Boolean> sendEconomyNotification(String eventType, UUID playerId, double amount, String message);
    
    // === 多世界系統整合 ===
    
    /**
     * 處理Discord多世界指令
     * 
     * @param command 指令名稱
     * @param args 指令參數
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleMultiWorldCommand(String command, String[] args, String discordUserId);
    
    /**
     * 獲取玩家島嶼數據 (Discord格式)
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回島嶼數據
     */
    CompletableFuture<String> getPlayerIslandData(String discordUserId);
    
    /**
     * 獲取島嶼排行榜 (Discord格式)
     * 
     * @param criteria 排行標準
     * @param limit 排行榜條目數量
     * @return 異步返回排行榜數據
     */
    CompletableFuture<String> getIslandRanking(String criteria, int limit);
    
    /**
     * 觸發島嶼備份
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回備份結果
     */
    CompletableFuture<String> triggerIslandBackup(String discordUserId);
    
    /**
     * 發送多世界系統通知
     * 
     * @param eventType 事件類型
     * @param islandId 相關島嶼ID
     * @param message 消息內容
     * @return 異步返回發送是否成功
     */
    CompletableFuture<Boolean> sendMultiWorldNotification(String eventType, UUID islandId, String message);
    
    // === 展示系統整合 ===
    
    /**
     * 處理Discord展示指令
     * 
     * @param command 指令名稱
     * @param args 指令參數
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleDisplayCommand(String command, String[] args, String discordUserId);
    
    /**
     * 獲取島嶼展示數據 (Discord格式)
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回展示數據
     */
    CompletableFuture<String> getIslandDisplayData(String discordUserId);
    
    /**
     * 獲取展示排行榜 (Discord格式)
     * 
     * @param criteria 排行標準
     * @param limit 排行榜條目數量
     * @return 異步返回排行榜數據
     */
    CompletableFuture<String> getDisplayRanking(String criteria, int limit);
    
    /**
     * 發送展示系統通知
     * 
     * @param eventType 事件類型
     * @param islandId 相關島嶼ID
     * @param message 消息內容
     * @return 異步返回發送是否成功
     */
    CompletableFuture<Boolean> sendDisplayNotification(String eventType, UUID islandId, String message);
    
    // === 國家系統整合 ===
    
    /**
     * 處理Discord國家指令
     * 
     * @param command 指令名稱
     * @param args 指令參數
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleNationCommand(String command, String[] args, String discordUserId);
    
    /**
     * 獲取玩家國家數據 (Discord格式)
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回國家數據
     */
    CompletableFuture<String> getPlayerNationData(String discordUserId);
    
    /**
     * 獲取國家排行榜 (Discord格式)
     * 
     * @param criteria 排行標準
     * @param limit 排行榜條目數量
     * @return 異步返回排行榜數據
     */
    CompletableFuture<String> getNationRanking(String criteria, int limit);
    
    /**
     * 發送國家系統通知
     * 
     * @param eventType 事件類型
     * @param nationId 相關國家ID
     * @param message 消息內容
     * @return 異步返回發送是否成功
     */
    CompletableFuture<Boolean> sendNationNotification(String eventType, UUID nationId, String message);
    
    // === 統一指令處理 ===
    
    /**
     * 處理Discord斜線指令
     * 
     * @param commandName 指令名稱
     * @param options 指令選項
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleSlashCommand(String commandName, 
                                                             java.util.Map<String, String> options, 
                                                             String discordUserId);
    
    /**
     * 處理Discord文字指令
     * 
     * @param message 完整消息內容
     * @param discordUserId Discord用戶ID
     * @param channelId 頻道ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<DiscordCommandResult> handleTextCommand(String message, String discordUserId, String channelId);
    
    // === 系統統計與狀態 ===
    
    /**
     * 獲取服務器總體統計 (Discord格式)
     * 
     * @return 異步返回統計數據
     */
    CompletableFuture<String> getServerStats();
    
    /**
     * 獲取在線玩家列表 (Discord格式)
     * 
     * @return 異步返回在線玩家數據
     */
    CompletableFuture<String> getOnlinePlayersData();
    
    /**
     * 獲取系統健康狀態 (Discord格式)
     * 
     * @return 異步返回健康狀態數據
     */
    CompletableFuture<String> getSystemHealthStatus();
    
    // === Webhook管理 ===
    
    /**
     * 註冊Webhook URL
     * 
     * @param eventType 事件類型
     * @param webhookUrl Webhook URL
     * @return 異步返回註冊是否成功
     */
    CompletableFuture<Boolean> registerWebhook(String eventType, String webhookUrl);
    
    /**
     * 移除Webhook
     * 
     * @param eventType 事件類型
     * @return 異步返回移除是否成功
     */
    CompletableFuture<Boolean> removeWebhook(String eventType);
    
    /**
     * 發送自定義Webhook通知
     * 
     * @param webhookUrl Webhook URL
     * @param title 標題
     * @param description 描述
     * @param color 顏色代碼
     * @return 異步返回發送是否成功
     */
    CompletableFuture<Boolean> sendCustomWebhook(String webhookUrl, String title, String description, int color);
    
    // === 權限與安全 ===
    
    /**
     * 檢查Discord用戶權限
     * 
     * @param discordUserId Discord用戶ID
     * @param permission 權限名稱
     * @return 異步返回是否有權限
     */
    CompletableFuture<Boolean> hasPermission(String discordUserId, String permission);
    
    /**
     * 設置Discord用戶權限
     * 
     * @param discordUserId Discord用戶ID
     * @param permission 權限名稱
     * @param grant 是否授予權限
     * @return 異步返回設置是否成功
     */
    CompletableFuture<Boolean> setPermission(String discordUserId, String permission, boolean grant);
    
    /**
     * 檢查API速率限制
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回是否在限制範圍內
     */
    CompletableFuture<Boolean> checkRateLimit(String discordUserId);
    
    // === 數據類定義 ===
    
    /**
     * Discord指令執行結果
     */
    class DiscordCommandResult {
        private final boolean success;
        private final String message;
        private final String embedTitle;
        private final String embedDescription;
        private final int embedColor;
        private final List<EmbedField> embedFields;
        
        public DiscordCommandResult(boolean success, String message) {
            this(success, message, null, null, 0, null);
        }
        
        public DiscordCommandResult(boolean success, String message, String embedTitle, 
                                  String embedDescription, int embedColor, List<EmbedField> embedFields) {
            this.success = success;
            this.message = message;
            this.embedTitle = embedTitle;
            this.embedDescription = embedDescription;
            this.embedColor = embedColor;
            this.embedFields = embedFields;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getEmbedTitle() { return embedTitle; }
        public String getEmbedDescription() { return embedDescription; }
        public int getEmbedColor() { return embedColor; }
        public List<EmbedField> getEmbedFields() { return embedFields; }
        
        public boolean hasEmbed() {
            return embedTitle != null || embedDescription != null;
        }
    }
    
    /**
     * Discord嵌入字段
     */
    class EmbedField {
        private final String name;
        private final String value;
        private final boolean inline;
        
        public EmbedField(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
        
        public String getName() { return name; }
        public String getValue() { return value; }
        public boolean isInline() { return inline; }
    }
}