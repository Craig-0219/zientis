package com.zientis.nations;

import com.zientis.core.api.ZientisAPI;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.nations.api.ZientisNationsAPIImpl;
import com.zientis.nations.commands.NationCommand;
import com.zientis.nations.listeners.NationEventListener;
import com.zientis.nations.manager.NationManager;
import com.zientis.nations.tasks.NationMaintenanceTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 賽恩堤斯國家系統主插件
 * 
 * 負責初始化和管理整個國家系統，包括：
 * - 國家創建與管理
 * - 成員和角色系統
 * - 外交和戰爭機制
 * - Discord Bot API整合
 */
public class ZientisNationsPlugin extends JavaPlugin {
    
    private static final Logger logger = Logger.getLogger(ZientisNationsPlugin.class.getName());
    
    // API實例
    private ZientisNationsAPIImpl nationsAPI;
    
    // 外部依賴
    private ZientisAPI coreAPI;
    private ZientisEconomyAPI economyAPI;
    private ZientisMultiWorldAPI multiWorldAPI;
    
    // 核心管理器
    private NationManager nationManager;
    
    // 定時任務
    private NationMaintenanceTask maintenanceTask;

    @Override
    public void onEnable() {
        logger.info("正在啟動 Zientis 國家系統...");
        
        try {
            // 1. 載入配置
            loadConfiguration();
            
            // 2. 初始化核心組件
            initializeComponents();
            
            // 3. 設置外部依賴
            setupDependencies();
            
            // 4. 註冊指令和事件監聽器
            registerCommandsAndListeners();
            
            // 5. 啟動定時任務
            startScheduledTasks();
            
            // 6. 註冊 API 服務
            registerAPIService();
            
            logger.info("Zientis 國家系統已成功啟動！");
            
        } catch (Exception e) {
            logger.severe("啟動 Zientis 國家系統失敗: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("正在關閉 Zientis 國家系統...");
        
        try {
            // 停止定時任務
            if (maintenanceTask != null) {
                maintenanceTask.cancel();
            }
            
            // 執行最終維護
            if (nationManager != null) {
                performFinalMaintenance();
            }
            
            logger.info("Zientis 國家系統已安全關閉");
            
        } catch (Exception e) {
            logger.severe("關閉國家系統時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 載入配置文件
     */
    private void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        
        logger.info("國家系統配置已載入");
    }

    /**
     * 初始化核心組件
     */
    private void initializeComponents() {
        // 初始化國家管理器
        nationManager = new NationManager(this);
        
        // 初始化 API 實現
        nationsAPI = new ZientisNationsAPIImpl(nationManager);
        
        logger.info("國家系統核心組件已初始化");
    }

    /**
     * 設置外部依賴
     */
    private void setupDependencies() {
        // 獲取核心 API
        var coreRegistration = getServer().getServicesManager()
            .getRegistration(ZientisAPI.class);
        coreAPI = coreRegistration != null ? coreRegistration.getProvider() : null;
        if (coreAPI != null) {
            nationsAPI.setCoreAPI(coreAPI);
            logger.info("已連接到 Zientis 核心系統");
        } else {
            logger.warning("未找到 Zientis 核心系統");
        }
        
        // 獲取經濟 API
        var economyRegistration = getServer().getServicesManager()
            .getRegistration(ZientisEconomyAPI.class);
        economyAPI = economyRegistration != null ? economyRegistration.getProvider() : null;
        if (economyAPI != null) {
            nationsAPI.setEconomyAPI(economyAPI);
            logger.info("已連接到 Zientis 經濟系統");
        } else {
            logger.warning("未找到 Zientis 經濟系統");
        }
        
        // 獲取多世界 API
        var multiWorldRegistration = getServer().getServicesManager()
            .getRegistration(ZientisMultiWorldAPI.class);
        multiWorldAPI = multiWorldRegistration != null ? multiWorldRegistration.getProvider() : null;
        if (multiWorldAPI != null) {
            nationsAPI.setMultiWorldAPI(multiWorldAPI);
            logger.info("已連接到 Zientis 多世界系統");
        } else {
            logger.warning("未找到 Zientis 多世界系統");
        }
    }

    /**
     * 註冊指令和事件監聽器
     */
    private void registerCommandsAndListeners() {
        // 註冊主指令
        NationCommand nationCommand = new NationCommand(nationsAPI);
        getCommand("nation").setExecutor(nationCommand);
        getCommand("nation").setTabCompleter(nationCommand);
        
        // 註冊簡化指令
        getCommand("n").setExecutor(nationCommand);
        getCommand("n").setTabCompleter(nationCommand);
        
        // 註冊事件監聽器
        NationEventListener eventListener = new NationEventListener(nationsAPI);
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        logger.info("已註冊指令和事件監聽器");
    }

    /**
     * 啟動定時任務
     */
    private void startScheduledTasks() {
        // 啟動維護任務 (每小時執行一次)
        maintenanceTask = new NationMaintenanceTask(nationsAPI);
        maintenanceTask.runTaskTimerAsynchronously(this, 20L * 60 * 60, 20L * 60 * 60);
        
        logger.info("已啟動定時維護任務");
    }

    /**
     * 註冊 API 服務
     */
    private void registerAPIService() {
        getServer().getServicesManager().register(
            ZientisNationsAPI.class, 
            nationsAPI, 
            this, 
            org.bukkit.plugin.ServicePriority.High
        );
        
        logger.info("已註冊國家系統 API 服務");
    }

    /**
     * 執行最終維護
     */
    private void performFinalMaintenance() {
        try {
            logger.info("正在執行最終維護...");
            
            // 保存所有國家數據
            nationManager.saveAllNations();
            
            // 清理緩存
            nationManager.clearCache();
            
            logger.info("最終維護完成");
            
        } catch (Exception e) {
            logger.severe("執行最終維護時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 重載插件配置
     */
    public void reloadNationSystem() {
        logger.info("重載國家系統配置...");
        
        try {
            // 重新載入配置
            reloadConfig();
            
            // 重新設置依賴
            setupDependencies();
            
            // 重新載入國家數據
            nationManager.reloadNations();
            
            logger.info("國家系統配置重載完成");
            
        } catch (Exception e) {
            logger.severe("重載配置失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatusReport() {
        if (nationsAPI == null) {
            return "國家系統未初始化";
        }
        
        try {
            var stats = nationsAPI.getSystemStats().join();
            
            return String.format(
                "=== Zientis 國家系統狀態 ===\n" +
                "總國家數量: %d\n" +
                "活躍國家: %d\n" +
                "總成員數: %d\n" +
                "總領土數: %d\n" +
                "總國庫: %.2f\n" +
                "活躍戰爭: %d\n" +
                "核心系統連接: %s\n" +
                "經濟系統連接: %s\n" +
                "多世界系統連接: %s",
                stats.getTotalNations(),
                stats.getActiveNations(),
                stats.getTotalMembers(),
                stats.getTotalTerritories(),
                stats.getTotalTreasury(),
                stats.getActiveWars(),
                coreAPI != null ? "已連接" : "未連接",
                economyAPI != null ? "已連接" : "未連接",
                multiWorldAPI != null ? "已連接" : "未連接"
            );
            
        } catch (Exception e) {
            return "獲取狀態報告時發生錯誤: " + e.getMessage();
        }
    }

    /**
     * 檢查Discord整合狀態
     */
    public boolean isDiscordIntegrationAvailable() {
        return getConfig().getBoolean("discord.enabled", false);
    }

    /**
     * 獲取Discord Webhook URL
     */
    public String getDiscordWebhookUrl() {
        return getConfig().getString("discord.webhook-url", "");
    }

    /**
     * 執行Discord通知
     */
    public void sendDiscordNotification(String eventType, String nationName, String message) {
        if (!isDiscordIntegrationAvailable()) {
            return;
        }
        
        // 這裡會實現實際的Discord Webhook調用
        logger.info(String.format("Discord通知: [%s] %s - %s", eventType, nationName, message));
    }

    // === Getter 方法 ===
    
    /**
     * 獲取國家系統 API
     */
    public ZientisNationsAPI getNationsAPI() {
        return nationsAPI;
    }
    
    /**
     * 獲取國家管理器
     */
    public NationManager getNationManager() {
        return nationManager;
    }
    
    /**
     * 獲取核心 API
     */
    public ZientisAPI getCoreAPI() {
        return coreAPI;
    }
    
    /**
     * 獲取經濟 API
     */
    public ZientisEconomyAPI getEconomyAPI() {
        return economyAPI;
    }
    
    /**
     * 獲取多世界 API
     */
    public ZientisMultiWorldAPI getMultiWorldAPI() {
        return multiWorldAPI;
    }
}