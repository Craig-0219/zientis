package com.zientis.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 資料庫事務操作接口
 * 用於定義需要事務保證的複雜操作
 */
@FunctionalInterface
public interface DatabaseTransaction<T> {
    
    /**
     * 在事務中執行操作
     * 
     * @param connection 已開啟事務的資料庫連接
     * @return 操作結果
     * @throws SQLException 資料庫操作異常
     */
    T execute(Connection connection) throws SQLException;
}