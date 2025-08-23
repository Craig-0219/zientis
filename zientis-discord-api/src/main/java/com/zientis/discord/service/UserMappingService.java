package com.zientis.discord.service;

import com.zientis.core.discord.DiscordIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Discord 用戶與 Minecraft 玩家映射服務
 * 管理用戶帳戶綁定和驗證
 */
@Service
public class UserMappingService {
    
    private static final Logger logger = Logger.getLogger(UserMappingService.class.getName());
    
    // 用戶映射緩存 (實際應用中應使用數據庫)
    private final Map<String, UUID> discordToMinecraft = new ConcurrentHashMap<>();
    private final Map<UUID, String> minecraftToDiscord = new ConcurrentHashMap<>();
    
    // 驗證碼緩存 (24小時過期)
    private final Map<String, VerificationEntry> verificationCodes = new ConcurrentHashMap<>();
    
    @Autowired
    private DiscordIntegrationService discordIntegrationService;
    
    /**
     * 獲取 Discord 用戶對應的 Minecraft UUID
     */
    public CompletableFuture<UUID> getMinecraftUuid(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID uuid = discordToMinecraft.get(discordId);
                if (uuid != null) {
                    logger.info("Found Minecraft UUID for Discord user " + discordId + ": " + uuid);
                    return uuid;
                }
                
                logger.warning("No Minecraft UUID found for Discord user: " + discordId);
                return null;
                
            } catch (Exception e) {
                logger.severe("Error getting Minecraft UUID for Discord user " + discordId + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 獲取 Minecraft 玩家對應的 Discord ID
     */
    public CompletableFuture<String> getDiscordId(UUID minecraftUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String discordId = minecraftToDiscord.get(minecraftUuid);
                if (discordId != null) {
                    logger.info("Found Discord ID for Minecraft UUID " + minecraftUuid + ": " + discordId);
                    return discordId;
                }
                
                logger.warning("No Discord ID found for Minecraft UUID: " + minecraftUuid);
                return null;
                
            } catch (Exception e) {
                logger.severe("Error getting Discord ID for Minecraft UUID " + minecraftUuid + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 生成帳戶綁定驗證碼
     */
    public CompletableFuture<String> generateVerificationCode(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 生成 6 位數驗證碼
                String code = String.format("%06d", (int) (Math.random() * 1000000));
                
                VerificationEntry entry = new VerificationEntry(
                    discordId,
                    code,
                    LocalDateTime.now().plusHours(24) // 24小時後過期
                );
                
                verificationCodes.put(code, entry);
                
                logger.info("Generated verification code for Discord user " + discordId + ": " + code);
                return code;
                
            } catch (Exception e) {
                logger.severe("Error generating verification code for Discord user " + discordId + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 驗證綁定代碼並建立帳戶關聯
     */
    public CompletableFuture<Boolean> linkAccounts(String discordId, UUID minecraftUuid, String verificationCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 驗證綁定代碼
                if (!validateVerificationCode(discordId, verificationCode)) {
                    logger.warning("Invalid verification code for Discord user " + discordId);
                    return false;
                }
                
                // 2. 檢查是否已經綁定其他帳戶
                if (discordToMinecraft.containsKey(discordId) || minecraftToDiscord.containsKey(minecraftUuid)) {
                    logger.warning("Account already linked: Discord " + discordId + " or Minecraft " + minecraftUuid);
                    return false;
                }
                
                // 3. 建立雙向映射
                discordToMinecraft.put(discordId, minecraftUuid);
                minecraftToDiscord.put(minecraftUuid, discordId);
                
                // 4. 清除驗證碼
                verificationCodes.entrySet().removeIf(entry -> 
                    entry.getValue().getDiscordId().equals(discordId));
                
                logger.info("Successfully linked accounts: Discord " + discordId + " <-> Minecraft " + minecraftUuid);
                
                // 5. 同步現有經濟數據
                return syncExistingEconomyData(discordId, minecraftUuid).join();
                
            } catch (Exception e) {
                logger.severe("Error linking accounts: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * 解除帳戶綁定
     */
    public CompletableFuture<Boolean> unlinkAccounts(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID minecraftUuid = discordToMinecraft.get(discordId);
                if (minecraftUuid == null) {
                    logger.warning("No linked account found for Discord user: " + discordId);
                    return false;
                }
                
                // 移除雙向映射
                discordToMinecraft.remove(discordId);
                minecraftToDiscord.remove(minecraftUuid);
                
                logger.info("Successfully unlinked accounts: Discord " + discordId + " <-> Minecraft " + minecraftUuid);
                
                // 通知 Discord Bot
                discordIntegrationService.sendGameEvent("account_unlinked", Map.of(
                    "discord_id", discordId,
                    "minecraft_uuid", minecraftUuid.toString(),
                    "timestamp", LocalDateTime.now().toString()
                ));
                
                return true;
                
            } catch (Exception e) {
                logger.severe("Error unlinking accounts: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * 檢查帳戶是否已綁定
     */
    public CompletableFuture<Boolean> isAccountLinked(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            return discordToMinecraft.containsKey(discordId);
        });
    }
    
    /**
     * 獲取所有綁定的帳戶統計
     */
    public CompletableFuture<Map<String, Object>> getBindingStats() {
        return CompletableFuture.supplyAsync(() -> {
            return Map.of(
                "total_linked_accounts", discordToMinecraft.size(),
                "active_verification_codes", verificationCodes.size(),
                "last_updated", LocalDateTime.now().toString()
            );
        });
    }
    
    // === 私有輔助方法 ===
    
    /**
     * 驗證綁定代碼
     */
    private boolean validateVerificationCode(String discordId, String code) {
        VerificationEntry entry = verificationCodes.get(code);
        
        if (entry == null) {
            return false;
        }
        
        // 檢查是否過期
        if (LocalDateTime.now().isAfter(entry.getExpiryTime())) {
            verificationCodes.remove(code);
            return false;
        }
        
        // 檢查用戶是否匹配
        return entry.getDiscordId().equals(discordId);
    }
    
    /**
     * 同步現有經濟數據
     */
    private CompletableFuture<Boolean> syncExistingEconomyData(String discordId, UUID minecraftUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 從 Discord Bot 獲取現有餘額
                Map<String, Object> discordData = discordIntegrationService
                    .getDiscordUserData(minecraftUuid).join();
                
                if (discordData.containsKey("raw_data")) {
                    logger.info("Syncing existing economy data for linked accounts");
                    
                    // 通知 Discord Bot 帳戶已綁定
                    discordIntegrationService.sendGameEvent("account_linked", Map.of(
                        "discord_id", discordId,
                        "minecraft_uuid", minecraftUuid.toString(),
                        "sync_data", discordData,
                        "timestamp", LocalDateTime.now().toString()
                    ));
                }
                
                return true;
                
            } catch (Exception e) {
                logger.warning("Error syncing existing economy data: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * 清理過期的驗證碼
     */
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpiryTime()));
        
        logger.info("Cleaned up expired verification codes");
    }
    
    /**
     * 驗證條目內部類
     */
    private static class VerificationEntry {
        private final String discordId;
        private final String code;
        private final LocalDateTime expiryTime;
        
        public VerificationEntry(String discordId, String code, LocalDateTime expiryTime) {
            this.discordId = discordId;
            this.code = code;
            this.expiryTime = expiryTime;
        }
        
        public String getDiscordId() {
            return discordId;
        }
        
        public String getCode() {
            return code;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}