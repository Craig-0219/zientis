package com.zientis.core.discord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

/**
 * Discord Bot API客戶端
 * 負責與Discord Bot後端API的HTTP通訊
 */
public class DiscordApiClient {
    
    private final Logger logger;
    private final DiscordConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public DiscordApiClient(DiscordConfig config) {
        this.config = config;
        this.logger = Logger.getLogger(DiscordApiClient.class.getName());
        this.objectMapper = new ObjectMapper();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .build();
                
        logger.info("Discord API客戶端初始化完成: " + config.getBotApiEndpoint());
    }
    
    /**
     * 發送GET請求到Discord Bot API
     */
    public CompletableFuture<JsonNode> get(String endpoint) {
        return sendRequest("GET", endpoint, null);
    }
    
    /**
     * 發送POST請求到Discord Bot API
     */
    public CompletableFuture<JsonNode> post(String endpoint, Object data) {
        return sendRequest("POST", endpoint, data);
    }
    
    /**
     * 發送PUT請求到Discord Bot API
     */
    public CompletableFuture<JsonNode> put(String endpoint, Object data) {
        return sendRequest("PUT", endpoint, data);
    }
    
    /**
     * 發送DELETE請求到Discord Bot API
     */
    public CompletableFuture<JsonNode> delete(String endpoint) {
        return sendRequest("DELETE", endpoint, null);
    }
    
    /**
     * 通用HTTP請求方法
     */
    private CompletableFuture<JsonNode> sendRequest(String method, String endpoint, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = config.getBotApiEndpoint() + endpoint;
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(config.getReadTimeout()))
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Content-Type", "application/json")
                        .header("X-Server-Key", config.getServerKey());
                
                // 設置請求方法和資料
                if (data != null) {
                    String jsonData = objectMapper.writeValueAsString(data);
                    
                    // 如果啟用加密，對資料進行加密
                    if (config.isEnableEncryption()) {
                        jsonData = encrypt(jsonData);
                    }
                    
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
            
            // 如果回應是加密的，進行解密
            if (config.isEnableEncryption() && responseBody.startsWith("encrypted:")) {
                responseBody = decrypt(responseBody.substring(10));
            }
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // 檢查HTTP狀態碼
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return jsonResponse;
            } else {
                String errorMessage = jsonResponse.has("error") ? 
                    jsonResponse.get("error").asText() : 
                    "HTTP " + response.statusCode();
                throw new RuntimeException("API請求失敗: " + errorMessage);
            }
            
        } catch (Exception e) {
            // 如果還有重試次數，則重試
            if (attempt < config.getMaxRetries()) {
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
     * 測試API連接
     */
    public CompletableFuture<Boolean> testConnection() {
        return get("/health")
                .thenApply(response -> {
                    boolean healthy = response.has("status") && 
                        "healthy".equals(response.get("status").asText());
                    logger.info("Discord API連接測試: " + (healthy ? "成功" : "失敗"));
                    return healthy;
                })
                .exceptionally(throwable -> {
                    logger.severe("Discord API連接測試失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 同步玩家經濟資料到Discord
     */
    public CompletableFuture<Boolean> syncPlayerEconomy(String minecraftUuid, 
            Map<String, Object> economyData) {
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("minecraft_uuid", minecraftUuid);
        payload.put("economy_data", economyData);
        payload.put("source_platform", "minecraft");
        payload.put("timestamp", System.currentTimeMillis());
        
        return post("/cross-platform/economy/sync", payload)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    
                    if (success) {
                        logger.info("玩家經濟資料同步成功: " + minecraftUuid);
                    } else {
                        logger.warning("玩家經濟資料同步失敗: " + minecraftUuid);
                    }
                    
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("經濟資料同步API調用失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 發送玩家成就資料到Discord
     */
    public CompletableFuture<Boolean> syncPlayerAchievements(String minecraftUuid, 
            Map<String, Object> achievementData) {
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("minecraft_uuid", minecraftUuid);
        payload.put("achievement_data", achievementData);
        payload.put("source_platform", "minecraft");
        payload.put("timestamp", System.currentTimeMillis());
        
        return post("/cross-platform/achievements/sync", payload)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    
                    if (success) {
                        logger.info("玩家成就資料同步成功: " + minecraftUuid);
                    } else {
                        logger.warning("玩家成就資料同步失敗: " + minecraftUuid);
                    }
                    
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("成就資料同步API調用失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 加密資料
     */
    private String encrypt(String data) {
        try {
            if (config.getServerKey().isEmpty()) {
                return data;
            }
            
            Cipher cipher = Cipher.getInstance(config.getEncryptionAlgorithm());
            SecretKeySpec keySpec = new SecretKeySpec(
                config.getServerKey().getBytes(), config.getEncryptionAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return "encrypted:" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.warning("資料加密失敗，使用明文傳輸: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * 解密資料
     */
    private String decrypt(String encryptedData) {
        try {
            if (config.getServerKey().isEmpty()) {
                return encryptedData;
            }
            
            Cipher cipher = Cipher.getInstance(config.getEncryptionAlgorithm());
            SecretKeySpec keySpec = new SecretKeySpec(
                config.getServerKey().getBytes(), config.getEncryptionAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            logger.warning("資料解密失敗: " + e.getMessage());
            return encryptedData;
        }
    }
    
    /**
     * 發送遊戲事件到Discord Bot
     */
    public CompletableFuture<Boolean> sendGameEvent(String eventType, Map<String, Object> eventData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event_type", eventType);
        payload.put("event_data", eventData);
        payload.put("timestamp", System.currentTimeMillis());
        
        return post("/game-events", payload)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    
                    if (success) {
                        logger.info("遊戲事件發送成功: " + eventType);
                    } else {
                        logger.warning("遊戲事件發送失敗: " + eventType);
                    }
                    
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("發送遊戲事件API調用失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    // =============== DiscordSRV風格的新方法 ===============
    
    /**
     * 發送訊息到Discord頻道
     */
    public CompletableFuture<JsonNode> sendChannelMessage(String channelId, Map<String, Object> messageData) {
        return post("/discord/channels/" + channelId + "/messages", messageData)
                .exceptionally(throwable -> {
                    logger.severe("發送頻道訊息失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 編輯Discord頻道訊息
     */
    public CompletableFuture<JsonNode> editChannelMessage(String channelId, String messageId, Map<String, Object> messageData) {
        return put("/discord/channels/" + channelId + "/messages/" + messageId, messageData)
                .exceptionally(throwable -> {
                    logger.severe("編輯頻道訊息失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 發送Webhook訊息
     */
    public CompletableFuture<JsonNode> sendWebhookMessage(String webhookUrl, Map<String, Object> messageData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("webhook_url", webhookUrl);
        payload.put("message_data", messageData);
        
        return post("/discord/webhook/send", payload)
                .exceptionally(throwable -> {
                    logger.severe("發送Webhook訊息失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 更新頻道話題
     */
    public CompletableFuture<Boolean> updateChannelTopic(String channelId, String topic) {
        Map<String, Object> data = new HashMap<>();
        data.put("topic", topic);
        
        return put("/discord/channels/" + channelId + "/topic", data)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("更新頻道話題失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 獲取頻道資訊
     */
    public CompletableFuture<JsonNode> getChannelInfo(String channelId) {
        return get("/discord/channels/" + channelId)
                .exceptionally(throwable -> {
                    logger.severe("獲取頻道資訊失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 獲取Discord伺服器資訊
     */
    public CompletableFuture<JsonNode> getGuildInfo(String guildId) {
        return get("/discord/guilds/" + guildId)
                .exceptionally(throwable -> {
                    logger.severe("獲取伺服器資訊失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 發送私人訊息
     */
    public CompletableFuture<JsonNode> sendDirectMessage(String userId, Map<String, Object> messageData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("message_data", messageData);
        
        return post("/discord/users/" + userId + "/dm", payload)
                .exceptionally(throwable -> {
                    logger.severe("發送私人訊息失敗: " + throwable.getMessage());
                    return objectMapper.createObjectNode();
                });
    }
    
    /**
     * 批次發送訊息到多個頻道
     */
    public CompletableFuture<Boolean> sendToMultipleChannels(String[] channelIds, Map<String, Object> messageData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("channel_ids", channelIds);
        payload.put("message_data", messageData);
        
        return post("/discord/channels/broadcast", payload)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("批次發送訊息失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }
    
    /**
     * 發送伺服器狀態更新
     */
    public CompletableFuture<Boolean> updateServerStatus(Map<String, Object> statusData) {
        return post("/discord/server-status", statusData)
                .thenApply(response -> {
                    boolean success = response.has("success") && 
                        response.get("success").asBoolean();
                    return success;
                })
                .exceptionally(throwable -> {
                    logger.severe("更新伺服器狀態失敗: " + throwable.getMessage());
                    return Boolean.FALSE;
                });
    }

    /**
     * 關閉客戶端
     */
    public void shutdown() {
        logger.info("Discord API客戶端已關閉");
    }
}