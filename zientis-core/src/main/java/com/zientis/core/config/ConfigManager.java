package com.zientis.core.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 配置管理器
 * 管理所有模組的配置檔案
 */
public class ConfigManager {
    
    private final Plugin plugin;
    private final Logger logger;
    private final File configDir;
    private final ConcurrentHashMap<String, YamlConfiguration> configs;
    private final ConcurrentHashMap<String, File> configFiles;
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configDir = new File(plugin.getDataFolder(), "config");
        this.configs = new ConcurrentHashMap<>();
        this.configFiles = new ConcurrentHashMap<>();
        
        // 建立配置目錄
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }
    
    /**
     * 載入配置檔案
     */
    public ZientisConfig loadConfig(String name) {
        try {
            File configFile = new File(configDir, name + ".yml");
            
            // 如果檔案不存在，嘗試從資源複製
            if (!configFile.exists()) {
                plugin.saveResource("config/" + name + ".yml", false);
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            configs.put(name, config);
            configFiles.put(name, configFile);
            
            logger.info("配置檔案載入成功: " + name + ".yml");
            return new ZientisConfig(config);
            
        } catch (Exception e) {
            logger.severe("配置檔案載入失敗: " + name + ".yml - " + e.getMessage());
            return new ZientisConfig(new YamlConfiguration());
        }
    }
    
    /**
     * 獲取配置
     */
    public ZientisConfig getConfig(String name) {
        YamlConfiguration config = configs.get(name);
        if (config == null) {
            return loadConfig(name);
        }
        return new ZientisConfig(config);
    }
    
    /**
     * 儲存配置檔案
     */
    public void saveConfig(String name) {
        try {
            YamlConfiguration config = configs.get(name);
            File configFile = configFiles.get(name);
            
            if (config != null && configFile != null) {
                config.save(configFile);
                logger.info("配置檔案儲存成功: " + name + ".yml");
            } else {
                logger.warning("配置檔案不存在，無法儲存: " + name + ".yml");
            }
        } catch (IOException e) {
            logger.severe("配置檔案儲存失敗: " + name + ".yml - " + e.getMessage());
        }
    }
    
    /**
     * 重新載入配置檔案
     */
    public ZientisConfig reloadConfig(String name) {
        try {
            File configFile = configFiles.get(name);
            if (configFile != null && configFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                configs.put(name, config);
                logger.info("配置檔案重新載入成功: " + name + ".yml");
                return new ZientisConfig(config);
            } else {
                logger.warning("配置檔案不存在，無法重新載入: " + name + ".yml");
                return loadConfig(name);
            }
        } catch (Exception e) {
            logger.severe("配置檔案重新載入失敗: " + name + ".yml - " + e.getMessage());
            return new ZientisConfig(new YamlConfiguration());
        }
    }
    
    /**
     * 儲存所有配置檔案
     */
    public void saveAll() {
        for (String name : configs.keySet()) {
            saveConfig(name);
        }
    }
    
    /**
     * 重新載入所有配置檔案
     */
    public void reloadAll() {
        for (String name : configs.keySet()) {
            reloadConfig(name);
        }
    }
    
    /**
     * 檢查配置檔案是否存在
     */
    public boolean hasConfig(String name) {
        return configs.containsKey(name);
    }
    
    /**
     * 建立預設配置檔案
     */
    public void createDefaultConfig(String name, String content) {
        try {
            File configFile = new File(configDir, name + ".yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
                java.nio.file.Files.write(configFile.toPath(), content.getBytes("UTF-8"));
                logger.info("預設配置檔案建立成功: " + name + ".yml");
            }
        } catch (IOException e) {
            logger.severe("預設配置檔案建立失敗: " + name + ".yml - " + e.getMessage());
        }
    }
}