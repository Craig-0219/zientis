package com.zientis.core;

import com.zientis.core.discord.DiscordIntegrationService;
import com.zientis.core.service.ServiceRegistry;
import com.zientis.core.web.WebServerManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Zientis核心插件主類
 * 負責啟動核心系統並註冊服務
 */
public class ZientisCorePlugin extends JavaPlugin {
    
    private ZientisCore core;
    private WebServerManager webServerManager;
    
    @Override
    public void onLoad() {
        getLogger().info("正在載入Zientis核心系統...");
    }
    
    @Override
    public void onEnable() {
        try {
            // 初始化核心系統
            getLogger().info("正在啟動Zientis核心系統...");
            
            core = ZientisCore.initialize(this).join();
            
            // 啟動Web服務器（REST API）
            startWebServer();
            
            // 註冊服務到Bukkit
            registerBukkitServices();
            
            getLogger().info("Zientis核心系統啟動完成！");
            
        } catch (Exception e) {
            getLogger().severe("Zientis核心系統啟動失敗: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            getLogger().info("正在關閉Zientis核心系統...");
            
            // 關閉Web服務器
            if (webServerManager != null) {
                webServerManager.stop();
            }
            
            // 關閉核心系統
            if (core != null) {
                core.shutdown();
            }
            
            getLogger().info("Zientis核心系統關閉完成");
            
        } catch (Exception e) {
            getLogger().severe("Zientis核心系統關閉時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 啟動Web服務器
     */
    private void startWebServer() {
        try {
            webServerManager = new WebServerManager(this);
            webServerManager.start();
            
            getLogger().info("REST API服務器已啟動");
            
        } catch (Exception e) {
            getLogger().warning("REST API服務器啟動失敗: " + e.getMessage());
            // 不阻止插件啟動，API服務器是可選的
        }
    }
    
    /**
     * 註冊服務到Bukkit服務管理器
     */
    private void registerBukkitServices() {
        try {
            // 註冊Discord整合服務
            DiscordIntegrationService discordService = core.getDiscordService();
            if (discordService != null) {
                getServer().getServicesManager().register(
                    DiscordIntegrationService.class,
                    discordService,
                    this,
                    ServicePriority.Highest
                );
                getLogger().info("Discord整合服務已註冊");
            }
            
            // 註冊服務註冊器（供其他插件使用）
            getServer().getServicesManager().register(
                ServiceRegistry.class,
                ServiceRegistry.getInstance(),
                this,
                ServicePriority.Highest
            );
            
            getLogger().info("核心服務註冊完成");
            
        } catch (Exception e) {
            getLogger().warning("註冊核心服務失敗: " + e.getMessage());
        }
    }
    
    /**
     * 獲取核心實例
     */
    public ZientisCore getCore() {
        return core;
    }
    
    /**
     * 獲取Web服務器管理器
     */
    public WebServerManager getWebServerManager() {
        return webServerManager;
    }
}