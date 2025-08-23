package com.zientis.discord.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Discord Bot 認證服務
 * 處理服務器密鑰驗證、用戶令牌驗證和請求簽名
 */
@Service
public class DiscordAuthService {
    
    private static final Logger logger = Logger.getLogger(DiscordAuthService.class.getName());
    
    @Value("${discord.integration.server-key:default_server_key}")
    private String serverKey;
    
    @Value("${discord.integration.admin-key:default_admin_key}")
    private String adminKey;
    
    @Value("${discord.integration.signature-secret:default_signature_secret}")
    private String signatureSecret;
    
    // 用戶令牌緩存 (實際應用中應使用 Redis 或數據庫)
    private final ConcurrentHashMap<String, UserTokenEntry> userTokens = new ConcurrentHashMap<>();
    
    // 令牌有效期 (24小時)
    private static final long TOKEN_VALIDITY_HOURS = 24;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    /**
     * 驗證服務器密鑰
     */
    public boolean validateServerKey(String providedKey) {
        try {
            if (providedKey == null || providedKey.trim().isEmpty()) {
                logger.warning("Empty server key provided");
                return false;
            }
            
            // 移除 "Bearer " 前綴（如果存在）
            String cleanKey = providedKey.startsWith("Bearer ") 
                ? providedKey.substring(7) 
                : providedKey;
            
            boolean isValid = serverKey.equals(cleanKey);
            
            if (!isValid) {
                logger.warning("Invalid server key provided: " + cleanKey.substring(0, Math.min(8, cleanKey.length())) + "...");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.severe("Error validating server key: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 驗證管理員密鑰
     */
    public boolean validateAdminKey(String providedKey) {
        try {
            if (providedKey == null || providedKey.trim().isEmpty()) {
                logger.warning("Empty admin key provided");
                return false;
            }
            
            String cleanKey = providedKey.startsWith("Bearer ") 
                ? providedKey.substring(7) 
                : providedKey;
            
            boolean isValid = adminKey.equals(cleanKey);
            
            if (!isValid) {
                logger.warning("Invalid admin key provided");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.severe("Error validating admin key: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 驗證用戶令牌
     */
    public boolean validateUserToken(String token, String userId) {
        try {
            if (token == null || userId == null) {
                logger.warning("Null token or user ID provided");
                return false;
            }
            
            String cleanToken = token.startsWith("Bearer ") 
                ? token.substring(7) 
                : token;
            
            UserTokenEntry entry = userTokens.get(userId);
            if (entry == null) {
                logger.warning("No token found for user: " + userId);
                return false;
            }
            
            // 檢查令牌是否過期
            if (Instant.now().isAfter(entry.getExpiryTime())) {
                userTokens.remove(userId);
                logger.warning("Expired token for user: " + userId);
                return false;
            }
            
            // 驗證令牌
            boolean isValid = entry.getToken().equals(cleanToken);
            
            if (isValid) {
                // 更新最後使用時間
                entry.updateLastUsed();
                logger.fine("Valid token for user: " + userId);
            } else {
                logger.warning("Invalid token for user: " + userId);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.severe("Error validating user token: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成用戶令牌
     */
    public String generateUserToken(String userId) {
        try {
            // 生成基於時間和用戶ID的令牌
            String tokenData = userId + ":" + Instant.now().toEpochMilli();
            String token = Base64.getEncoder().encodeToString(tokenData.getBytes(StandardCharsets.UTF_8));
            
            Instant expiryTime = Instant.now().plusSeconds(TOKEN_VALIDITY_HOURS * 3600);
            UserTokenEntry entry = new UserTokenEntry(token, expiryTime);
            
            userTokens.put(userId, entry);
            
            logger.info("Generated token for user: " + userId);
            return token;
            
        } catch (Exception e) {
            logger.severe("Error generating user token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 撤銷用戶令牌
     */
    public boolean revokeUserToken(String userId) {
        try {
            UserTokenEntry removed = userTokens.remove(userId);
            if (removed != null) {
                logger.info("Revoked token for user: " + userId);
                return true;
            } else {
                logger.warning("No token to revoke for user: " + userId);
                return false;
            }
            
        } catch (Exception e) {
            logger.severe("Error revoking user token: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 驗證請求簽名
     */
    public boolean verifyRequestSignature(String payload, String signature) {
        try {
            if (payload == null || signature == null) {
                logger.warning("Null payload or signature provided");
                return false;
            }
            
            String expectedSignature = generateSignature(payload);
            boolean isValid = expectedSignature.equals(signature);
            
            if (!isValid) {
                logger.warning("Invalid request signature");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.severe("Error verifying request signature: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成請求簽名
     */
    public String generateSignature(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                signatureSecret.getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.severe("Error generating signature: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 清理過期令牌
     */
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int removed = 0;
        
        userTokens.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue().getExpiryTime())) {
                removed++;
                return true;
            }
            return false;
        });
        
        if (removed > 0) {
            logger.info("Cleaned up " + removed + " expired tokens");
        }
    }
    
    /**
     * 獲取認證統計信息
     */
    public AuthStats getAuthStats() {
        Instant now = Instant.now();
        long activeTokens = userTokens.values().stream()
            .filter(entry -> now.isBefore(entry.getExpiryTime()))
            .count();
        
        return new AuthStats(
            userTokens.size(),
            activeTokens,
            now.toString()
        );
    }
    
    /**
     * 用戶令牌條目內部類
     */
    private static class UserTokenEntry {
        private final String token;
        private final Instant expiryTime;
        private Instant lastUsed;
        
        public UserTokenEntry(String token, Instant expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
            this.lastUsed = Instant.now();
        }
        
        public String getToken() {
            return token;
        }
        
        public Instant getExpiryTime() {
            return expiryTime;
        }
        
        public Instant getLastUsed() {
            return lastUsed;
        }
        
        public void updateLastUsed() {
            this.lastUsed = Instant.now();
        }
    }
    
    /**
     * 認證統計信息類
     */
    public static class AuthStats {
        private final long totalTokens;
        private final long activeTokens;
        private final String timestamp;
        
        public AuthStats(long totalTokens, long activeTokens, String timestamp) {
            this.totalTokens = totalTokens;
            this.activeTokens = activeTokens;
            this.timestamp = timestamp;
        }
        
        public long getTotalTokens() {
            return totalTokens;
        }
        
        public long getActiveTokens() {
            return activeTokens;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
    }
}