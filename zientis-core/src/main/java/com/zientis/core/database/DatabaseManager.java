package com.zientis.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 統一的資料庫連接管理器
 * 提供高性能的連接池和異步操作支援
 */
public class DatabaseManager {
    
    private final Plugin plugin;
    private final Logger logger;
    private HikariDataSource dataSource;
    private final DatabaseConfig config;
    
    public DatabaseManager(Plugin plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
    }
    
    /**
     * 初始化資料庫連接池
     */
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在初始化資料庫連接池...");
                
                // 臨時跳過數據庫初始化以測試插件載入
                logger.warning("暫時跳過資料庫初始化 - 僅供測試使用");
                return true;
                
                /*
                HikariConfig hikariConfig = new HikariConfig();
                
                // 基本連接設定
                hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", 
                    config.getHost(), config.getPort(), config.getDatabase()));
                hikariConfig.setUsername(config.getUsername());
                hikariConfig.setPassword(config.getPassword());
                hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
                
                // 連接池設定
                hikariConfig.setMinimumIdle(config.getMinimumIdle());
                hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
                hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
                hikariConfig.setIdleTimeout(config.getIdleTimeout());
                hikariConfig.setMaxLifetime(config.getMaxLifetime());
                
                // 效能優化設定
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
                
                // 連接驗證
                hikariConfig.setConnectionTestQuery("SELECT 1");
                hikariConfig.setValidationTimeout(3000);
                
                dataSource = new HikariDataSource(hikariConfig);
                
                // 測試連接
                try (Connection connection = dataSource.getConnection()) {
                    logger.info("資料庫連接池初始化成功！");
                    return true;
                }
                */
                
            } catch (Exception e) {
                logger.severe("資料庫連接池初始化失敗: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    /**
     * 獲取資料庫連接
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("資料庫連接池未初始化或已關閉");
        }
        return dataSource.getConnection();
    }
    
    /**
     * 執行異步查詢操作
     */
    public <T> CompletableFuture<T> executeQuery(DatabaseQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                return query.execute(connection);
            } catch (SQLException e) {
                logger.severe("資料庫查詢執行失敗: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 執行異步更新操作
     */
    public CompletableFuture<Integer> executeUpdate(DatabaseUpdate update) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                return update.execute(connection);
            } catch (SQLException e) {
                logger.severe("資料庫更新執行失敗: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 執行事務操作
     */
    public <T> CompletableFuture<T> executeTransaction(DatabaseTransaction<T> transaction) {
        return CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            try {
                connection = getConnection();
                connection.setAutoCommit(false);
                
                T result = transaction.execute(connection);
                
                connection.commit();
                return result;
                
            } catch (Exception e) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.severe("事務回滾失敗: " + rollbackEx.getMessage());
                    }
                }
                logger.severe("事務執行失敗: " + e.getMessage());
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        logger.severe("連接關閉失敗: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * 獲取連接池統計資訊
     */
    public DatabaseStats getStats() {
        if (dataSource == null) {
            return new DatabaseStats();
        }
        
        return new DatabaseStats(
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * 關閉資料庫連接池
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("正在關閉資料庫連接池...");
            dataSource.close();
            logger.info("資料庫連接池已關閉");
        }
    }
    
    /**
     * 檢查資料庫連接是否健康
     */
    public boolean isHealthy() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}