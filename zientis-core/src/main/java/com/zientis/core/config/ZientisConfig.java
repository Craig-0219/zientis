package com.zientis.core.config;

import com.zientis.core.database.DatabaseConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Set;

/**
 * Zientis配置實現類
 * 包裝Bukkit的YamlConfiguration，提供額外功能
 */
public class ZientisConfig implements ConfigSection {
    
    private final YamlConfiguration config;
    
    public ZientisConfig(YamlConfiguration config) {
        this.config = config;
    }
    
    @Override
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    @Override
    public String getString(String path) {
        return config.getString(path);
    }
    
    @Override
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    @Override
    public int getInt(String path) {
        return config.getInt(path);
    }
    
    @Override
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    @Override
    public long getLong(String path) {
        return config.getLong(path);
    }
    
    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    @Override
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    
    @Override
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    @Override
    public double getDouble(String path) {
        return config.getDouble(path);
    }
    
    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
    
    @Override
    public List<Integer> getIntegerList(String path) {
        return config.getIntegerList(path);
    }
    
    @Override
    public ConfigSection getSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        
        YamlConfiguration sectionConfig = new YamlConfiguration();
        for (String key : section.getKeys(true)) {
            sectionConfig.set(key, section.get(key));
        }
        
        return new ZientisConfig(sectionConfig);
    }
    
    @Override
    public void set(String path, Object value) {
        config.set(path, value);
    }
    
    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    @Override
    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }
    
    @Override
    public void remove(String path) {
        config.set(path, null);
    }
    
    /**
     * 獲取底層YamlConfiguration
     */
    public YamlConfiguration getHandle() {
        return config;
    }
    
    /**
     * 從配置建立資料庫配置
     */
    public DatabaseConfig getDatabaseConfig(String sectionPath) {
        ConfigSection dbSection = getSection(sectionPath);
        if (dbSection == null) {
            return new DatabaseConfig();
        }
        
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setHost(dbSection.getString("host", "localhost"))
                .setPort(dbSection.getInt("port", 3306))
                .setDatabase(dbSection.getString("database", "zientis"))
                .setUsername(dbSection.getString("username", "zientis"))
                .setPassword(dbSection.getString("password", ""));
        
        // 連接池設定
        ConfigSection poolSection = dbSection.getSection("pool");
        if (poolSection != null) {
            dbConfig.setMinimumIdle(poolSection.getInt("minimum-idle", 5))
                    .setMaximumPoolSize(poolSection.getInt("maximum-pool-size", 20))
                    .setConnectionTimeout(poolSection.getLong("connection-timeout", 30000))
                    .setIdleTimeout(poolSection.getLong("idle-timeout", 600000))
                    .setMaxLifetime(poolSection.getLong("max-lifetime", 1800000));
        }
        
        return dbConfig;
    }
    
    /**
     * 設定資料庫配置到配置檔案
     */
    public void setDatabaseConfig(String sectionPath, DatabaseConfig dbConfig) {
        set(sectionPath + ".host", dbConfig.getHost());
        set(sectionPath + ".port", dbConfig.getPort());
        set(sectionPath + ".database", dbConfig.getDatabase());
        set(sectionPath + ".username", dbConfig.getUsername());
        set(sectionPath + ".password", dbConfig.getPassword());
        
        set(sectionPath + ".pool.minimum-idle", dbConfig.getMinimumIdle());
        set(sectionPath + ".pool.maximum-pool-size", dbConfig.getMaximumPoolSize());
        set(sectionPath + ".pool.connection-timeout", dbConfig.getConnectionTimeout());
        set(sectionPath + ".pool.idle-timeout", dbConfig.getIdleTimeout());
        set(sectionPath + ".pool.max-lifetime", dbConfig.getMaxLifetime());
    }
}