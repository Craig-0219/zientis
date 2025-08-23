package com.zientis.core.discord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

/**
 * 直接Discord API客戶端
 * 使用Bot Token直接與Discord REST API通訊
 * 用於DiscordSRV風格功能（聊天、狀態、事件）
 */
public class DirectDiscordApiClient {
    
    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";
    
    private final Logger logger;
    private final DiscordConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public DirectDiscordApiClient(DiscordConfig config) {
        this.config = config;
        this.logger = Logger.getLogger(DirectDiscordApiClient.class.getName());
        this.objectMapper = new ObjectMapper();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .build();
                
        logger.info("Direct Discord API客戶端初始化完成，Guild ID: " + config.getGuildId());
    }
    
    /**
     * 發送訊息到Discord頻道
     */
    public CompletableFuture<JsonNode> sendChannelMessage(String channelId, Map<String, Object> messageData) {
        String endpoint = "/channels/" + channelId + "/messages";
        return post(endpoint, messageData);
    }
    
    /**
     * 編輯Discord頻道訊息
     */
    public CompletableFuture<JsonNode> editChannelMessage(String channelId, String messageId, Map<String, Object> messageData) {
        String endpoint = "/channels/" + channelId + "/messages/" + messageId;
        return patch(endpoint, messageData);
    }
    
    /**
     * 更新頻道資訊（包括話題）
     */
    public CompletableFuture<JsonNode> updateChannel(String channelId, Map<String, Object> channelData) {
        String endpoint = "/channels/" + channelId;
        return patch(endpoint, channelData);
    }
    
    /**
     * 獲取頻道資訊
     */
    public CompletableFuture<JsonNode> getChannel(String channelId) {
        String endpoint = "/channels/" + channelId;
        return get(endpoint);
    }
    
    /**
     * 獲取Guild資訊
     */
    public CompletableFuture<JsonNode> getGuild() {
        String endpoint = "/guilds/" + config.getGuildId();
        return get(endpoint);
    }
    
    /**
     * 獲取Guild成員列表
     */
    public CompletableFuture<JsonNode> getGuildMembers(int limit) {
        String endpoint = "/guilds/" + config.getGuildId() + "/members?limit=" + limit;
        return get(endpoint);
    }
    
    /**
     * 發送私人訊息
     */
    public CompletableFuture<JsonNode> sendDirectMessage(String userId, Map<String, Object> messageData) {
        // 先創建DM頻道
        Map<String, Object> dmData = Map.of("recipient_id", userId);
        
        return post("/users/@me/channels", dmData)
                .thenCompose(response -> {
                    if (response.has("id")) {
                        String dmChannelId = response.get("id").asText();
                        return sendChannelMessage(dmChannelId, messageData);
                    } else {
                        throw new RuntimeException("無法創建DM頻道");
                    }
                });
    }
    
    /**
     * 測試Bot連接
     */
    public CompletableFuture<Boolean> testConnection() {
        return get("/users/@me")
                .thenApply(response -> {
                    boolean success = response.has("id");
                    if (success) {
                        String botName = response.has("username") ? response.get("username").asText() : "Unknown";
                        logger.info("Discord Bot連接成功: " + botName);
                    } else {
                        logger.warning("Discord Bot連接失敗");
                    }
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("Discord Bot連接測試失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 發送GET請求
     */
    private CompletableFuture<JsonNode> get(String endpoint) {
        return sendRequest("GET", endpoint, null);
    }
    
    /**
     * 發送POST請求
     */
    private CompletableFuture<JsonNode> post(String endpoint, Object data) {
        return sendRequest("POST", endpoint, data);
    }
    
    /**
     * 發送PATCH請求
     */
    private CompletableFuture<JsonNode> patch(String endpoint, Object data) {
        return sendRequest("PATCH", endpoint, data);
    }
    
    /**
     * 通用HTTP請求方法
     */
    private CompletableFuture<JsonNode> sendRequest(String method, String endpoint, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = DISCORD_API_BASE + endpoint;
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(config.getReadTimeout()))
                        .header("Authorization", "Bot " + config.getBotToken())
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Zientis-Minecraft-Bot/1.0");
                
                // 設置請求方法和資料
                if (data != null) {
                    String jsonData = objectMapper.writeValueAsString(data);
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonData));
                } else {
                    requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
                }
                
                HttpRequest request = requestBuilder.build();
                
                // 發送請求並處理回應
                return sendWithRetry(request, 0);
                
            } catch (Exception e) {
                logger.severe("Discord API請求失敗: " + e.getMessage());
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * 帶重試機制的請求發送
     */
    private JsonNode sendWithRetry(HttpRequest request, int attempt) throws Exception {
        try {
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            String responseBody = response.body();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // 檢查HTTP狀態碼
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return jsonResponse;
            } else if (response.statusCode() == 429) {
                // 處理Rate Limit
                int retryAfter = jsonResponse.has("retry_after") ? 
                    jsonResponse.get("retry_after").asInt() * 1000 : 1000;
                
                logger.warning("Discord API Rate Limited，等待 " + retryAfter + "ms");
                Thread.sleep(retryAfter);
                
                if (attempt < config.getMaxRetries()) {
                    return sendWithRetry(request, attempt + 1);
                } else {
                    throw new RuntimeException("Rate limit exceeded");
                }
            } else {
                String errorMessage = jsonResponse.has("message") ? 
                    jsonResponse.get("message").asText() : 
                    "HTTP " + response.statusCode();
                throw new RuntimeException("Discord API請求失敗: " + errorMessage);
            }
            
        } catch (Exception e) {
            // 如果還有重試次數，則重試
            if (attempt < config.getMaxRetries() && !(e instanceof RuntimeException && e.getMessage().contains("Rate limit"))) {
                logger.warning("Discord API請求失敗，準備重試 (" + (attempt + 1) + "/" + 
                    config.getMaxRetries() + "): " + e.getMessage());
                
                Thread.sleep(config.getRetryDelay() * (attempt + 1));
                return sendWithRetry(request, attempt + 1);
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 獲取配置
     */
    public DiscordConfig getConfig() {
        return config;
    }
    
    /**
     * 關閉客戶端
     */
    public void shutdown() {
        logger.info("Direct Discord API客戶端已關閉");
    }
}