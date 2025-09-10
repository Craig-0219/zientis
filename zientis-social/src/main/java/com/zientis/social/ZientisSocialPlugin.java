package com.zientis.social;

import com.zientis.core.api.ZientisAPI;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.social.api.ZientisSocialAPI;
import com.zientis.social.api.ZientisSocialAPIImpl;
import com.zientis.social.commands.FriendCommand;
import com.zientis.social.commands.MessageCommand;
import com.zientis.social.commands.PartyCommand;
import com.zientis.social.listeners.SocialEventListener;
import com.zientis.social.manager.FriendManager;
import com.zientis.social.manager.MessageManager;
import com.zientis.social.manager.PartyManager;
import com.zientis.social.manager.SocialManager;
import com.zientis.social.tasks.SocialMaintenanceTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 賽恩堤斯社交系統主插件
 * 
 * 負責管理社交功能，包括：
 * - 好友系統
 * - 私人訊息
 * - 組隊系統
 * - 社交互動
 * - Discord整合
 */
public class ZientisSocialPlugin extends JavaPlugin {
    
    private static final Logger logger = Logger.getLogger(ZientisSocialPlugin.class.getName());
    
    // API實例
    private ZientisSocialAPIImpl socialAPI;
    
    // 外部依賴
    private ZientisAPI coreAPI;
    private ZientisEconomyAPI economyAPI;
    private ZientisNationsAPI nationsAPI;
    
    // 核心管理器
    private SocialManager socialManager;
    private FriendManager friendManager;
    private MessageManager messageManager;
    private PartyManager partyManager;
    
    // 定時任務
    private SocialMaintenanceTask maintenanceTask;

    @Override
    public void onEnable() {
        logger.info("正在啟動 Zientis 社交系統...");
        
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
            
            logger.info("Zientis 社交系統已成功啟動！");
            
        } catch (Exception e) {
            logger.severe("啟動 Zientis 社交系統失敗: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("正在關閉 Zientis 社交系統...");
        
        try {
            // 停止定時任務
            if (maintenanceTask != null) {
                maintenanceTask.cancel();
            }
            
            // 執行最終維護
            if (socialManager != null) {
                performFinalMaintenance();
            }
            
            logger.info("Zientis 社交系統已安全關閉");
            
        } catch (Exception e) {
            logger.severe("關閉社交系統時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 載入配置文件
     */
    private void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        
        logger.info("社交系統配置已載入");
    }

    /**
     * 初始化核心組件
     */
    private void initializeComponents() {
        // 初始化各種管理器
        friendManager = new FriendManager(this);
        messageManager = new MessageManager(this);
        partyManager = new PartyManager(this);
        socialManager = new SocialManager(this, friendManager, messageManager, partyManager);
        
        // 初始化 API 實現
        socialAPI = new ZientisSocialAPIImpl(socialManager);
        
        logger.info("社交系統核心組件已初始化");
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
            socialAPI.setCoreAPI(coreAPI);
            logger.info("已連接到 Zientis 核心系統");
        } else {
            logger.warning("未找到 Zientis 核心系統");
        }
        
        // 獲取經濟 API
        var economyRegistration = getServer().getServicesManager()
            .getRegistration(ZientisEconomyAPI.class);
        economyAPI = economyRegistration != null ? economyRegistration.getProvider() : null;
        if (economyAPI != null) {
            socialAPI.setEconomyAPI(economyAPI);
            logger.info("已連接到 Zientis 經濟系統");
        } else {
            logger.warning("未找到 Zientis 經濟系統");
        }
        
        // 獲取國家 API
        var nationsRegistration = getServer().getServicesManager()
            .getRegistration(ZientisNationsAPI.class);
        nationsAPI = nationsRegistration != null ? nationsRegistration.getProvider() : null;
        if (nationsAPI != null) {
            socialAPI.setNationsAPI(nationsAPI);
            logger.info("已連接到 Zientis 國家系統");
        } else {
            logger.warning("未找到 Zientis 國家系統");
        }
    }

    /**
     * 註冊指令和事件監聽器
     */
    private void registerCommandsAndListeners() {
        // 註冊好友指令
        FriendCommand friendCommand = new FriendCommand(socialAPI);
        getCommand("friend").setExecutor(friendCommand);
        getCommand("friend").setTabCompleter(friendCommand);
        getCommand("f").setExecutor(friendCommand);
        getCommand("f").setTabCompleter(friendCommand);
        
        // 註冊訊息指令
        MessageCommand messageCommand = new MessageCommand(socialAPI);
        getCommand("message").setExecutor(messageCommand);
        getCommand("message").setTabCompleter(messageCommand);
        getCommand("msg").setExecutor(messageCommand);
        getCommand("msg").setTabCompleter(messageCommand);
        getCommand("tell").setExecutor(messageCommand);
        getCommand("tell").setTabCompleter(messageCommand);
        getCommand("reply").setExecutor(messageCommand);
        getCommand("r").setExecutor(messageCommand);
        
        // 註冊組隊指令
        PartyCommand partyCommand = new PartyCommand(socialAPI);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);
        getCommand("p").setExecutor(partyCommand);
        getCommand("p").setTabCompleter(partyCommand);
        
        // 註冊事件監聽器
        SocialEventListener eventListener = new SocialEventListener(socialAPI);
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        logger.info("已註冊指令和事件監聽器");
    }

    /**
     * 啟動定時任務
     */
    private void startScheduledTasks() {
        // 啟動維護任務 (每30分鐘執行一次)
        maintenanceTask = new SocialMaintenanceTask(socialAPI);
        maintenanceTask.runTaskTimerAsynchronously(this, 20L * 60 * 30, 20L * 60 * 30);
        
        logger.info("已啟動定時維護任務");
    }

    /**
     * 註冊 API 服務
     */
    private void registerAPIService() {
        getServer().getServicesManager().register(
            ZientisSocialAPI.class, 
            socialAPI, 
            this, 
            org.bukkit.plugin.ServicePriority.High
        );
        
        logger.info("已註冊社交系統 API 服務");
    }

    /**
     * 執行最終維護
     */
    private void performFinalMaintenance() {
        try {
            logger.info("正在執行最終維護...");
            
            // 保存所有社交數據
            socialManager.saveAllData();
            
            // 清理緩存
            socialManager.clearCache();
            
            logger.info("最終維護完成");
            
        } catch (Exception e) {
            logger.severe("執行最終維護時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 重載插件配置
     */
    public void reloadSocialSystem() {
        logger.info("重載社交系統配置...");
        
        try {
            // 重新載入配置
            reloadConfig();
            
            // 重新設置依賴
            setupDependencies();
            
            // 重新載入社交數據
            socialManager.reloadData();
            
            logger.info("社交系統配置重載完成");
            
        } catch (Exception e) {
            logger.severe("重載配置失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatusReport() {
        if (socialAPI == null) {
            return "社交系統未初始化";
        }
        
        try {
            var stats = socialAPI.getSystemStats().join();
            
            return String.format(
                "=== Zientis 社交系統狀態 ===\n" +
                "總用戶數: %d\n" +
                "在線用戶數: %d\n" +
                "總好友關係: %d\n" +
                "活躍組隊: %d\n" +
                "今日訊息數: %d\n" +
                "核心系統連接: %s\n" +
                "經濟系統連接: %s\n" +
                "國家系統連接: %s",
                stats.get("totalUsers"),
                stats.get("onlineUsers"),
                stats.get("totalFriendships"),
                stats.get("activeParties"),
                stats.get("todayMessages"),
                coreAPI != null ? "已連接" : "未連接",
                economyAPI != null ? "已連接" : "未連接",
                nationsAPI != null ? "已連接" : "未連接"
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
    public void sendDiscordNotification(String eventType, String playerName, String message) {
        if (!isDiscordIntegrationAvailable()) {
            return;
        }
        
        // 這裡會實現實際的Discord Webhook調用
        logger.info(String.format("Discord通知: [%s] %s - %s", eventType, playerName, message));
    }

    // === Getter 方法 ===
    
    /**
     * 獲取社交系統 API
     */
    public ZientisSocialAPI getSocialAPI() {
        return socialAPI;
    }
    
    /**
     * 獲取社交管理器
     */
    public SocialManager getSocialManager() {
        return socialManager;
    }
    
    /**
     * 獲取好友管理器
     */
    public FriendManager getFriendManager() {
        return friendManager;
    }
    
    /**
     * 獲取訊息管理器
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    /**
     * 獲取組隊管理器
     */
    public PartyManager getPartyManager() {
        return partyManager;
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
     * 獲取國家 API
     */
    public ZientisNationsAPI getNationsAPI() {
        return nationsAPI;
    }
}