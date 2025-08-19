package com.zientis.core.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 資料庫操作實用工具類
 * 提供常用的資料庫操作輔助方法
 */
public class DatabaseUtils {
    
    private static final Logger logger = Logger.getLogger(DatabaseUtils.class.getName());
    
    /**
     * 安全地設置 UUID 參數
     */
    public static void setUUID(PreparedStatement stmt, int parameterIndex, UUID uuid) throws SQLException {
        if (uuid != null) {
            stmt.setString(parameterIndex, uuid.toString());
        } else {
            stmt.setNull(parameterIndex, java.sql.Types.VARCHAR);
        }
    }
    
    /**
     * 安全地獲取 UUID 結果
     */
    public static UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        String uuidString = rs.getString(columnName);
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }
    
    /**
     * 安全地設置時間戳參數
     */
    public static void setLocalDateTime(PreparedStatement stmt, int parameterIndex, LocalDateTime dateTime) throws SQLException {
        if (dateTime != null) {
            stmt.setTimestamp(parameterIndex, Timestamp.valueOf(dateTime));
        } else {
            stmt.setNull(parameterIndex, java.sql.Types.TIMESTAMP);
        }
    }
    
    /**
     * 安全地獲取時間戳結果
     */
    public static LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    /**
     * 建立分頁查詢的 LIMIT 和 OFFSET 子句
     */
    public static String buildPaginationClause(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        
        int offset = (page - 1) * pageSize;
        return String.format(" LIMIT %d OFFSET %d", pageSize, offset);
    }
    
    /**
     * 記錄資料庫操作執行時間
     */
    public static void logExecutionTime(String operation, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        if (executionTime > 1000) {
            logger.warning(String.format("資料庫操作 '%s' 執行時間過長: %dms", operation, executionTime));
        } else {
            logger.fine(String.format("資料庫操作 '%s' 執行完成: %dms", operation, executionTime));
        }
    }
    
    /**
     * 安全地關閉資料庫資源
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    logger.warning("關閉資料庫資源時發生錯誤: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 驗證表名是否安全（防止 SQL 注入）
     */
    public static boolean isValidTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }
        // 只允許字母、數字和底線
        return tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
    
    /**
     * 驗證欄位名是否安全（防止 SQL 注入）
     */
    public static boolean isValidColumnName(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return false;
        }
        // 只允許字母、數字和底線
        return columnName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}