package com.zientis.core.discord.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 遊戲事件資料模型
 * 用於向Discord Bot發送Minecraft遊戲事件
 */
public class GameEvent {
    
    public enum EventType {
        PLAYER_JOIN("player_join", "玩家加入"),
        PLAYER_LEAVE("player_leave", "玩家離開"),
        PLAYER_DEATH("player_death", "玩家死亡"),
        PLAYER_ACHIEVEMENT("player_achievement", "玩家成就"),
        ECONOMY_TRANSACTION("economy_transaction", "經濟交易"),
        ISLAND_CREATE("island_create", "島嶼創建"),
        ISLAND_DELETE("island_delete", "島嶼刪除"),
        NATION_CREATE("nation_create", "國家創建"),
        NATION_WAR("nation_war", "國家戰爭"),
        SERVER_START("server_start", "伺服器啟動"),
        SERVER_STOP("server_stop", "伺服器關閉"),
        CUSTOM_EVENT("custom_event", "自定義事件");
        
        private final String code;
        private final String displayName;
        
        EventType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
    }
    
    private EventType eventType;
    private String playerName;
    private String playerId;
    private Map<String, Object> eventData;
    private LocalDateTime timestamp;
    private String serverName;
    private boolean urgent;
    
    public GameEvent(EventType eventType) {
        this.eventType = eventType;
        this.eventData = new HashMap<>();
        this.timestamp = LocalDateTime.now();
        this.urgent = false;
    }
    
    public GameEvent(EventType eventType, String playerName, String playerId) {
        this(eventType);
        this.playerName = playerName;
        this.playerId = playerId;
    }
    
    /**
     * 添加事件資料
     */
    public GameEvent addData(String key, Object value) {
        eventData.put(key, value);
        return this;
    }
    
    /**
     * 設定為緊急事件（會立即發送到Discord）
     */
    public GameEvent setUrgent(boolean urgent) {
        this.urgent = urgent;
        return this;
    }
    
    /**
     * 設定伺服器名稱
     */
    public GameEvent setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }
    
    /**
     * 轉換為API傳輸格式
     */
    public Map<String, Object> toApiFormat() {
        Map<String, Object> data = new HashMap<>();
        data.put("event_type", eventType.getCode());
        data.put("event_display_name", eventType.getDisplayName());
        data.put("player_name", playerName);
        data.put("player_id", playerId);
        data.put("server_name", serverName);
        data.put("timestamp", timestamp.toString());
        data.put("urgent", urgent);
        data.put("event_data", eventData);
        return data;
    }
    
    /**
     * 建立玩家加入事件
     */
    public static GameEvent playerJoin(String playerName, String playerId) {
        return new GameEvent(EventType.PLAYER_JOIN, playerName, playerId)
                .addData("message", playerName + " 加入了伺服器");
    }
    
    /**
     * 建立玩家離開事件
     */
    public static GameEvent playerLeave(String playerName, String playerId) {
        return new GameEvent(EventType.PLAYER_LEAVE, playerName, playerId)
                .addData("message", playerName + " 離開了伺服器");
    }
    
    /**
     * 建立玩家死亡事件
     */
    public static GameEvent playerDeath(String playerName, String playerId, String cause) {
        return new GameEvent(EventType.PLAYER_DEATH, playerName, playerId)
                .addData("death_cause", cause)
                .addData("message", playerName + " " + cause);
    }
    
    /**
     * 建立玩家成就事件
     */
    public static GameEvent playerAchievement(String playerName, String playerId, 
            String achievementName, String description) {
        return new GameEvent(EventType.PLAYER_ACHIEVEMENT, playerName, playerId)
                .addData("achievement_name", achievementName)
                .addData("achievement_description", description)
                .addData("message", playerName + " 解鎖了成就: " + achievementName)
                .setUrgent(true);
    }
    
    /**
     * 建立經濟交易事件
     */
    public static GameEvent economyTransaction(String playerName, String playerId, 
            String transactionType, int amount, String currency) {
        return new GameEvent(EventType.ECONOMY_TRANSACTION, playerName, playerId)
                .addData("transaction_type", transactionType)
                .addData("amount", amount)
                .addData("currency", currency)
                .addData("message", playerName + " " + transactionType + " " + amount + " " + currency);
    }
    
    /**
     * 建立伺服器啟動事件
     */
    public static GameEvent serverStart(String serverName) {
        return new GameEvent(EventType.SERVER_START)
                .setServerName(serverName)
                .addData("message", "伺服器 " + serverName + " 已啟動")
                .setUrgent(true);
    }
    
    /**
     * 建立伺服器關閉事件
     */
    public static GameEvent serverStop(String serverName) {
        return new GameEvent(EventType.SERVER_STOP)
                .setServerName(serverName)
                .addData("message", "伺服器 " + serverName + " 已關閉")
                .setUrgent(true);
    }
    
    // Getters
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public Map<String, Object> getEventData() {
        return eventData;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public boolean isUrgent() {
        return urgent;
    }
    
    @Override
    public String toString() {
        return String.format("GameEvent{type=%s, player='%s', server='%s', urgent=%s}", 
                eventType.getDisplayName(), playerName, serverName, urgent);
    }
}