package com.zientis.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Discord 同步響應數據傳輸對象
 */
public class SyncResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("balances")
    private Map<String, Double> balances;
    
    @JsonProperty("adjustments")
    private Map<String, Double> adjustments;
    
    @JsonProperty("server_info")
    private Map<String, Object> serverInfo;
    
    // 構造函數
    public SyncResponse() {}
    
    public SyncResponse(String status, String message, String timestamp, 
                       Map<String, Double> balances, Map<String, Double> adjustments) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.balances = balances;
        this.adjustments = adjustments;
    }
    
    public SyncResponse(String status, String message, String timestamp, 
                       Map<String, Double> balances, Map<String, Double> adjustments,
                       Map<String, Object> serverInfo) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.balances = balances;
        this.adjustments = adjustments;
        this.serverInfo = serverInfo;
    }
    
    // Getter 和 Setter
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Double> getBalances() {
        return balances;
    }
    
    public void setBalances(Map<String, Double> balances) {
        this.balances = balances;
    }
    
    public Map<String, Double> getAdjustments() {
        return adjustments;
    }
    
    public void setAdjustments(Map<String, Double> adjustments) {
        this.adjustments = adjustments;
    }
    
    public Map<String, Object> getServerInfo() {
        return serverInfo;
    }
    
    public void setServerInfo(Map<String, Object> serverInfo) {
        this.serverInfo = serverInfo;
    }
    
    @Override
    public String toString() {
        return "SyncResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", balances=" + balances +
                ", adjustments=" + adjustments +
                '}';
    }
}