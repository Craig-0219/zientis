package com.zientis.display;

import com.zientis.core.api.ZientisAPI;
import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.display.api.ZientisDisplayAPIImpl;
import com.zientis.display.commands.DisplayCommand;
import com.zientis.display.listeners.DisplayInteractionListener;
import com.zientis.display.tasks.DisplayUpdateTask;

import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 賽恩堤斯島嶼展示系統主插件
 * 
 * 負責初始化和管理整個島嶼展示系統
 */
public class ZientisDisplayPlugin extends JavaPlugin {
    
    private static final Logger logger = Logger.getLogger(ZientisDisplayPlugin.class.getName());
    
    // API實例
    private ZientisDisplayAPIImpl displayAPI;
    
    // 外部依賴
    private ZientisAPI coreAPI;
    private ZientisMultiWorldAPI multiWorldAPI;
    
    // 定時任務
    private DisplayUpdateTask updateTask;

    @Override
    public void onEnable() {
        logger.info("正在啟動 Zientis 島嶼展示系統...");
        
        try {
            // 1. 初始化 API
            initializeAPI();
            
            // 2. 設置外部依賴
            setupDependencies();
            
            // 3. 註冊指令和事件監聽器
            registerCommandsAndListeners();
            
            // 4. 啟動定時任務
            startScheduledTasks();
            
            // 5. 註冊 API 服務
            registerAPIService();
            
            logger.info("Zientis 島嶼展示系統已成功啟動！");
            
        } catch (Exception e) {
            logger.severe("啟動 Zientis 島嶼展示系統失敗: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("正在關閉 Zientis 島嶼展示系統...");
        
        try {
            // 停止定時任務
            if (updateTask != null) {
                updateTask.stop();
            }
            
            // 清理所有展示
            if (displayAPI != null) {
                cleanupAllDisplays();
            }
            
            logger.info("Zientis 島嶼展示系統已安全關閉");
            
        } catch (Exception e) {
            logger.severe("關閉展示系統時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化 API
     */
    private void initializeAPI() {
        displayAPI = new ZientisDisplayAPIImpl();
        logger.info("展示系統 API 已初始化");
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
            displayAPI.setCoreAPI(coreAPI);
            logger.info("已連接到 Zientis 核心系統");
        } else {
            logger.warning("未找到 Zientis 核心系統");
        }
        
        // 獲取多世界 API
        var multiWorldRegistration = getServer().getServicesManager()
            .getRegistration(ZientisMultiWorldAPI.class);
        multiWorldAPI = multiWorldRegistration != null ? multiWorldRegistration.getProvider() : null;
        if (multiWorldAPI != null) {
            displayAPI.setMultiWorldAPI(multiWorldAPI);
            logger.info("已連接到 Zientis 多世界系統");
        } else {
            logger.warning("未找到 Zientis 多世界系統");
        }
        
        // 設置主世界
        World mainWorld = getServer().getWorld("world");
        if (mainWorld != null) {
            displayAPI.setMainWorld(mainWorld);
            logger.info("已設置主世界: " + mainWorld.getName());
        } else {
            logger.warning("未找到主世界");
        }
    }

    /**
     * 註冊指令和事件監聽器
     */
    private void registerCommandsAndListeners() {
        // 註冊指令
        DisplayCommand displayCommand = new DisplayCommand(displayAPI);
        getCommand("display").setExecutor(displayCommand);
        getCommand("display").setTabCompleter(displayCommand);
        
        // 註冊事件監聽器
        DisplayInteractionListener interactionListener = new DisplayInteractionListener(displayAPI);
        getServer().getPluginManager().registerEvents(interactionListener, this);
        
        logger.info("已註冊指令和事件監聽器");
    }

    /**
     * 啟動定時任務
     */
    private void startScheduledTasks() {
        updateTask = new DisplayUpdateTask(displayAPI);
        
        // 每30秒執行一次更新檢查
        updateTask.runTaskTimerAsynchronously(this, 20L * 30, 20L * 30);
        
        logger.info("已啟動定時更新任務");
    }

    /**
     * 註冊 API 服務
     */
    private void registerAPIService() {
        getServer().getServicesManager().register(
            ZientisDisplayAPI.class, 
            displayAPI, 
            this, 
            org.bukkit.plugin.ServicePriority.High
        );
        
        logger.info("已註冊展示系統 API 服務");
    }

    /**
     * 清理所有展示
     */
    private void cleanupAllDisplays() {
        try {
            ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
            logger.info("正在清理 " + stats.getTotalDisplays() + " 個展示...");
            
            // 移除所有展示
            displayAPI.getAllDisplays().forEach(model -> {
                try {
                    displayAPI.removeDisplay(model.getIslandId()).join();
                } catch (Exception e) {
                    logger.warning("清理展示失敗: " + model.getIslandId() + " - " + e.getMessage());
                }
            });
            
            logger.info("展示清理完成");
            
        } catch (Exception e) {
            logger.severe("清理展示時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 獲取展示系統 API
     */
    public ZientisDisplayAPI getDisplayAPI() {
        return displayAPI;
    }

    /**
     * 重載插件配置
     */
    public void reloadDisplaySystem() {
        logger.info("重載展示系統配置...");
        
        try {
            // 重新載入配置
            reloadConfig();
            
            // 重新設置依賴
            setupDependencies();
            
            logger.info("展示系統配置重載完成");
            
        } catch (Exception e) {
            logger.severe("重載配置失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatusReport() {
        if (displayAPI == null) {
            return "展示系統未初始化";
        }
        
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        
        return String.format(
            "=== Zientis 島嶼展示系統狀態 ===\n" +
            "總展示數量: %d\n" +
            "活躍展示: %d\n" +
            "記憶體使用: %d MB\n" +
            "平均更新時間: %.2f ms\n" +
            "核心系統連接: %s\n" +
            "多世界系統連接: %s\n" +
            "定時任務狀態: %s",
            stats.getTotalDisplays(),
            stats.getActiveDisplays(),
            stats.getMemoryUsage() / 1024 / 1024,
            stats.getAverageUpdateTime(),
            coreAPI != null ? "已連接" : "未連接",
            multiWorldAPI != null ? "已連接" : "未連接",
            updateTask != null && !updateTask.isCancelled() ? "運行中" : "已停止"
        );
    }
}