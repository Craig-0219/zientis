package com.zientis.nations.api;

import com.zientis.nations.data.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 賽恩堤斯國家系統 API 接口
 * 
 * 提供國家管理、成員管理、外交和戰爭系統的完整功能
 * 同時預留Discord Bot整合的接口
 */
public interface ZientisNationsAPI {
    
    // === 國家管理 ===
    
    /**
     * 創建新國家
     * 
     * @param nationName 國家名稱
     * @param founderId 創始人UUID
     * @return 異步返回創建的國家對象
     */
    CompletableFuture<Nation> createNation(String nationName, UUID founderId);
    
    /**
     * 解散國家
     * 
     * @param nationId 國家ID
     * @param dissolverId 解散者UUID
     * @return 異步返回是否成功解散
     */
    CompletableFuture<Boolean> dissolveNation(UUID nationId, UUID dissolverId);
    
    /**
     * 獲取國家信息
     * 
     * @param nationId 國家ID
     * @return 異步返回國家對象
     */
    CompletableFuture<Nation> getNation(UUID nationId);
    
    /**
     * 根據名稱獲取國家
     * 
     * @param nationName 國家名稱
     * @return 異步返回國家對象
     */
    CompletableFuture<Nation> getNationByName(String nationName);
    
    /**
     * 獲取玩家所屬的國家
     * 
     * @param playerId 玩家UUID
     * @return 異步返回國家對象
     */
    CompletableFuture<Nation> getPlayerNation(UUID playerId);
    
    /**
     * 獲取所有國家列表
     * 
     * @return 異步返回國家列表
     */
    CompletableFuture<List<Nation>> getAllNations();
    
    /**
     * 獲取活躍國家列表
     * 
     * @return 異步返回活躍國家列表
     */
    CompletableFuture<List<Nation>> getActiveNations();
    
    /**
     * 升級國家等級
     * 
     * @param nationId 國家ID
     * @param upgraderId 發起升級的玩家UUID
     * @return 異步返回是否成功升級
     */
    CompletableFuture<Boolean> upgradeNation(UUID nationId, UUID upgraderId);
    
    // === 成員管理 ===
    
    /**
     * 邀請玩家加入國家
     * 
     * @param nationId 國家ID
     * @param inviterId 邀請者UUID
     * @param targetId 被邀請者UUID
     * @param role 初始角色
     * @return 異步返回是否成功邀請
     */
    CompletableFuture<Boolean> invitePlayer(UUID nationId, UUID inviterId, UUID targetId, NationRole role);
    
    /**
     * 玩家接受邀請加入國家
     * 
     * @param nationId 國家ID
     * @param playerId 玩家UUID
     * @return 異步返回是否成功加入
     */
    CompletableFuture<Boolean> joinNation(UUID nationId, UUID playerId);
    
    /**
     * 玩家離開國家
     * 
     * @param nationId 國家ID
     * @param playerId 玩家UUID
     * @return 異步返回是否成功離開
     */
    CompletableFuture<Boolean> leaveNation(UUID nationId, UUID playerId);
    
    /**
     * 踢出國家成員
     * 
     * @param nationId 國家ID
     * @param kickerId 踢出者UUID
     * @param targetId 被踢出者UUID
     * @return 異步返回是否成功踢出
     */
    CompletableFuture<Boolean> kickMember(UUID nationId, UUID kickerId, UUID targetId);
    
    /**
     * 設置成員角色
     * 
     * @param nationId 國家ID
     * @param setId 設置者UUID
     * @param targetId 目標成員UUID
     * @param role 新角色
     * @return 異步返回是否成功設置
     */
    CompletableFuture<Boolean> setMemberRole(UUID nationId, UUID setId, UUID targetId, NationRole role);
    
    /**
     * 獲取國家成員列表
     * 
     * @param nationId 國家ID
     * @return 異步返回成員UUID和角色的映射
     */
    CompletableFuture<Map<UUID, NationRole>> getNationMembers(UUID nationId);
    
    // === 領土管理 ===
    
    /**
     * 聲稱島嶼領土
     * 
     * @param nationId 國家ID
     * @param claimerId 聲稱者UUID
     * @param islandId 島嶼ID
     * @return 異步返回是否成功聲稱
     */
    CompletableFuture<Boolean> claimTerritory(UUID nationId, UUID claimerId, UUID islandId);
    
    /**
     * 放棄島嶼領土
     * 
     * @param nationId 國家ID
     * @param unclaimerId 放棄者UUID
     * @param islandId 島嶼ID
     * @return 異步返回是否成功放棄
     */
    CompletableFuture<Boolean> unclaimTerritory(UUID nationId, UUID unclaimerId, UUID islandId);
    
    /**
     * 設置國家首都
     * 
     * @param nationId 國家ID
     * @param setId 設置者UUID
     * @param islandId 首都島嶼ID
     * @return 異步返回是否成功設置
     */
    CompletableFuture<Boolean> setCapital(UUID nationId, UUID setId, UUID islandId);
    
    /**
     * 獲取國家領土列表
     * 
     * @param nationId 國家ID
     * @return 異步返回島嶼ID列表
     */
    CompletableFuture<List<UUID>> getNationTerritories(UUID nationId);
    
    /**
     * 檢查島嶼是否屬於某國家
     * 
     * @param islandId 島嶼ID
     * @return 異步返回擁有該島嶼的國家ID，如果無主則返回null
     */
    CompletableFuture<UUID> getIslandOwner(UUID islandId);
    
    // === 經濟管理 ===
    
    /**
     * 向國庫存款
     * 
     * @param nationId 國家ID
     * @param depositorId 存款者UUID
     * @param amount 金額
     * @return 異步返回是否成功存款
     */
    CompletableFuture<Boolean> depositToTreasury(UUID nationId, UUID depositorId, double amount);
    
    /**
     * 從國庫提款
     * 
     * @param nationId 國家ID
     * @param withdrawerId 提款者UUID
     * @param amount 金額
     * @return 異步返回是否成功提款
     */
    CompletableFuture<Boolean> withdrawFromTreasury(UUID nationId, UUID withdrawerId, double amount);
    
    /**
     * 設置稅收政策
     * 
     * @param nationId 國家ID
     * @param setId 設置者UUID
     * @param dailyTax 每日稅收
     * @param memberTax 成員稅收
     * @return 異步返回是否成功設置
     */
    CompletableFuture<Boolean> setTaxPolicy(UUID nationId, UUID setId, double dailyTax, double memberTax);
    
    /**
     * 收取稅收
     * 
     * @param nationId 國家ID
     * @return 異步返回收取的稅收總額
     */
    CompletableFuture<Double> collectTaxes(UUID nationId);
    
    // === 外交系統 ===
    
    /**
     * 設置與其他國家的外交關係
     * 
     * @param nationId 國家ID
     * @param setId 設置者UUID
     * @param targetNationId 目標國家ID
     * @param relation 外交關係
     * @return 異步返回是否成功設置
     */
    CompletableFuture<Boolean> setDiplomaticRelation(UUID nationId, UUID setId, UUID targetNationId, DiplomaticRelation relation);
    
    /**
     * 獲取國家的所有外交關係
     * 
     * @param nationId 國家ID
     * @return 異步返回外交關係映射
     */
    CompletableFuture<Map<UUID, DiplomaticRelation>> getDiplomaticRelations(UUID nationId);
    
    /**
     * 獲取兩國間的外交關係
     * 
     * @param nationId1 國家1 ID
     * @param nationId2 國家2 ID
     * @return 異步返回外交關係
     */
    CompletableFuture<DiplomaticRelation> getDiplomaticRelation(UUID nationId1, UUID nationId2);
    
    // === 戰爭系統 ===
    
    /**
     * 對其他國家宣戰
     * 
     * @param aggressorNationId 發起方國家ID
     * @param declarerId 宣戰者UUID
     * @param targetNationId 目標國家ID
     * @param warReason 戰爭理由
     * @return 異步返回是否成功宣戰
     */
    CompletableFuture<Boolean> declareWar(UUID aggressorNationId, UUID declarerId, UUID targetNationId, String warReason);
    
    /**
     * 結束戰爭
     * 
     * @param nationId1 國家1 ID
     * @param nationId2 國家2 ID
     * @param victoryNationId 勝利方國家ID
     * @param endReason 結束原因
     * @return 異步返回是否成功結束
     */
    CompletableFuture<Boolean> endWar(UUID nationId1, UUID nationId2, UUID victoryNationId, String endReason);
    
    /**
     * 獲取國家的活躍戰爭列表
     * 
     * @param nationId 國家ID
     * @return 異步返回敵對國家ID列表
     */
    CompletableFuture<List<UUID>> getActiveWars(UUID nationId);
    
    /**
     * 檢查兩國是否處於戰爭狀態
     * 
     * @param nationId1 國家1 ID
     * @param nationId2 國家2 ID
     * @return 異步返回是否處於戰爭狀態
     */
    CompletableFuture<Boolean> isAtWar(UUID nationId1, UUID nationId2);
    
    // === 統計和排行 ===
    
    /**
     * 獲取國家統計信息
     * 
     * @param nationId 國家ID
     * @return 異步返回統計數據
     */
    CompletableFuture<NationStats> getNationStats(UUID nationId);
    
    /**
     * 獲取國家權力排行榜
     * 
     * @param limit 排行榜條目數量
     * @return 異步返回排序後的國家列表
     */
    CompletableFuture<List<Nation>> getPowerRanking(int limit);
    
    /**
     * 獲取國家財富排行榜
     * 
     * @param limit 排行榜條目數量
     * @return 異步返回排序後的國家列表
     */
    CompletableFuture<List<Nation>> getWealthRanking(int limit);
    
    /**
     * 獲取系統統計信息
     * 
     * @return 異步返回系統統計數據
     */
    CompletableFuture<NationSystemStats> getSystemStats();
    
    // === Discord Bot API (預留接口) ===
    
    /**
     * 獲取用於Discord Bot的國家數據
     * 
     * @param nationId 國家ID
     * @return 異步返回Discord格式的國家數據
     */
    CompletableFuture<DiscordNationData> getDiscordNationData(UUID nationId);
    
    /**
     * 根據Discord用戶ID獲取國家數據
     * 
     * @param discordUserId Discord用戶ID
     * @return 異步返回Discord格式的國家數據
     */
    CompletableFuture<DiscordNationData> getDiscordNationDataByDiscordUser(String discordUserId);
    
    /**
     * 獲取Discord Bot用的國家排行榜
     * 
     * @param rankingType 排行榜類型 (power/wealth/members)
     * @param limit 限制數量
     * @return 異步返回Discord格式的排行榜數據
     */
    CompletableFuture<List<DiscordNationData>> getDiscordRanking(String rankingType, int limit);
    
    /**
     * 啟用國家的Discord整合
     * 
     * @param nationId 國家ID
     * @param enablerPlayerId 啟用者玩家ID
     * @param discordGuildId Discord伺服器ID
     * @param discordRoleId Discord角色ID
     * @return 異步返回是否成功啟用
     */
    CompletableFuture<Boolean> enableDiscordIntegration(UUID nationId, UUID enablerPlayerId, String discordGuildId, String discordRoleId);
    
    /**
     * 處理Discord指令
     * 
     * @param command 指令名稱
     * @param args 指令參數
     * @param discordUserId Discord用戶ID
     * @return 異步返回指令執行結果
     */
    CompletableFuture<String> handleDiscordCommand(String command, String[] args, String discordUserId);
    
    /**
     * 發送Discord Webhook通知
     * 
     * @param eventType 事件類型
     * @param nationId 相關國家ID
     * @param message 通知消息
     * @return 異步返回是否成功發送
     */
    CompletableFuture<Boolean> sendDiscordNotification(String eventType, UUID nationId, String message);
    
    // === 系統管理 ===
    
    /**
     * 重載國家系統配置
     * 
     * @return 異步返回是否成功重載
     */
    CompletableFuture<Boolean> reloadConfig();
    
    /**
     * 執行國家系統維護任務
     * 
     * @return 異步返回維護結果
     */
    CompletableFuture<String> performMaintenance();
    
    /**
     * 系統統計數據內部類
     */
    class NationSystemStats {
        private final int totalNations;
        private final int activeNations;
        private final int totalMembers;
        private final int totalTerritories;
        private final double totalTreasury;
        private final int activeWars;
        private final long averageOnlineTime;
        
        public NationSystemStats(int totalNations, int activeNations, int totalMembers, 
                               int totalTerritories, double totalTreasury, int activeWars, 
                               long averageOnlineTime) {
            this.totalNations = totalNations;
            this.activeNations = activeNations;
            this.totalMembers = totalMembers;
            this.totalTerritories = totalTerritories;
            this.totalTreasury = totalTreasury;
            this.activeWars = activeWars;
            this.averageOnlineTime = averageOnlineTime;
        }
        
        // Getter methods
        public int getTotalNations() { return totalNations; }
        public int getActiveNations() { return activeNations; }
        public int getTotalMembers() { return totalMembers; }
        public int getTotalTerritories() { return totalTerritories; }
        public double getTotalTreasury() { return totalTreasury; }
        public int getActiveWars() { return activeWars; }
        public long getAverageOnlineTime() { return averageOnlineTime; }
    }
}