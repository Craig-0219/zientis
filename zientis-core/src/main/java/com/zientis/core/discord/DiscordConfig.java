package com.zientis.core.discord;

/**
 * Discord整合配置類
 * 包含Discord Bot API連接的所有配置參數
 */
public class DiscordConfig {
    
    private String botApiEndpoint = "http://localhost:8080/api/v1";
    private String apiKey = "";
    private String webhookUrl = "";
    private int connectionTimeout = 30000; // 30秒
    private int readTimeout = 60000; // 60秒
    private int maxRetries = 3;
    private long retryDelay = 1000; // 1秒
    private boolean enabled = false;
    
    // 跨平台經濟設定
    private boolean economySync = true;
    private boolean achievementSync = true;
    private boolean playerDataSync = true;
    private int syncInterval = 300; // 5分鐘
    
    // 安全設定
    private String serverKey = "";
    private boolean enableEncryption = true;
    private String encryptionAlgorithm = "AES";
    
    public DiscordConfig() {}
    
    public DiscordConfig(String botApiEndpoint, String apiKey) {
        this.botApiEndpoint = botApiEndpoint;
        this.apiKey = apiKey;
    }
    
    // Getters and Setters
    public String getBotApiEndpoint() {
        return botApiEndpoint;
    }
    
    public DiscordConfig setBotApiEndpoint(String botApiEndpoint) {
        this.botApiEndpoint = botApiEndpoint;
        return this;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public DiscordConfig setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public DiscordConfig setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        return this;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public DiscordConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public DiscordConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public DiscordConfig setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public DiscordConfig setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public DiscordConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public boolean isEconomySync() {
        return economySync;
    }
    
    public DiscordConfig setEconomySync(boolean economySync) {
        this.economySync = economySync;
        return this;
    }
    
    public boolean isAchievementSync() {
        return achievementSync;
    }
    
    public DiscordConfig setAchievementSync(boolean achievementSync) {
        this.achievementSync = achievementSync;
        return this;
    }
    
    public boolean isPlayerDataSync() {
        return playerDataSync;
    }
    
    public DiscordConfig setPlayerDataSync(boolean playerDataSync) {
        this.playerDataSync = playerDataSync;
        return this;
    }
    
    public int getSyncInterval() {
        return syncInterval;
    }
    
    public DiscordConfig setSyncInterval(int syncInterval) {
        this.syncInterval = syncInterval;
        return this;
    }
    
    public String getServerKey() {
        return serverKey;
    }
    
    public DiscordConfig setServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }
    
    public boolean isEnableEncryption() {
        return enableEncryption;
    }
    
    public DiscordConfig setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
        return this;
    }
    
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public DiscordConfig setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("DiscordConfig{endpoint='%s', enabled=%s, economySync=%s}", 
            botApiEndpoint, enabled, economySync);
    }
}