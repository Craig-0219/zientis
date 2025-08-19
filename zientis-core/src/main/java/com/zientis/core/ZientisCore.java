package com.zientis.core;

import com.zientis.core.config.ConfigService;
import com.zientis.core.config.ZientisConfig;
import com.zientis.core.database.DatabaseConfig;
import com.zientis.core.database.DatabaseManager;
import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceManager;
import com.zientis.core.service.ServiceRegistry;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Zientis核心API
 * 提供統一的核心功能存取入口
 */
public class ZientisCore {
    
    private static ZientisCore instance;
    private final Plugin plugin;
    private final Logger logger;
    
    private ServiceManager serviceManager;
    private ConfigService configService;
    private DatabaseManager databaseManager;
    private DependencyContainer dependencyContainer;
    
    private boolean initialized = false;
    
    private ZientisCore(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * 初始化Zientis核心
     */
    public static CompletableFuture<ZientisCore> initialize(Plugin plugin) {
        return CompletableFuture.supplyAsync(() -> {
            if (instance == null) {
                instance = new ZientisCore(plugin);
                instance.initializeCore();
            }
            return instance;
        });
    }
    
    /**
     * 獲取核心實例
     */
    public static ZientisCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Zientis核心尚未初始化");
        }
        return instance;
    }
    
    /**
     * 執行核心初始化
     */
    private void initializeCore() {
        try {
            logger.info("正在初始化Zientis核心...");
            
            // 1. 初始化依賴注入容器
            dependencyContainer = DependencyContainer.getInstance();
            
            // 2. 初始化服務註冊器
            ServiceRegistry.initialize(plugin);
            serviceManager = new ServiceManager(plugin);
            
            // 3. 初始化配置服務
            configService = new ConfigService(plugin);
            serviceManager.registerAndStartService(configService).join();
            
            // 4. 初始化資料庫管理器
            ZientisConfig databaseConfig = configService.getModuleConfig("database");
            DatabaseConfig dbConfig = databaseConfig.getDatabaseConfig("database");
            
            databaseManager = new DatabaseManager(plugin, dbConfig);
            if (!databaseManager.initialize().join()) {
                throw new RuntimeException("資料庫初始化失敗");
            }
            
            // 5. 註冊核心服務到容器
            registerCoreServices();
            
            // 6. 設置關閉掛鉤
            setupShutdownHook();
            
            initialized = true;
            logger.info("Zientis核心初始化完成！");
            
        } catch (Exception e) {
            logger.severe("Zientis核心初始化失敗: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("核心初始化失敗", e);
        }
    }
    
    /**
     * 註冊核心服務到依賴注入容器
     */
    private void registerCoreServices() {
        // 註冊服務管理器
        serviceManager.registerCoreServices(databaseManager);
        
        // 註冊到依賴注入容器
        dependencyContainer.registerSingleton(ServiceManager.class, serviceManager);
        dependencyContainer.registerSingleton(ConfigService.class, configService);
        dependencyContainer.registerSingleton(DatabaseManager.class, databaseManager);
        dependencyContainer.registerSingleton(DependencyContainer.class, dependencyContainer);
        
        // 註冊命名實例
        dependencyContainer.registerNamed("serviceManager", serviceManager);
        dependencyContainer.registerNamed("configService", configService);
        dependencyContainer.registerNamed("databaseManager", databaseManager);
        
        logger.info("核心服務註冊完成");
    }
    
    /**
     * 設置關閉掛鉤
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("執行系統關閉...");
            shutdown();
        }));
    }
    
    /**
     * 獲取服務管理器
     */
    public ServiceManager getServiceManager() {
        ensureInitialized();
        return serviceManager;
    }
    
    /**
     * 獲取配置服務
     */
    public ConfigService getConfigService() {
        ensureInitialized();
        return configService;
    }
    
    /**
     * 獲取資料庫管理器
     */
    public DatabaseManager getDatabaseManager() {
        ensureInitialized();
        return databaseManager;
    }
    
    /**
     * 獲取依賴注入容器
     */
    public DependencyContainer getDependencyContainer() {
        ensureInitialized();
        return dependencyContainer;
    }
    
    /**
     * 執行依賴注入
     */
    public void inject(Object target) {
        ensureInitialized();
        dependencyContainer.inject(target);
    }
    
    /**
     * 檢查是否為偵錯模式
     */
    public boolean isDebugMode() {
        return configService != null && configService.isDebugMode();
    }
    
    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatus() {
        if (!initialized) {
            return "Zientis核心尚未初始化";
        }
        
        StringBuilder status = new StringBuilder();
        status.append("=== Zientis 系統狀態 ===\n");
        status.append("核心狀態: 已初始化\n");
        status.append("偵錯模式: ").append(isDebugMode() ? "啟用" : "停用").append("\n\n");
        
        // 添加服務狀態
        status.append(serviceManager.getSystemStatus());
        status.append("\n");
        
        // 添加依賴注入容器狀態
        status.append(dependencyContainer.getStatus());
        
        return status.toString();
    }
    
    /**
     * 關閉核心系統
     */
    public void shutdown() {
        if (initialized) {
            logger.info("正在關閉Zientis核心...");
            
            try {
                // 優雅關閉服務管理器
                if (serviceManager != null) {
                    serviceManager.gracefulShutdown().join();
                }
                
                // 關閉資料庫管理器
                if (databaseManager != null) {
                    databaseManager.shutdown();
                }
                
                // 清空依賴注入容器
                if (dependencyContainer != null) {
                    dependencyContainer.clear();
                }
                
                initialized = false;
                logger.info("Zientis核心關閉完成");
                
            } catch (Exception e) {
                logger.severe("Zientis核心關閉時發生錯誤: " + e.getMessage());
            }
        }
    }
    
    /**
     * 檢查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 確保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Zientis核心尚未初始化");
        }
    }
}