package com.zientis.core.service;

import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 服務註冊器
 * 管理所有Zientis模組的服務生命週期
 */
public class ServiceRegistry {
    
    private static ServiceRegistry instance;
    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, Service> services;
    private final Map<Class<?>, Object> implementations;
    private final AtomicBoolean isShuttingDown;
    
    private ServiceRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.services = new ConcurrentHashMap<>();
        this.implementations = new ConcurrentHashMap<>();
        this.isShuttingDown = new AtomicBoolean(false);
    }
    
    /**
     * 初始化服務註冊器
     */
    public static void initialize(Plugin plugin) {
        if (instance == null) {
            instance = new ServiceRegistry(plugin);
            instance.logger.info("服務註冊器已初始化");
        }
    }
    
    /**
     * 獲取服務註冊器實例
     */
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            throw new IllegalStateException("服務註冊器尚未初始化");
        }
        return instance;
    }
    
    /**
     * 註冊服務
     */
    public void registerService(Service service) {
        if (isShuttingDown.get()) {
            logger.warning("系統正在關閉，無法註冊新服務: " + service.getName());
            return;
        }
        
        String serviceName = service.getName();
        if (services.containsKey(serviceName)) {
            logger.warning("服務已存在，將覆蓋現有服務: " + serviceName);
            Service existingService = services.get(serviceName);
            if (existingService.isRunning()) {
                existingService.shutdown();
            }
        }
        
        try {
            service.initialize();
            services.put(serviceName, service);
            logger.info("服務註冊成功: " + serviceName + " v" + service.getVersion());
        } catch (Exception e) {
            logger.severe("服務註冊失敗: " + serviceName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 註冊服務實現
     */
    public <T> void registerImplementation(Class<T> interfaceClass, T implementation) {
        implementations.put(interfaceClass, implementation);
        logger.info("服務實現已註冊: " + interfaceClass.getSimpleName());
    }
    
    /**
     * 獲取服務
     */
    public Service getService(String serviceName) {
        return services.get(serviceName);
    }
    
    /**
     * 獲取服務實現
     */
    @SuppressWarnings("unchecked")
    public <T> T getImplementation(Class<T> interfaceClass) {
        return (T) implementations.get(interfaceClass);
    }
    
    /**
     * 檢查服務是否存在
     */
    public boolean hasService(String serviceName) {
        return services.containsKey(serviceName);
    }
    
    /**
     * 檢查實現是否存在
     */
    public boolean hasImplementation(Class<?> interfaceClass) {
        return implementations.containsKey(interfaceClass);
    }
    
    /**
     * 取消註冊服務
     */
    public void unregisterService(String serviceName) {
        Service service = services.remove(serviceName);
        if (service != null) {
            try {
                if (service.isRunning()) {
                    service.shutdown();
                }
                logger.info("服務已取消註冊: " + serviceName);
            } catch (Exception e) {
                logger.severe("服務關閉失敗: " + serviceName + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * 取消註冊實現
     */
    public void unregisterImplementation(Class<?> interfaceClass) {
        implementations.remove(interfaceClass);
        logger.info("服務實現已取消註冊: " + interfaceClass.getSimpleName());
    }
    
    /**
     * 獲取所有服務名稱
     */
    public Set<String> getServiceNames() {
        return new HashSet<>(services.keySet());
    }
    
    /**
     * 獲取所有正在運行的服務
     */
    public List<Service> getRunningServices() {
        return services.values().stream()
                .filter(Service::isRunning)
                .toList();
    }
    
    /**
     * 獲取服務狀態報告
     */
    public String getStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Zientis 服務狀態報告 ===\n");
        report.append("總服務數: ").append(services.size()).append("\n");
        report.append("運行中服務數: ").append(getRunningServices().size()).append("\n");
        report.append("註冊實現數: ").append(implementations.size()).append("\n\n");
        
        report.append("服務詳情:\n");
        for (Service service : services.values()) {
            report.append(String.format("  %s v%s - %s\n", 
                service.getName(), 
                service.getVersion(), 
                service.getStatus()
            ));
        }
        
        return report.toString();
    }
    
    /**
     * 關閉所有服務
     */
    public void shutdown() {
        if (isShuttingDown.compareAndSet(false, true)) {
            logger.info("開始關閉所有服務...");
            
            // 先關閉所有服務
            List<Service> servicesToShutdown = new ArrayList<>(services.values());
            for (Service service : servicesToShutdown) {
                try {
                    if (service.isRunning()) {
                        logger.info("正在關閉服務: " + service.getName());
                        service.shutdown();
                    }
                } catch (Exception e) {
                    logger.severe("服務關閉時發生錯誤: " + service.getName() + " - " + e.getMessage());
                }
            }
            
            // 清空註冊資料
            services.clear();
            implementations.clear();
            
            logger.info("所有服務已關閉");
        }
    }
}