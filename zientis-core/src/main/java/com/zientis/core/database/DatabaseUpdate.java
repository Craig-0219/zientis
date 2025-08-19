package com.zientis.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 資料庫更新操作接口
 * 用於定義INSERT、UPDATE、DELETE等修改操作
 */
@FunctionalInterface
public interface DatabaseUpdate {
    
    /**
     * 執行更新操作
     * 
     * @param connection 資料庫連接
     * @return 受影響的行數
     * @throws SQLException 資料庫操作異常
     */
    int execute(Connection connection) throws SQLException;
}