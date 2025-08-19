package com.zientis.core.database;

/**
 * 資料庫連接池統計資訊
 * 提供連接池的健康狀態和效能指標
 */
public class DatabaseStats {
    
    private final int totalConnections;
    private final int activeConnections;
    private final int idleConnections;
    private final int waitingThreads;
    
    public DatabaseStats() {
        this(0, 0, 0, 0);
    }
    
    public DatabaseStats(int totalConnections, int activeConnections, int idleConnections, int waitingThreads) {
        this.totalConnections = totalConnections;
        this.activeConnections = activeConnections;
        this.idleConnections = idleConnections;
        this.waitingThreads = waitingThreads;
    }
    
    /**
     * 獲取總連接數
     */
    public int getTotalConnections() {
        return totalConnections;
    }
    
    /**
     * 獲取活躍連接數
     */
    public int getActiveConnections() {
        return activeConnections;
    }
    
    /**
     * 獲取閒置連接數
     */
    public int getIdleConnections() {
        return idleConnections;
    }
    
    /**
     * 獲取等待連接的線程數
     */
    public int getWaitingThreads() {
        return waitingThreads;
    }
    
    /**
     * 計算連接池使用率
     */
    public double getUsagePercentage() {
        if (totalConnections == 0) {
            return 0.0;
        }
        return (double) activeConnections / totalConnections * 100.0;
    }
    
    /**
     * 檢查連接池是否健康
     */
    public boolean isHealthy() {
        // 如果使用率超過90%或有過多等待線程，認為不健康
        return getUsagePercentage() < 90.0 && waitingThreads < 5;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DatabaseStats{總連接數=%d, 活躍連接數=%d, 閒置連接數=%d, 等待線程數=%d, 使用率=%.1f%%}",
            totalConnections, activeConnections, idleConnections, waitingThreads, getUsagePercentage()
        );
    }
}