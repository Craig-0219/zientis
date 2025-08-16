package com.zientis.discord;

import com.zientis.discord.api.ZientisDiscordAPI;
import com.zientis.discord.api.ZientisDiscordAPIImpl;
import com.zientis.discord.auth.DiscordAuthManager;
import com.zientis.discord.bot.ZientisDiscordBot;
import com.zientis.discord.cache.DiscordCacheManager;
import com.zientis.discord.security.DiscordSecurityManager;
import com.zientis.discord.webhook.DiscordWebhookManager;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.nations.api.ZientisNationsAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 賽恩堤斯 Discord API 主插件
 * 
 * 統一管理所有系統與Discord Bot的整合
 * 提供完整的Discord指令、Webhook通知和數據查詢功能
 */
public class ZientisDiscordApiPlugin extends JavaPlugin {
    
    private static final Logger logger = Logger.getLogger(ZientisDiscordApiPlugin.class.getName());
    
    // API實例
    private ZientisDiscordAPIImpl discordAPI;
    
    // 核心管理器
    private ZientisDiscordBot discordBot;
    private DiscordAuthManager authManager;
    private DiscordWebhookManager webhookManager;
    private DiscordSecurityManager securityManager;
    private DiscordCacheManager cacheManager;
    
    // 外部API依賴
    private ZientisEconomyAPI economyAPI;
    private ZientisMultiWorldAPI multiWorldAPI;
    private ZientisDisplayAPI displayAPI;
    private ZientisNationsAPI nationsAPI;

    @Override
    public void onEnable() {
        logger.info("正在啟動 Zientis Discord API...");
        
        try {
            // 1. 載入配置
            loadConfiguration();
            
            // 2. 檢查依賴
            if (!checkDependencies()) {
                logger.severe("缺少必要的Zientis系統依賴，插件將被停用");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // 3. 初始化核心組件
            initializeComponents();
            
            // 4. 設置外部依賴
            setupDependencies();
            
            // 5. 啟動Discord Bot
            if (getConfig().getBoolean("discord.bot.enabled", false)) {
                startDiscordBot();
            }
            
            // 6. 註冊API服務
            registerAPIService();
            
            logger.info("Zientis Discord API 已成功啟動！");
            
        } catch (Exception e) {
            logger.severe("啟動 Zientis Discord API 失敗: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("正在關閉 Zientis Discord API...");
        
        try {
            // 關閉Discord Bot
            if (discordBot != null) {
                discordBot.shutdown();
            }
            
            // 清理緩存
            if (cacheManager != null) {
                cacheManager.shutdown();
            }
            
            logger.info("Zientis Discord API 已安全關閉");
            
        } catch (Exception e) {
            logger.severe("關閉Discord API時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 載入配置文件
     */
    private void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        
        logger.info("Discord API 配置已載入");
    }

    /**
     * 檢查依賴系統
     */
    private boolean checkDependencies() {
        boolean hasEconomy = getServer().getPluginManager().getPlugin("ZientisEconomy") != null;
        boolean hasMultiWorld = getServer().getPluginManager().getPlugin("ZientisMultiWorld") != null;
        boolean hasDisplay = getServer().getPluginManager().getPlugin("ZientisDisplay") != null;
        boolean hasNations = getServer().getPluginManager().getPlugin("ZientisNations") != null;
        
        logger.info("依賴檢查結果:");
        logger.info("- 經濟系統: " + (hasEconomy ? "✓" : "✗"));
        logger.info("- 多世界系統: " + (hasMultiWorld ? "✓" : "✗"));
        logger.info("- 展示系統: " + (hasDisplay ? "✓" : "✗"));
        logger.info("- 國家系統: " + (hasNations ? "✓" : "✗"));
        
        // 至少需要一個系統
        return hasEconomy || hasMultiWorld || hasDisplay || hasNations;
    }

    /**
     * 初始化核心組件
     */
    private void initializeComponents() {
        // 初始化管理器
        authManager = new DiscordAuthManager(this);
        webhookManager = new DiscordWebhookManager(this);
        securityManager = new DiscordSecurityManager(this);
        cacheManager = new DiscordCacheManager(this);
        
        // 初始化API實現
        discordAPI = new ZientisDiscordAPIImpl(this, authManager, webhookManager, 
                                              securityManager, cacheManager);
        
        logger.info("Discord API 核心組件已初始化");
    }

    /**
     * 設置外部依賴
     */
    private void setupDependencies() {
        // 獲取經濟API
        var economyRegistration = getServer().getServicesManager()
            .getRegistration(ZientisEconomyAPI.class);
        economyAPI = economyRegistration != null ? economyRegistration.getProvider() : null;
        if (economyAPI != null) {
            discordAPI.setEconomyAPI(economyAPI);
            logger.info("已連接到 Zientis 經濟系統");
        }
        
        // 獲取多世界API
        var multiWorldRegistration = getServer().getServicesManager()
            .getRegistration(ZientisMultiWorldAPI.class);
        multiWorldAPI = multiWorldRegistration != null ? multiWorldRegistration.getProvider() : null;
        if (multiWorldAPI != null) {
            discordAPI.setMultiWorldAPI(multiWorldAPI);
            logger.info("已連接到 Zientis 多世界系統");
        }
        
        // 獲取展示API
        var displayRegistration = getServer().getServicesManager()
            .getRegistration(ZientisDisplayAPI.class);
        displayAPI = displayRegistration != null ? displayRegistration.getProvider() : null;
        if (displayAPI != null) {
            discordAPI.setDisplayAPI(displayAPI);
            logger.info("已連接到 Zientis 展示系統");
        }
        
        // 獲取國家API
        var nationsRegistration = getServer().getServicesManager()
            .getRegistration(ZientisNationsAPI.class);
        nationsAPI = nationsRegistration != null ? nationsRegistration.getProvider() : null;
        if (nationsAPI != null) {
            discordAPI.setNationsAPI(nationsAPI);
            logger.info("已連接到 Zientis 國家系統");
        }
    }

    /**
     * 啟動Discord Bot
     */
    private void startDiscordBot() {
        String botToken = getConfig().getString("discord.bot.token");
        if (botToken == null || botToken.isEmpty()) {
            logger.warning("Discord Bot Token 未配置，跳過Bot啟動");
            return;
        }
        
        try {
            discordBot = new ZientisDiscordBot(this, discordAPI);
            discordBot.start(botToken);
            
            logger.info("Discord Bot 已啟動");
            
        } catch (Exception e) {
            logger.severe("啟動Discord Bot失敗: " + e.getMessage());
        }
    }

    /**
     * 註冊API服務
     */
    private void registerAPIService() {
        getServer().getServicesManager().register(
            ZientisDiscordAPI.class, 
            discordAPI, 
            this, 
            org.bukkit.plugin.ServicePriority.High
        );
        
        logger.info("已註冊Discord API服務");
    }

    /**
     * 重載插件配置
     */
    public void reloadDiscordAPI() {
        logger.info("重載Discord API配置...");
        
        try {
            // 重新載入配置
            reloadConfig();
            
            // 重新設置依賴
            setupDependencies();
            
            // 重啟Discord Bot (如果已啟用)
            if (getConfig().getBoolean("discord.bot.enabled", false)) {
                if (discordBot != null) {
                    discordBot.shutdown();
                }
                startDiscordBot();
            }
            
            logger.info("Discord API配置重載完成");
            
        } catch (Exception e) {
            logger.severe("重載配置失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取系統狀態報告
     */
    public String getSystemStatusReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=== Zientis Discord API 狀態 ===\n");
        report.append("Discord Bot: ").append(discordBot != null && discordBot.isConnected() ? "✓ 已連接" : "✗ 未連接").append("\n");
        report.append("認證管理器: ").append(authManager != null ? "✓ 已初始化" : "✗ 未初始化").append("\n");
        report.append("Webhook管理器: ").append(webhookManager != null ? "✓ 已初始化" : "✗ 未初始化").append("\n");
        report.append("安全管理器: ").append(securityManager != null ? "✓ 已初始化" : "✗ 未初始化").append("\n");
        report.append("緩存管理器: ").append(cacheManager != null ? "✓ 已初始化" : "✗ 未初始化").append("\n");
        report.append("\n系統整合狀態:\n");
        report.append("經濟系統: ").append(economyAPI != null ? "✓ 已連接" : "✗ 未連接").append("\n");
        report.append("多世界系統: ").append(multiWorldAPI != null ? "✓ 已連接" : "✗ 未連接").append("\n");
        report.append("展示系統: ").append(displayAPI != null ? "✓ 已連接" : "✗ 未連接").append("\n");
        report.append("國家系統: ").append(nationsAPI != null ? "✓ 已連接" : "✗ 未連接").append("\n");
        
        return report.toString();
    }

    /**
     * 檢查Discord整合是否可用
     */
    public boolean isDiscordIntegrationAvailable() {
        return discordBot != null && discordBot.isConnected();
    }

    /**
     * 發送測試通知
     */
    public void sendTestNotification(String message) {
        if (webhookManager != null) {
            webhookManager.sendTestMessage(message);
        }
    }

    // === Getter 方法 ===
    
    public ZientisDiscordAPI getDiscordAPI() { return discordAPI; }
    public ZientisDiscordBot getDiscordBot() { return discordBot; }
    public DiscordAuthManager getAuthManager() { return authManager; }
    public DiscordWebhookManager getWebhookManager() { return webhookManager; }
    public DiscordSecurityManager getSecurityManager() { return securityManager; }
    public DiscordCacheManager getCacheManager() { return cacheManager; }
    
    public ZientisEconomyAPI getEconomyAPI() { return economyAPI; }
    public ZientisMultiWorldAPI getMultiWorldAPI() { return multiWorldAPI; }
    public ZientisDisplayAPI getDisplayAPI() { return displayAPI; }
    public ZientisNationsAPI getNationsAPI() { return nationsAPI; }
}