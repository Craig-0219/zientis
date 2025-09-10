package com.zientis.social.data;

import java.util.UUID;

/**
 * 好友關係數據類
 */
public class Friendship {
    
    private final UUID player1Id;
    private final UUID player2Id;
    private final long establishedTime;
    private int interactionCount;
    private long lastInteractionTime;
    private FriendshipLevel level;
    
    public Friendship(UUID player1Id, UUID player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.establishedTime = System.currentTimeMillis();
        this.interactionCount = 0;
        this.lastInteractionTime = System.currentTimeMillis();
        this.level = FriendshipLevel.NORMAL;
    }
    
    public Friendship(UUID player1Id, UUID player2Id, long establishedTime, 
                     int interactionCount, long lastInteractionTime, FriendshipLevel level) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.establishedTime = establishedTime;
        this.interactionCount = interactionCount;
        this.lastInteractionTime = lastInteractionTime;
        this.level = level;
    }
    
    /**
     * 獲取好友ID（相對於指定玩家）
     */
    public UUID getFriendId(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player2Id;
        } else if (player2Id.equals(playerId)) {
            return player1Id;
        }
        throw new IllegalArgumentException("指定的玩家不在此好友關係中");
    }
    
    /**
     * 檢查是否包含指定玩家
     */
    public boolean contains(UUID playerId) {
        return player1Id.equals(playerId) || player2Id.equals(playerId);
    }
    
    /**
     * 增加互動次數
     */
    public void addInteraction() {
        this.interactionCount++;
        this.lastInteractionTime = System.currentTimeMillis();
        updateFriendshipLevel();
    }
    
    /**
     * 根據互動次數更新好友等級
     */
    private void updateFriendshipLevel() {
        if (interactionCount >= 1000) {
            level = FriendshipLevel.BEST_FRIEND;
        } else if (interactionCount >= 500) {
            level = FriendshipLevel.CLOSE_FRIEND;
        } else if (interactionCount >= 100) {
            level = FriendshipLevel.GOOD_FRIEND;
        } else {
            level = FriendshipLevel.NORMAL;
        }
    }
    
    /**
     * 獲取好友關係持續天數
     */
    public long getFriendshipDays() {
        return (System.currentTimeMillis() - establishedTime) / (24 * 60 * 60 * 1000);
    }
    
    /**
     * 檢查是否為活躍好友（最近30天有互動）
     */
    public boolean isActiveFriendship() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        return lastInteractionTime >= thirtyDaysAgo;
    }
    
    // === Getters 和 Setters ===
    
    public UUID getPlayer1Id() {
        return player1Id;
    }
    
    public UUID getPlayer2Id() {
        return player2Id;
    }
    
    public long getEstablishedTime() {
        return establishedTime;
    }
    
    public int getInteractionCount() {
        return interactionCount;
    }
    
    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
        updateFriendshipLevel();
    }
    
    public long getLastInteractionTime() {
        return lastInteractionTime;
    }
    
    public void setLastInteractionTime(long lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }
    
    public FriendshipLevel getLevel() {
        return level;
    }
    
    public void setLevel(FriendshipLevel level) {
        this.level = level;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Friendship that = (Friendship) obj;
        return (player1Id.equals(that.player1Id) && player2Id.equals(that.player2Id)) ||
               (player1Id.equals(that.player2Id) && player2Id.equals(that.player1Id));
    }
    
    @Override
    public int hashCode() {
        // 確保無論順序如何，hash值都相同
        return player1Id.hashCode() + player2Id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Friendship{player1=%s, player2=%s, level=%s, interactions=%d}", 
            player1Id, player2Id, level, interactionCount);
    }
    
    /**
     * 好友等級枚舉
     */
    public enum FriendshipLevel {
        NORMAL("普通好友", 0),
        GOOD_FRIEND("好朋友", 100),
        CLOSE_FRIEND("親密好友", 500),
        BEST_FRIEND("摯友", 1000);
        
        private final String displayName;
        private final int requiredInteractions;
        
        FriendshipLevel(String displayName, int requiredInteractions) {
            this.displayName = displayName;
            this.requiredInteractions = requiredInteractions;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getRequiredInteractions() {
            return requiredInteractions;
        }
        
        public static FriendshipLevel fromInteractionCount(int interactions) {
            if (interactions >= BEST_FRIEND.requiredInteractions) {
                return BEST_FRIEND;
            } else if (interactions >= CLOSE_FRIEND.requiredInteractions) {
                return CLOSE_FRIEND;
            } else if (interactions >= GOOD_FRIEND.requiredInteractions) {
                return GOOD_FRIEND;
            } else {
                return NORMAL;
            }
        }
    }
}