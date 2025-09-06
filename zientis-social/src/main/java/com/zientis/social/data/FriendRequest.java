package com.zientis.social.data;

import java.util.UUID;

/**
 * 好友請求數據類
 */
public class FriendRequest {
    
    private final UUID id;
    private final UUID senderId;
    private final UUID recipientId;
    private final long sentTime;
    private final String message;
    private FriendRequestStatus status;
    private long processedTime;
    
    public FriendRequest(UUID senderId, UUID recipientId, String message) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message != null ? message : "";
        this.sentTime = System.currentTimeMillis();
        this.status = FriendRequestStatus.PENDING;
        this.processedTime = 0;
    }
    
    public FriendRequest(UUID id, UUID senderId, UUID recipientId, String message, 
                        long sentTime, FriendRequestStatus status, long processedTime) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.sentTime = sentTime;
        this.status = status;
        this.processedTime = processedTime;
    }
    
    /**
     * 接受好友請求
     */
    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
        this.processedTime = System.currentTimeMillis();
    }
    
    /**
     * 拒絕好友請求
     */
    public void decline() {
        this.status = FriendRequestStatus.DECLINED;
        this.processedTime = System.currentTimeMillis();
    }
    
    /**
     * 撤回好友請求
     */
    public void withdraw() {
        this.status = FriendRequestStatus.WITHDRAWN;
        this.processedTime = System.currentTimeMillis();
    }
    
    /**
     * 檢查請求是否過期（7天未處理）
     */
    public boolean isExpired() {
        if (status != FriendRequestStatus.PENDING) {
            return false;
        }
        
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        return sentTime < sevenDaysAgo;
    }
    
    /**
     * 獲取請求存在時間（小時）
     */
    public long getAgeInHours() {
        return (System.currentTimeMillis() - sentTime) / (60 * 60 * 1000);
    }
    
    /**
     * 檢查是否可以處理
     */
    public boolean canBeProcessed() {
        return status == FriendRequestStatus.PENDING && !isExpired();
    }
    
    // === Getters ===
    
    public UUID getId() {
        return id;
    }
    
    public UUID getSenderId() {
        return senderId;
    }
    
    public UUID getRecipientId() {
        return recipientId;
    }
    
    public long getSentTime() {
        return sentTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public FriendRequestStatus getStatus() {
        return status;
    }
    
    public long getProcessedTime() {
        return processedTime;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FriendRequest that = (FriendRequest) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("FriendRequest{id=%s, sender=%s, recipient=%s, status=%s}", 
            id, senderId, recipientId, status);
    }
    
    /**
     * 好友請求狀態枚舉
     */
    public enum FriendRequestStatus {
        PENDING("待處理"),
        ACCEPTED("已接受"),
        DECLINED("已拒絕"),
        WITHDRAWN("已撤回"),
        EXPIRED("已過期");
        
        private final String displayName;
        
        FriendRequestStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}