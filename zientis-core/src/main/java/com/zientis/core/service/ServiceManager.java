package com.zientis.core.service;

import com.zientis.core.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 服務管理器
 * 提供高階服務管理功能和依賴注入支援
 */
public class ServiceManager {
    
    private final Plugin plugin;
    private final Logger logger;
    private final ServiceRegistry registry;
    
    public ServiceManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.registry = ServiceRegistry.getInstance();
    }
    
    /**
     * 註冊並啟動服務
     */
    public CompletableFuture<Void> registerAndStartService(Service service) {
        return CompletableFuture.runAsync(() -> {
            registry.registerService(service);
        });
    }
    
    /**
     * 批量註冊服務
     */
    public CompletableFuture<Void> registerServices(List<Service> services) {
        return CompletableFuture.runAsync(() -> {
            for (Service service : services) {
                registry.registerService(service);
            }
        });
    }
    
    /**
     * 安全地獲取服務
     */
    public <T extends Service> T getService(String serviceName, Class<T> serviceClass) {
        Service service = registry.getService(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("服務不存在: " + serviceName);
        }
        
        if (!serviceClass.isInstance(service)) {
            throw new IllegalArgumentException("服務類型不匹配: " + serviceName);
        }
        
        return serviceClass.cast(service);
    }
    
    /**
     * 安全地獲取實現
     */
    public <T> T getImplementation(Class<T> interfaceClass) {
        T implementation = registry.getImplementation(interfaceClass);
        if (implementation == null) {
            throw new IllegalArgumentException("實現不存在: " + interfaceClass.getSimpleName());
        }
        return implementation;
    }
    
    /**
     * 註冊核心服務
     */
    public void registerCoreServices(DatabaseManager databaseManager) {
        // 註冊資料庫管理器
        registry.registerImplementation(DatabaseManager.class, databaseManager);
        logger.info("核心服務註冊完成");
    }
    
    /**
     * 檢查所有服務健康狀態
     */
    public CompletableFuture<Boolean> checkServicesHealth() {
        return CompletableFuture.supplyAsync(() -> {
            List<Service> runningServices = registry.getRunningServices();
            
            // 檢查資料庫連接
            try {
                DatabaseManager dbManager = getImplementation(DatabaseManager.class);
                if (!dbManager.isHealthy()) {
                    logger.warning("資料庫連接不健康");
                    return false;
                }
            } catch (Exception e) {
                logger.warning("無法檢查資料庫健康狀態: " + e.getMessage());
                return false;
            }
            
            // 檢查其他服務
            for (Service service : runningServices) {
                if (!service.isRunning()) {
                    logger.warning("服務狀態異常: " + service.getName());
                    return false;
                }
            }
            
            return true;
        });
    }
    
    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatus() {
        StringBuilder status = new StringBuilder();
        status.append(registry.getStatusReport());
        
        // 添加資料庫狀態
        try {
            DatabaseManager dbManager = getImplementation(DatabaseManager.class);
            status.append("\n資料庫狀態: ");
            status.append(dbManager.isHealthy() ? "健康" : "異常");
            status.append("\n");
            status.append(dbManager.getStats().toString());
        } catch (Exception e) {
            status.append("\n資料庫狀態: 無法取得");
        }
        
        return status.toString();
    }
    
    /**
     * 執行優雅關閉
     */
    public CompletableFuture<Void> gracefulShutdown() {
        return CompletableFuture.runAsync(() -> {
            logger.info("開始執行優雅關閉...");
            
            // 等待正在進行的操作完成
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 關閉所有服務
            registry.shutdown();
            
            logger.info("優雅關閉完成");
        });
    }
}