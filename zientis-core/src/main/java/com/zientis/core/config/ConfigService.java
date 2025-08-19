package com.zientis.core.config;

import com.zientis.core.service.AbstractService;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置服務
 * 提供統一的配置管理功能
 */
public class ConfigService extends AbstractService {
    
    private ConfigManager configManager;
    private final ConcurrentHashMap<String, ZientisConfig> moduleConfigs;
    
    public ConfigService(Plugin plugin) {
        super(plugin, "ConfigService", "1.0.0");
        this.moduleConfigs = new ConcurrentHashMap<>();
    }
    
    @Override
    protected void onInitialize() throws Exception {
        configManager = new ConfigManager(plugin);
        
        // 載入核心配置
        loadCoreConfigs();
        
        logger.info("配置服務初始化完成");
    }
    
    @Override
    protected void onShutdown() throws Exception {
        // 儲存所有配置
        if (configManager != null) {
            configManager.saveAll();
        }
        
        moduleConfigs.clear();
        logger.info("配置服務關閉完成");
    }
    
    /**
     * 載入核心配置檔案
     */
    private void loadCoreConfigs() {
        // 載入主配置
        ZientisConfig mainConfig = configManager.loadConfig("main");
        moduleConfigs.put("main", mainConfig);
        
        // 載入資料庫配置
        ZientisConfig databaseConfig = configManager.loadConfig("database");
        moduleConfigs.put("database", databaseConfig);
        
        // 建立預設配置檔案（如果不存在）
        createDefaultConfigs();
    }
    
    /**
     * 建立預設配置檔案
     */
    private void createDefaultConfigs() {
        // 主配置
        String mainConfigContent = """
# Zientis 主配置檔案
server:
  name: "Zientis Server"
  version: "0.1.0-ALPHA"
  debug: false
  
features:
  economy: true
  multiworld: true
  social: false
  nations: false
  
locale:
  default: "zh_TW"
  supported:
    - "zh_TW"
    - "en_US"
""";
        configManager.createDefaultConfig("main", mainConfigContent);
        
        // 資料庫配置
        String databaseConfigContent = """
# Zientis 資料庫配置檔案
database:
  host: "localhost"
  port: 3306
  database: "zientis"
  username: "zientis_dev"
  password: "dev_password_2024"
  
  pool:
    minimum-idle: 5
    maximum-pool-size: 20
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000
""";
        configManager.createDefaultConfig("database", databaseConfigContent);
    }
    
    /**
     * 獲取模組配置
     */
    public ZientisConfig getModuleConfig(String moduleName) {
        ensureInitialized();
        
        ZientisConfig config = moduleConfigs.get(moduleName);
        if (config == null) {
            config = configManager.loadConfig(moduleName);
            moduleConfigs.put(moduleName, config);
        }
        return config;
    }
    
    /**
     * 重新載入模組配置
     */
    public ZientisConfig reloadModuleConfig(String moduleName) {
        ensureInitialized();
        
        ZientisConfig config = configManager.reloadConfig(moduleName);
        moduleConfigs.put(moduleName, config);
        return config;
    }
    
    /**
     * 儲存模組配置
     */
    public void saveModuleConfig(String moduleName) {
        ensureInitialized();
        configManager.saveConfig(moduleName);
    }
    
    /**
     * 檢查功能是否啟用
     */
    public boolean isFeatureEnabled(String featureName) {
        ZientisConfig mainConfig = getModuleConfig("main");
        return mainConfig.getBoolean("features." + featureName, false);
    }
    
    /**
     * 獲取伺服器名稱
     */
    public String getServerName() {
        ZientisConfig mainConfig = getModuleConfig("main");
        return mainConfig.getString("server.name", "Zientis Server");
    }
    
    /**
     * 檢查是否為偵錯模式
     */
    public boolean isDebugMode() {
        ZientisConfig mainConfig = getModuleConfig("main");
        return mainConfig.getBoolean("server.debug", false);
    }
    
    /**
     * 獲取預設語言
     */
    public String getDefaultLocale() {
        ZientisConfig mainConfig = getModuleConfig("main");
        return mainConfig.getString("locale.default", "zh_TW");
    }
    
    /**
     * 重新載入所有配置
     */
    public void reloadAll() {
        ensureInitialized();
        
        configManager.reloadAll();
        moduleConfigs.clear();
        loadCoreConfigs();
        
        logger.info("所有配置已重新載入");
    }
    
    /**
     * 儲存所有配置
     */
    public void saveAll() {
        ensureInitialized();
        configManager.saveAll();
    }
}