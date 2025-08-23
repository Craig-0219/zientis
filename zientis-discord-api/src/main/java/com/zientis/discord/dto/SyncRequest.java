package com.zientis.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Discord 同步請求數據傳輸對象
 */
public class SyncRequest {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("guild_id")
    private String guildId;
    
    @JsonProperty("balances")
    private Map<String, Integer> balances;
    
    @JsonProperty("sync_type")
    private String syncType;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("signature")
    private String signature;
    
    // 構造函數
    public SyncRequest() {}
    
    public SyncRequest(String userId, String guildId, Map<String, Integer> balances, 
                      String syncType, String timestamp, String signature) {
        this.userId = userId;
        this.guildId = guildId;
        this.balances = balances;
        this.syncType = syncType;
        this.timestamp = timestamp;
        this.signature = signature;
    }
    
    // Getter 和 Setter
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
    
    public Map<String, Integer> getBalances() {
        return balances;
    }
    
    public void setBalances(Map<String, Integer> balances) {
        this.balances = balances;
    }
    
    public String getSyncType() {
        return syncType;
    }
    
    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    @Override
    public String toString() {
        return "SyncRequest{" +
                "userId='" + userId + '\'' +
                ", guildId='" + guildId + '\'' +
                ", balances=" + balances +
                ", syncType='" + syncType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}