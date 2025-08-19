package com.zientis.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 資料庫查詢操作接口
 * 用於定義可執行的查詢操作
 */
@FunctionalInterface
public interface DatabaseQuery<T> {
    
    /**
     * 執行查詢操作
     * 
     * @param connection 資料庫連接
     * @return 查詢結果
     * @throws SQLException 資料庫操作異常
     */
    T execute(Connection connection) throws SQLException;
}