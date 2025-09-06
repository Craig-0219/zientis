package com.zientis.social.api;

import com.zientis.core.api.ZientisAPI;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.social.data.*;
import com.zientis.social.manager.SocialManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Zientis社交系統API實作
 */
public class ZientisSocialAPIImpl implements ZientisSocialAPI {
    
    private final SocialManager socialManager;
    private ZientisAPI coreAPI;
    private ZientisEconomyAPI economyAPI;
    private ZientisNationsAPI nationsAPI;
    
    public ZientisSocialAPIImpl(SocialManager socialManager) {
        this.socialManager = socialManager;
    }
    
    // === 好友系統 ===
    
    @Override
    public CompletableFuture<Boolean> sendFriendRequest(UUID senderId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getFriendManager().sendFriendRequest(senderId, targetId, "");
        });
    }
    
    @Override
    public CompletableFuture<Boolean> acceptFriendRequest(UUID receiverId, UUID senderId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getFriendManager().acceptFriendRequest(receiverId, senderId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> declineFriendRequest(UUID receiverId, UUID senderId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getFriendManager().declineFriendRequest(receiverId, senderId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> removeFriend(UUID playerId, UUID friendId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getFriendManager().removeFriend(playerId, friendId);
        });
    }
    
    @Override
    public List<Friendship> getFriends(UUID playerId) {
        return socialManager.getFriendManager().getFriends(playerId);
    }
    
    @Override
    public List<FriendRequest> getPendingFriendRequests(UUID playerId) {
        return socialManager.getFriendManager().getPendingRequests(playerId);
    }
    
    @Override
    public boolean areFriends(UUID playerId1, UUID playerId2) {
        return socialManager.getFriendManager().areFriends(playerId1, playerId2);
    }
    
    @Override
    public Map<UUID, Boolean> getFriendsOnlineStatus(UUID playerId) {
        List<Friendship> friends = getFriends(playerId);
        Map<UUID, Boolean> status = new HashMap<>();
        
        for (Friendship friendship : friends) {
            UUID friendId = friendship.getFriendId(playerId);
            Player player = org.bukkit.Bukkit.getPlayer(friendId);
            status.put(friendId, player != null && player.isOnline());
        }
        
        return status;
    }
    
    // === 私人訊息系統 ===
    
    @Override
    public CompletableFuture<Boolean> sendPrivateMessage(UUID senderId, UUID recipientId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getMessageManager().sendMessage(senderId, recipientId, message);
        });
    }
    
    @Override
    public List<PrivateMessage> getConversationHistory(UUID playerId, UUID otherId, int limit) {
        // 暫時返回空列表，實際實作會從MessageManager獲取
        return new ArrayList<>();
    }
    
    @Override
    public List<Conversation> getConversations(UUID playerId) {
        // 暫時返回空列表，實際實作會從MessageManager獲取
        return new ArrayList<>();
    }
    
    @Override
    public CompletableFuture<Boolean> markMessagesAsRead(UUID playerId, UUID senderId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getMessageManager().markAsRead(playerId, senderId);
        });
    }
    
    @Override
    public int getUnreadMessageCount(UUID playerId) {
        return socialManager.getMessageManager().getUnreadCount(playerId);
    }
    
    @Override
    public boolean isPlayerBlocked(UUID playerId, UUID blockedId) {
        return socialManager.getMessageManager().isBlocked(playerId, blockedId);
    }
    
    @Override
    public CompletableFuture<Boolean> blockPlayer(UUID playerId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getMessageManager().blockPlayer(playerId, targetId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> unblockPlayer(UUID playerId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getMessageManager().unblockPlayer(playerId, targetId);
        });
    }
    
    @Override
    public List<UUID> getBlockedPlayers(UUID playerId) {
        return socialManager.getMessageManager().getBlockedPlayers(playerId);
    }
    
    // === 組隊系統 ===
    
    @Override
    public CompletableFuture<Party> createParty(UUID leaderId, String partyName) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().createParty(leaderId, partyName);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> inviteToParty(UUID partyId, UUID inviterId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().inviteToParty(partyId, inviterId, targetId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> acceptPartyInvitation(UUID targetId, UUID partyId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().acceptInvitation(targetId, partyId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> declinePartyInvitation(UUID targetId, UUID partyId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().declineInvitation(targetId, partyId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> leaveParty(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().leaveParty(playerId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> kickFromParty(UUID partyId, UUID kickerId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().kickMember(partyId, kickerId, targetId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> transferPartyLeadership(UUID partyId, UUID currentLeaderId, UUID newLeaderId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().transferLeadership(partyId, currentLeaderId, newLeaderId);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> disbandParty(UUID partyId, UUID leaderId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().disbandParty(partyId, leaderId);
        });
    }
    
    @Override
    public Optional<Party> getPlayerParty(UUID playerId) {
        return socialManager.getPartyManager().getPlayerParty(playerId);
    }
    
    @Override
    public CompletableFuture<Boolean> sendPartyMessage(UUID playerId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.getPartyManager().sendPartyMessage(playerId, message);
        });
    }
    
    @Override
    public List<PartyMessage> getPartyChatHistory(UUID partyId, int limit) {
        return socialManager.getPartyManager().getChatHistory(partyId, limit);
    }
    
    // === 社交狀態管理 ===
    
    @Override
    public CompletableFuture<Boolean> setSocialStatus(UUID playerId, SocialStatus status) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.setPlayerStatus(playerId, status);
        });
    }
    
    @Override
    public SocialStatus getSocialStatus(UUID playerId) {
        return socialManager.getPlayerStatus(playerId);
    }
    
    @Override
    public CompletableFuture<Boolean> setStatusMessage(UUID playerId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.setStatusMessage(playerId, message);
        });
    }
    
    @Override
    public String getStatusMessage(UUID playerId) {
        return socialManager.getStatusMessage(playerId);
    }
    
    // === 社交互動 ===
    
    @Override
    public CompletableFuture<Boolean> sendEmote(UUID senderId, UUID targetId, String emoteType) {
        return CompletableFuture.supplyAsync(() -> {
            // 實作表情互動邏輯
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> sendGift(UUID senderId, UUID recipientId, String giftType, String message) {
        return CompletableFuture.supplyAsync(() -> {
            // 實作禮物系統邏輯
            return true;
        });
    }
    
    @Override
    public List<Gift> getGiftHistory(UUID playerId, int limit) {
        // 暫時返回空列表
        return new ArrayList<>();
    }
    
    @Override
    public CompletableFuture<Boolean> endorsePlayer(UUID endorserId, UUID targetId, String endorsementType) {
        return CompletableFuture.supplyAsync(() -> {
            // 實作點讚系統邏輯
            return true;
        });
    }
    
    @Override
    public Map<String, Integer> getPlayerEndorsements(UUID playerId) {
        // 暫時返回空map
        return new HashMap<>();
    }
    
    // === 社交統計 ===
    
    @Override
    public CompletableFuture<SocialStats> getPlayerSocialStats(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            return socialManager.calculatePlayerStats(playerId);
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> getSystemStats() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalUsers", socialManager.getTotalUsers());
            stats.put("onlineUsers", socialManager.getOnlineUsers());
            stats.put("totalFriendships", socialManager.getFriendManager().getTotalFriendships());
            stats.put("activeParties", socialManager.getPartyManager().getActiveParties());
            stats.put("todayMessages", socialManager.getMessageManager().getTodayMessageCount());
            
            return stats;
        });
    }
    
    @Override
    public List<SocialRanking> getSocialLeaderboard(SocialRanking.RankingType type, int limit) {
        return socialManager.getLeaderboard(type, limit);
    }
    
    // === 事件通知 ===
    
    @Override
    public void notifyFriendsOfStatusChange(UUID playerId, String statusType, String message) {
        socialManager.notifyFriendsOfStatusChange(playerId, statusType, message);
    }
    
    @Override
    public void notifyPartyMembers(UUID partyId, String eventType, String message, UUID excludePlayerId) {
        socialManager.getPartyManager().notifyPartyMembers(partyId, eventType, message, excludePlayerId);
    }
    
    @Override
    public void sendSystemNotification(UUID playerId, String title, String message, String notificationType) {
        socialManager.sendSystemNotification(playerId, title, message, notificationType);
    }
    
    // === Discord整合 ===
    
    @Override
    public CompletableFuture<Boolean> syncDiscordStatus(UUID playerId, String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            // 實作Discord同步邏輯
            return true;
        });
    }
    
    @Override
    public void sendDiscordNotification(String channelType, String eventType, String message) {
        // 實作Discord通知邏輯
    }
    
    // === Setter 方法 ===
    
    public void setCoreAPI(ZientisAPI coreAPI) {
        this.coreAPI = coreAPI;
    }
    
    public void setEconomyAPI(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }
    
    public void setNationsAPI(ZientisNationsAPI nationsAPI) {
        this.nationsAPI = nationsAPI;
    }
}