package com.zientis.discord.controller;

import com.zientis.core.discord.DiscordIntegrationService;
import com.zientis.economy.manager.EconomyManager;
import com.zientis.economy.discord.DiscordEconomyData;
import com.zientis.discord.dto.SyncRequest;
import com.zientis.discord.dto.SyncResponse;
import com.zientis.discord.service.UserMappingService;
import com.zientis.discord.security.DiscordAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Discord Bot 經濟系統 API 控制器
 * 處理 Potato Bot 與 Zientis 經濟系統的整合
 */
@RestController
@RequestMapping("/api/v1/discord/economy")
@CrossOrigin(origins = "*")
public class DiscordEconomyController {
    
    @Autowired
    private EconomyManager economyManager;
    
    @Autowired
    private UserMappingService userMappingService;
    
    @Autowired
    private DiscordAuthService authService;
    
    @Autowired
    private DiscordIntegrationService discordIntegrationService;
    
    /**
     * 同步 Discord 餘額到 Minecraft
     */
    @PostMapping("/sync")
    public CompletableFuture<ResponseEntity<SyncResponse>> syncBalance(
            @RequestBody SyncRequest request,
            @RequestHeader(value = "Authorization", required = true) String auth,
            @RequestHeader(value = "X-Server-Key", required = true) String serverKey) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 驗證服務器密鑰
                if (!authService.validateServerKey(serverKey)) {
                    return ResponseEntity.status(401)
                        .body(new SyncResponse("error", "無效的服務器密鑰", null, null, null));
                }
                
                // 驗證用戶授權
                if (!authService.validateUserToken(auth, request.getUserId())) {
                    return ResponseEntity.status(403)
                        .body(new SyncResponse("error", "用戶授權驗證失敗", null, null, null));
                }
                
                // 獲取 Minecraft UUID
                UUID minecraftUuid = userMappingService.getMinecraftUuid(request.getUserId()).join();
                if (minecraftUuid == null) {
                    return ResponseEntity.status(404)
                        .body(new SyncResponse("error", "用戶未綁定 Minecraft 帳戶", null, null, null));
                }
                
                // 檢查帳戶是否凍結
                boolean isFrozen = economyManager.isAccountFrozen(minecraftUuid).join();
                if (isFrozen) {
                    return ResponseEntity.status(423)
                        .body(new SyncResponse("error", "Minecraft 帳戶已被凍結", null, null, null));
                }
                
                // 更新 Minecraft 餘額
                BigDecimal newBalance = BigDecimal.valueOf(request.getBalances().get("coins"));
                economyManager.setBalance(minecraftUuid, newBalance, "Discord 同步").join();
                
                // 計算並應用 Minecraft 獎勵加成
                BigDecimal bonus = calculateMinecraftBonus(request.getBalances());
                if (bonus.compareTo(BigDecimal.ZERO) > 0) {
                    economyManager.deposit(minecraftUuid, bonus, "服務器獎勵加成").join();
                }
                
                // 獲取更新後的餘額
                BigDecimal finalBalance = economyManager.getBalance(minecraftUuid).join();
                
                // 發送 Discord 通知
                discordIntegrationService.sendGameEvent("balance_sync", Map.of(
                    "user_id", request.getUserId(),
                    "minecraft_uuid", minecraftUuid.toString(),
                    "old_balance", newBalance.doubleValue(),
                    "bonus_applied", bonus.doubleValue(),
                    "final_balance", finalBalance.doubleValue()
                ));
                
                return ResponseEntity.ok(new SyncResponse(
                    "success",
                    "同步完成",
                    Instant.now().toString(),
                    Collections.singletonMap("coins", finalBalance.doubleValue()),
                    Collections.singletonMap("bonus_coins", bonus.doubleValue())
                ));
                
            } catch (Exception e) {
                return ResponseEntity.status(500)
                    .body(new SyncResponse("error", "同步失敗: " + e.getMessage(), null, null, null));
            }
        });
    }
    
    /**
     * 處理 Minecraft 活動 Webhook
     */
    @PostMapping("/webhook")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> handleMinecraftWebhook(
            @RequestBody Map<String, Object> eventData,
            @RequestHeader(value = "X-Server-Key", required = true) String serverKey) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 驗證服務器密鑰
                if (!authService.validateServerKey(serverKey)) {
                    return ResponseEntity.status(401)
                        .body(Map.of("status", "error", "message", "無效的服務器密鑰"));
                }
                
                String eventType = (String) eventData.get("activity_type");
                String userIdStr = (String) eventData.get("user_id");
                Number rewardAmount = (Number) eventData.get("reward_amount");
                
                if (userIdStr == null || rewardAmount == null) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "缺少必要參數"));
                }
                
                // 處理不同類型的活動事件
                switch (eventType) {
                    case "mining":
                        return handleMiningReward(userIdStr, rewardAmount.doubleValue(), eventData);
                    case "trading":
                        return handleTradingReward(userIdStr, rewardAmount.doubleValue(), eventData);
                    case "achievement":
                        return handleAchievementReward(userIdStr, rewardAmount.doubleValue(), eventData);
                    default:
                        return ResponseEntity.badRequest()
                            .body(Map.of("status", "error", "message", "未知的活動類型"));
                }
                
            } catch (Exception e) {
                return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "Webhook 處理失敗: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 獲取玩家經濟數據
     */
    @GetMapping("/player/{userId}")
    public CompletableFuture<ResponseEntity<DiscordEconomyData>> getPlayerEconomyData(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = true) String auth) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 驗證用戶授權
                if (!authService.validateUserToken(auth, userId)) {
                    return ResponseEntity.status(403).build();
                }
                
                // 獲取 Minecraft UUID
                UUID minecraftUuid = userMappingService.getMinecraftUuid(userId).join();
                if (minecraftUuid == null) {
                    return ResponseEntity.status(404).build();
                }
                
                // 獲取經濟數據
                DiscordEconomyData economyData = economyManager.getDiscordEconomyData(minecraftUuid).join();
                
                return ResponseEntity.ok(economyData);
                
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        });
    }
    
    /**
     * 獲取服務器經濟統計
     */
    @GetMapping("/stats/{guildId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getEconomyStats(
            @PathVariable String guildId,
            @RequestHeader(value = "X-Server-Key", required = true) String serverKey) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 驗證服務器密鑰
                if (!authService.validateServerKey(serverKey)) {
                    return ResponseEntity.status(401).build();
                }
                
                // 獲取經濟統計
                var stats = economyManager.getEconomyStats().join();
                
                Map<String, Object> response = Map.of(
                    "total_circulation", stats.getTotalCirculation().doubleValue(),
                    "total_accounts", stats.getTotalAccounts(),
                    "active_accounts", stats.getActiveAccounts(),
                    "average_balance", stats.getAverageBalance().doubleValue(),
                    "total_transactions", stats.getTotalTransactions(),
                    "server_id", "zientis-main",
                    "timestamp", Instant.now().toString()
                );
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                return ResponseEntity.status(500)
                    .body(Map.of("error", "獲取統計失敗: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 管理員調整端點
     */
    @PostMapping("/admin/adjust")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> adminAdjust(
            @RequestBody Map<String, Object> adjustmentData,
            @RequestHeader(value = "X-Admin-Key", required = true) String adminKey) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 驗證管理員密鑰
                if (!authService.validateAdminKey(adminKey)) {
                    return ResponseEntity.status(401)
                        .body(Map.of("status", "error", "message", "無效的管理員密鑰"));
                }
                
                String action = (String) adjustmentData.get("action");
                
                if ("anti_inflation".equals(action)) {
                    return handleAntiInflationAdjustment(adjustmentData);
                }
                
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "未知的調整操作"));
                
            } catch (Exception e) {
                return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "調整失敗: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean economyHealthy = economyManager != null;
            boolean discordHealthy = discordIntegrationService.checkDiscordServiceHealth().join();
            
            Map<String, Object> health = Map.of(
                "status", economyHealthy && discordHealthy ? "healthy" : "degraded",
                "economy_manager", economyHealthy,
                "discord_integration", discordHealthy,
                "timestamp", Instant.now().toString()
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("status", "unhealthy", "error", e.getMessage()));
        }
    }
    
    // === 私有輔助方法 ===
    
    private BigDecimal calculateMinecraftBonus(Map<String, Integer> balances) {
        int coins = balances.getOrDefault("coins", 0);
        
        // 基於餘額計算獎勵加成 (1% 的獎勵)
        return BigDecimal.valueOf(coins * 0.01);
    }
    
    private ResponseEntity<Map<String, Object>> handleMiningReward(String userId, double amount, Map<String, Object> eventData) {
        // 處理挖礦獎勵邏輯
        // 這裡可以添加特殊的挖礦獎勵計算
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "挖礦獎勵處理完成",
            "processed_amount", amount,
            "bonus_applied", amount * 0.1 // 10% 挖礦獎勵加成
        ));
    }
    
    private ResponseEntity<Map<String, Object>> handleTradingReward(String userId, double amount, Map<String, Object> eventData) {
        // 處理交易獎勵邏輯
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "交易獎勵處理完成",
            "processed_amount", amount
        ));
    }
    
    private ResponseEntity<Map<String, Object>> handleAchievementReward(String userId, double amount, Map<String, Object> eventData) {
        // 處理成就獎勵邏輯
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "成就獎勵處理完成",
            "processed_amount", amount,
            "bonus_applied", amount * 0.2 // 20% 成就獎勵加成
        ));
    }
    
    private ResponseEntity<Map<String, Object>> handleAntiInflationAdjustment(Map<String, Object> adjustmentData) {
        // 處理抗通膨調整邏輯
        Map<String, Object> minecraftData = (Map<String, Object>) adjustmentData.get("minecraft_data");
        
        if (minecraftData != null) {
            double totalCirculation = (Double) minecraftData.get("total_circulation");
            int totalAccounts = (Integer) minecraftData.get("total_accounts");
            
            // 計算建議的調整倍數
            double suggestedMultiplier = calculateInflationAdjustment(totalCirculation, totalAccounts);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "抗通膨調整計算完成",
                "reward_multiplier", suggestedMultiplier,
                "circulation_analysis", Map.of(
                    "current_circulation", totalCirculation,
                    "accounts", totalAccounts,
                    "avg_balance", totalCirculation / totalAccounts
                )
            ));
        }
        
        return ResponseEntity.badRequest()
            .body(Map.of("status", "error", "message", "缺少 Minecraft 數據"));
    }
    
    private double calculateInflationAdjustment(double totalCirculation, int totalAccounts) {
        double avgBalance = totalCirculation / totalAccounts;
        
        // 簡單的通膨控制邏輯
        if (avgBalance > 10000) {
            return 0.8; // 減少獎勵 20%
        } else if (avgBalance < 1000) {
            return 1.2; // 增加獎勵 20%
        }
        
        return 1.0; // 維持現狀
    }
}