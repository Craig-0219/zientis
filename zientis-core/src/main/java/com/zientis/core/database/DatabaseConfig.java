package com.zientis.core.database;

/**
 * 資料庫配置類
 * 包含所有資料庫連接和連接池的配置參數
 */
public class DatabaseConfig {
    
    private String host = "localhost";
    private int port = 3306;
    private String database = "zientis";
    private String username = "zientis";
    private String password = "";
    
    // 連接池配置
    private int minimumIdle = 5;
    private int maximumPoolSize = 20;
    private long connectionTimeout = 30000; // 30秒
    private long idleTimeout = 600000; // 10分鐘
    private long maxLifetime = 1800000; // 30分鐘
    
    public DatabaseConfig() {}
    
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public String getHost() {
        return host;
    }
    
    public DatabaseConfig setHost(String host) {
        this.host = host;
        return this;
    }
    
    public int getPort() {
        return port;
    }
    
    public DatabaseConfig setPort(int port) {
        this.port = port;
        return this;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public DatabaseConfig setDatabase(String database) {
        this.database = database;
        return this;
    }
    
    public String getUsername() {
        return username;
    }
    
    public DatabaseConfig setUsername(String username) {
        this.username = username;
        return this;
    }
    
    public String getPassword() {
        return password;
    }
    
    public DatabaseConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public int getMinimumIdle() {
        return minimumIdle;
    }
    
    public DatabaseConfig setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
        return this;
    }
    
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }
    
    public DatabaseConfig setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public DatabaseConfig setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    
    public long getIdleTimeout() {
        return idleTimeout;
    }
    
    public DatabaseConfig setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }
    
    public long getMaxLifetime() {
        return maxLifetime;
    }
    
    public DatabaseConfig setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', username='%s', poolSize=%d}",
            host, port, database, username, maximumPoolSize);
    }
}