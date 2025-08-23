package com.zientis.core.discord;

/**
 * Discord整合配置類
 * 支援混合架構：Bot Token直接串接 + 經濟API串接
 */
public class DiscordConfig {
    
    // ===== 連接模式設定 =====
    private boolean enabled = false;
    private ConnectionMode connectionMode = ConnectionMode.HYBRID; // 預設混合模式
    
    // ===== Bot Token直接串接設定 =====
    private String botToken = "";                                    // Discord Bot Token
    private String guildId = "";                                     // Discord伺服器ID
    private boolean useDirectApi = true;                             // 啟用直接API
    
    // ===== 經濟API串接設定 =====
    private String botApiEndpoint = "http://localhost:8080/api/v1";  // 經濟API端點
    private String apiKey = "";                                      // API認證金鑰
    private boolean useEconomyApi = true;                            // 啟用經濟API
    
    // ===== 通用設定 =====
    private String webhookUrl = "";
    private int connectionTimeout = 30000; // 30秒
    private int readTimeout = 60000; // 60秒
    private int maxRetries = 3;
    private long retryDelay = 1000; // 1秒
    
    // 跨平台經濟設定
    private boolean economySync = true;
    private boolean achievementSync = true;
    private boolean playerDataSync = true;
    private int syncInterval = 300; // 5分鐘
    
    // 安全設定
    private String serverKey = "";
    private boolean enableEncryption = true;
    private String encryptionAlgorithm = "AES";
    
    // ===== DiscordSRV風格功能設定 =====
    
    // 聊天同步
    private boolean chatSync = true;
    private boolean discordToMinecraft = true;
    private boolean minecraftToDiscord = true;
    
    // 伺服器狀態功能
    private boolean serverStatusEmbed = true;
    private boolean playerListSync = true;
    private boolean updateChannelTopic = true;
    private int statusUpdateInterval = 60; // 秒
    
    // 事件訊息
    private boolean joinLeaveMessages = true;
    private boolean deathMessages = true;
    private boolean achievementMessages = true;
    private boolean serverStartStopMessages = true;
    private boolean consoleRelay = false;
    
    // Discord頻道ID設定
    private String chatChannelId = "";
    private String statusChannelId = "";
    private String logChannelId = "";
    private String consoleChannelId = "";
    
    // 伺服器資訊設定
    private String serverName = "Zientis伺服器";
    private String serverDescription = "一個很棒的Minecraft伺服器";
    private String serverIcon = "";
    private String serverAddress = "";
    
    // Webhook設定
    private String chatWebhookUrl = "";
    private String statusWebhookUrl = "";
    private String logWebhookUrl = "";
    
    // 訊息格式設定
    private int maxMessageLength = 2000;
    private boolean allowMentions = false;
    private String messagePrefix = "";
    private String messageSuffix = "";
    
    // 權限設定
    private boolean allowDiscordCommands = true;
    private String adminRoleId = "";
    private String moderatorRoleId = "";
    
    // 過濾設定
    private boolean filterProfanity = false;
    private String[] blockedWords = new String[0];
    private boolean ignoreBots = true;
    
    public DiscordConfig() {}
    
    public DiscordConfig(String botToken, String guildId) {
        this.botToken = botToken;
        this.guildId = guildId;
        this.connectionMode = ConnectionMode.BOT_TOKEN_ONLY;
    }
    
    public DiscordConfig(String botApiEndpoint, String apiKey, boolean isEconomyApi) {
        if (isEconomyApi) {
            this.botApiEndpoint = botApiEndpoint;
            this.apiKey = apiKey;
            this.connectionMode = ConnectionMode.ECONOMY_API_ONLY;
        } else {
            // 舊版相容性，視為Bot Token
            this.botToken = botApiEndpoint;
            this.guildId = apiKey;
            this.connectionMode = ConnectionMode.BOT_TOKEN_ONLY;
        }
    }
    
    public DiscordConfig(String botToken, String guildId, String botApiEndpoint, String apiKey) {
        this.botToken = botToken;
        this.guildId = guildId;
        this.botApiEndpoint = botApiEndpoint;
        this.apiKey = apiKey;
        this.connectionMode = ConnectionMode.HYBRID;
    }
    
    /**
     * 連接模式枚舉
     */
    public enum ConnectionMode {
        BOT_TOKEN_ONLY,    // 僅使用Bot Token直接串接
        ECONOMY_API_ONLY,  // 僅使用經濟API串接
        HYBRID             // 混合模式（建議）
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
    
    // ===== DiscordSRV風格功能的 Getters 和 Setters =====
    
    public boolean isChatSync() { return chatSync; }
    public DiscordConfig setChatSync(boolean chatSync) { this.chatSync = chatSync; return this; }
    
    public boolean isDiscordToMinecraft() { return discordToMinecraft; }
    public DiscordConfig setDiscordToMinecraft(boolean discordToMinecraft) { this.discordToMinecraft = discordToMinecraft; return this; }
    
    public boolean isMinecraftToDiscord() { return minecraftToDiscord; }
    public DiscordConfig setMinecraftToDiscord(boolean minecraftToDiscord) { this.minecraftToDiscord = minecraftToDiscord; return this; }
    
    public boolean isServerStatusEmbed() { return serverStatusEmbed; }
    public DiscordConfig setServerStatusEmbed(boolean serverStatusEmbed) { this.serverStatusEmbed = serverStatusEmbed; return this; }
    
    public boolean isPlayerListSync() { return playerListSync; }
    public DiscordConfig setPlayerListSync(boolean playerListSync) { this.playerListSync = playerListSync; return this; }
    
    public boolean isUpdateChannelTopic() { return updateChannelTopic; }
    public DiscordConfig setUpdateChannelTopic(boolean updateChannelTopic) { this.updateChannelTopic = updateChannelTopic; return this; }
    
    public int getStatusUpdateInterval() { return statusUpdateInterval; }
    public DiscordConfig setStatusUpdateInterval(int statusUpdateInterval) { this.statusUpdateInterval = statusUpdateInterval; return this; }
    
    public boolean isJoinLeaveMessages() { return joinLeaveMessages; }
    public DiscordConfig setJoinLeaveMessages(boolean joinLeaveMessages) { this.joinLeaveMessages = joinLeaveMessages; return this; }
    
    public boolean isDeathMessages() { return deathMessages; }
    public DiscordConfig setDeathMessages(boolean deathMessages) { this.deathMessages = deathMessages; return this; }
    
    public boolean isAchievementMessages() { return achievementMessages; }
    public DiscordConfig setAchievementMessages(boolean achievementMessages) { this.achievementMessages = achievementMessages; return this; }
    
    public boolean isServerStartStopMessages() { return serverStartStopMessages; }
    public DiscordConfig setServerStartStopMessages(boolean serverStartStopMessages) { this.serverStartStopMessages = serverStartStopMessages; return this; }
    
    public boolean isConsoleRelay() { return consoleRelay; }
    public DiscordConfig setConsoleRelay(boolean consoleRelay) { this.consoleRelay = consoleRelay; return this; }
    
    public String getChatChannelId() { return chatChannelId; }
    public DiscordConfig setChatChannelId(String chatChannelId) { this.chatChannelId = chatChannelId; return this; }
    
    public String getStatusChannelId() { return statusChannelId; }
    public DiscordConfig setStatusChannelId(String statusChannelId) { this.statusChannelId = statusChannelId; return this; }
    
    public String getLogChannelId() { return logChannelId; }
    public DiscordConfig setLogChannelId(String logChannelId) { this.logChannelId = logChannelId; return this; }
    
    public String getConsoleChannelId() { return consoleChannelId; }
    public DiscordConfig setConsoleChannelId(String consoleChannelId) { this.consoleChannelId = consoleChannelId; return this; }
    
    public String getServerName() { return serverName; }
    public DiscordConfig setServerName(String serverName) { this.serverName = serverName; return this; }
    
    public String getServerDescription() { return serverDescription; }
    public DiscordConfig setServerDescription(String serverDescription) { this.serverDescription = serverDescription; return this; }
    
    public String getServerIcon() { return serverIcon; }
    public DiscordConfig setServerIcon(String serverIcon) { this.serverIcon = serverIcon; return this; }
    
    public String getServerAddress() { return serverAddress; }
    public DiscordConfig setServerAddress(String serverAddress) { this.serverAddress = serverAddress; return this; }
    
    public String getChatWebhookUrl() { return chatWebhookUrl; }
    public DiscordConfig setChatWebhookUrl(String chatWebhookUrl) { this.chatWebhookUrl = chatWebhookUrl; return this; }
    
    public String getStatusWebhookUrl() { return statusWebhookUrl; }
    public DiscordConfig setStatusWebhookUrl(String statusWebhookUrl) { this.statusWebhookUrl = statusWebhookUrl; return this; }
    
    public String getLogWebhookUrl() { return logWebhookUrl; }
    public DiscordConfig setLogWebhookUrl(String logWebhookUrl) { this.logWebhookUrl = logWebhookUrl; return this; }
    
    public int getMaxMessageLength() { return maxMessageLength; }
    public DiscordConfig setMaxMessageLength(int maxMessageLength) { this.maxMessageLength = maxMessageLength; return this; }
    
    public boolean isAllowMentions() { return allowMentions; }
    public DiscordConfig setAllowMentions(boolean allowMentions) { this.allowMentions = allowMentions; return this; }
    
    public String getMessagePrefix() { return messagePrefix; }
    public DiscordConfig setMessagePrefix(String messagePrefix) { this.messagePrefix = messagePrefix; return this; }
    
    public String getMessageSuffix() { return messageSuffix; }
    public DiscordConfig setMessageSuffix(String messageSuffix) { this.messageSuffix = messageSuffix; return this; }
    
    public boolean isAllowDiscordCommands() { return allowDiscordCommands; }
    public DiscordConfig setAllowDiscordCommands(boolean allowDiscordCommands) { this.allowDiscordCommands = allowDiscordCommands; return this; }
    
    public String getAdminRoleId() { return adminRoleId; }
    public DiscordConfig setAdminRoleId(String adminRoleId) { this.adminRoleId = adminRoleId; return this; }
    
    public String getModeratorRoleId() { return moderatorRoleId; }
    public DiscordConfig setModeratorRoleId(String moderatorRoleId) { this.moderatorRoleId = moderatorRoleId; return this; }
    
    public boolean isFilterProfanity() { return filterProfanity; }
    public DiscordConfig setFilterProfanity(boolean filterProfanity) { this.filterProfanity = filterProfanity; return this; }
    
    public String[] getBlockedWords() { return blockedWords; }
    public DiscordConfig setBlockedWords(String[] blockedWords) { this.blockedWords = blockedWords; return this; }
    
    public boolean isIgnoreBots() { return ignoreBots; }
    public DiscordConfig setIgnoreBots(boolean ignoreBots) { this.ignoreBots = ignoreBots; return this; }
    
    // ===== 新增連接模式相關 Getters 和 Setters =====
    
    public ConnectionMode getConnectionMode() { return connectionMode; }
    public DiscordConfig setConnectionMode(ConnectionMode connectionMode) { this.connectionMode = connectionMode; return this; }
    
    public String getBotToken() { return botToken; }
    public DiscordConfig setBotToken(String botToken) { this.botToken = botToken; return this; }
    
    public String getGuildId() { return guildId; }
    public DiscordConfig setGuildId(String guildId) { this.guildId = guildId; return this; }
    
    public boolean isUseDirectApi() { return useDirectApi; }
    public DiscordConfig setUseDirectApi(boolean useDirectApi) { this.useDirectApi = useDirectApi; return this; }
    
    public boolean isUseEconomyApi() { return useEconomyApi; }
    public DiscordConfig setUseEconomyApi(boolean useEconomyApi) { this.useEconomyApi = useEconomyApi; return this; }
    
    /**
     * 檢查是否啟用Bot Token模式
     */
    public boolean isBotTokenEnabled() {
        return enabled && (connectionMode == ConnectionMode.BOT_TOKEN_ONLY || connectionMode == ConnectionMode.HYBRID) 
               && !botToken.isEmpty() && !guildId.isEmpty();
    }
    
    /**
     * 檢查是否啟用經濟API模式
     */
    public boolean isEconomyApiEnabled() {
        return enabled && (connectionMode == ConnectionMode.ECONOMY_API_ONLY || connectionMode == ConnectionMode.HYBRID)
               && !botApiEndpoint.isEmpty() && !apiKey.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("DiscordConfig{mode=%s, botToken=%s, endpoint='%s', enabled=%s, economySync=%s, chatSync=%s}", 
            connectionMode, botToken.isEmpty() ? "not_set" : "***", botApiEndpoint, enabled, economySync, chatSync);
    }
}