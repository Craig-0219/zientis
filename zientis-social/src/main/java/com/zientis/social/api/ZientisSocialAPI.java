package com.zientis.social.api;

import com.zientis.social.data.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Zientis社交系統API介面
 */
public interface ZientisSocialAPI {
    
    // === 好友系統 ===
    
    /**
     * 發送好友請求
     */
    CompletableFuture<Boolean> sendFriendRequest(UUID senderId, UUID targetId);
    
    /**
     * 接受好友請求
     */
    CompletableFuture<Boolean> acceptFriendRequest(UUID receiverId, UUID senderId);
    
    /**
     * 拒絕好友請求
     */
    CompletableFuture<Boolean> declineFriendRequest(UUID receiverId, UUID senderId);
    
    /**
     * 移除好友
     */
    CompletableFuture<Boolean> removeFriend(UUID playerId, UUID friendId);
    
    /**
     * 獲取好友列表
     */
    List<Friendship> getFriends(UUID playerId);
    
    /**
     * 獲取待處理的好友請求
     */
    List<FriendRequest> getPendingFriendRequests(UUID playerId);
    
    /**
     * 檢查兩個玩家是否為好友
     */
    boolean areFriends(UUID playerId1, UUID playerId2);
    
    /**
     * 獲取好友的在線狀態
     */
    Map<UUID, Boolean> getFriendsOnlineStatus(UUID playerId);
    
    // === 私人訊息系統 ===
    
    /**
     * 發送私人訊息
     */
    CompletableFuture<Boolean> sendPrivateMessage(UUID senderId, UUID recipientId, String message);
    
    /**
     * 獲取與指定玩家的對話記錄
     */
    List<PrivateMessage> getConversationHistory(UUID playerId, UUID otherId, int limit);
    
    /**
     * 獲取所有對話列表
     */
    List<Conversation> getConversations(UUID playerId);
    
    /**
     * 標記訊息為已讀
     */
    CompletableFuture<Boolean> markMessagesAsRead(UUID playerId, UUID senderId);
    
    /**
     * 獲取未讀訊息數量
     */
    int getUnreadMessageCount(UUID playerId);
    
    /**
     * 檢查玩家是否屏蔽了另一個玩家
     */
    boolean isPlayerBlocked(UUID playerId, UUID blockedId);
    
    /**
     * 屏蔽玩家
     */
    CompletableFuture<Boolean> blockPlayer(UUID playerId, UUID targetId);
    
    /**
     * 解除屏蔽玩家
     */
    CompletableFuture<Boolean> unblockPlayer(UUID playerId, UUID targetId);
    
    /**
     * 獲取屏蔽列表
     */
    List<UUID> getBlockedPlayers(UUID playerId);
    
    // === 組隊系統 ===
    
    /**
     * 創建組隊
     */
    CompletableFuture<Party> createParty(UUID leaderId, String partyName);
    
    /**
     * 邀請玩家加入組隊
     */
    CompletableFuture<Boolean> inviteToParty(UUID partyId, UUID inviterId, UUID targetId);
    
    /**
     * 接受組隊邀請
     */
    CompletableFuture<Boolean> acceptPartyInvitation(UUID targetId, UUID partyId);
    
    /**
     * 拒絕組隊邀請
     */
    CompletableFuture<Boolean> declinePartyInvitation(UUID targetId, UUID partyId);
    
    /**
     * 離開組隊
     */
    CompletableFuture<Boolean> leaveParty(UUID playerId);
    
    /**
     * 踢出組隊成員
     */
    CompletableFuture<Boolean> kickFromParty(UUID partyId, UUID kickerId, UUID targetId);
    
    /**
     * 轉讓隊長
     */
    CompletableFuture<Boolean> transferPartyLeadership(UUID partyId, UUID currentLeaderId, UUID newLeaderId);
    
    /**
     * 解散組隊
     */
    CompletableFuture<Boolean> disbandParty(UUID partyId, UUID leaderId);
    
    /**
     * 獲取玩家的組隊
     */
    Optional<Party> getPlayerParty(UUID playerId);
    
    /**
     * 發送組隊訊息
     */
    CompletableFuture<Boolean> sendPartyMessage(UUID playerId, String message);
    
    /**
     * 獲取組隊聊天記錄
     */
    List<PartyMessage> getPartyChatHistory(UUID partyId, int limit);
    
    // === 社交狀態管理 ===
    
    /**
     * 設置玩家社交狀態
     */
    CompletableFuture<Boolean> setSocialStatus(UUID playerId, SocialStatus status);
    
    /**
     * 獲取玩家社交狀態
     */
    SocialStatus getSocialStatus(UUID playerId);
    
    /**
     * 設置玩家個人狀態訊息
     */
    CompletableFuture<Boolean> setStatusMessage(UUID playerId, String message);
    
    /**
     * 獲取玩家個人狀態訊息
     */
    String getStatusMessage(UUID playerId);
    
    // === 社交互動 ===
    
    /**
     * 發送表情符號反應
     */
    CompletableFuture<Boolean> sendEmote(UUID senderId, UUID targetId, String emoteType);
    
    /**
     * 發送禮物
     */
    CompletableFuture<Boolean> sendGift(UUID senderId, UUID recipientId, String giftType, String message);
    
    /**
     * 獲取禮物記錄
     */
    List<Gift> getGiftHistory(UUID playerId, int limit);
    
    /**
     * 點讚玩家
     */
    CompletableFuture<Boolean> endorsePlayer(UUID endorserId, UUID targetId, String endorsementType);
    
    /**
     * 獲取玩家的讚數
     */
    Map<String, Integer> getPlayerEndorsements(UUID playerId);
    
    // === 社交統計 ===
    
    /**
     * 獲取玩家社交統計
     */
    CompletableFuture<SocialStats> getPlayerSocialStats(UUID playerId);
    
    /**
     * 獲取系統統計
     */
    CompletableFuture<Map<String, Object>> getSystemStats();
    
    /**
     * 獲取社交排行榜
     */
    List<SocialRanking> getSocialLeaderboard(SocialRanking.RankingType type, int limit);
    
    // === 事件通知 ===
    
    /**
     * 向好友發送狀態更新
     */
    void notifyFriendsOfStatusChange(UUID playerId, String statusType, String message);
    
    /**
     * 向組隊成員發送通知
     */
    void notifyPartyMembers(UUID partyId, String eventType, String message, UUID excludePlayerId);
    
    /**
     * 發送系統通知
     */
    void sendSystemNotification(UUID playerId, String title, String message, String notificationType);
    
    // === Discord整合 ===
    
    /**
     * 同步Discord狀態
     */
    CompletableFuture<Boolean> syncDiscordStatus(UUID playerId, String discordUserId);
    
    /**
     * 發送Discord通知
     */
    void sendDiscordNotification(String channelType, String eventType, String message);
}