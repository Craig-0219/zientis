package com.zientis.core;

import com.zientis.core.config.ConfigService;
import com.zientis.core.config.ZientisConfig;
import com.zientis.core.database.DatabaseConfig;
import com.zientis.core.database.DatabaseManager;
import com.zientis.core.discord.DiscordConfig;
import com.zientis.core.discord.DiscordIntegrationService;
import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceManager;
import com.zientis.core.service.ServiceRegistry;
import com.zientis.core.web.WebServerManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Zientis核心插件主類
 * 負責啟動核心系統並註冊服務
 */
public class ZientisCorePlugin extends JavaPlugin {

    private WebServerManager webServerManager;
    private ServiceManager serviceManager;
    private DependencyContainer dependencyContainer;
    private DatabaseManager databaseManager;

    @Override
    public void onLoad() {
        getLogger().info("正在載入Zientis核心系統...");
        // 在此階段初始化註冊器和容器，以便其他插件在onLoad時能存取
        DependencyContainer.getInstance();
        ServiceRegistry.initialize(this);
    }

    @Override
    public void onEnable() {
        try {
            getLogger().info("正在啟動Zientis核心系統...");

            // 1. 獲取核心實例
            this.dependencyContainer = DependencyContainer.getInstance();
            this.serviceManager = new ServiceManager(this);

            // 2. 初始化並註冊基礎服務
            // ConfigService
            ConfigService configService = new ConfigService(this);
            serviceManager.registerAndStartService(configService).join();

            // DatabaseManager
            ZientisConfig databaseConfig = configService.getModuleConfig("database");
            DatabaseConfig dbConfig = databaseConfig.getDatabaseConfig("database");
            this.databaseManager = new DatabaseManager(this, dbConfig);
            if (!databaseManager.initialize().join()) {
                throw new RuntimeException("資料庫初始化失敗");
            }

            // DiscordIntegrationService
            ZientisConfig discordConfig = configService.getModuleConfig("discord");
            DiscordConfig discordCfg = discordConfig.getDiscordConfig("discord");
            DiscordIntegrationService discordService = new DiscordIntegrationService(this, discordCfg);
            serviceManager.registerAndStartService(discordService).join();

            // 3. 註冊核心服務到DI容器
            dependencyContainer.registerSingleton(Plugin.class, this);
            dependencyContainer.registerSingleton(ServiceManager.class, serviceManager);
            dependencyContainer.registerSingleton(ConfigService.class, configService);
            dependencyContainer.registerSingleton(DatabaseManager.class, databaseManager);
            dependencyContainer.registerSingleton(DiscordIntegrationService.class, discordService);
            dependencyContainer.registerSingleton(DependencyContainer.class, dependencyContainer);

            // 4. 註冊核心服務到Bukkit服務管理器 (供其他插件使用)
            registerBukkitServices();

            // 5. 啟動Web服務器（REST API）
            startWebServer();

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

            // 關閉服務管理器中的所有服務
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
            // 將核心實例注入到WebServer中，以便API可以訪問核心功能
            dependencyContainer.inject(webServerManager);
            webServerManager.start();
            getLogger().info("REST API服務器已啟動");
        } catch (Exception e) {
            getLogger().warning("REST API服務器啟動失敗: " + e.getMessage());
        }
    }

    /**
     * 註冊核心服務到Bukkit服務管理器
     */
    private void registerBukkitServices() {
        // 註冊ServiceRegistry，讓其他插件可以註冊自己的服務
        getServer().getServicesManager().register(
            ServiceRegistry.class,
            ServiceRegistry.getInstance(),
            this,
            ServicePriority.Highest
        );

        // 註冊DependencyContainer，讓其他插件可以進行依賴注入
        getServer().getServicesManager().register(
            DependencyContainer.class,
            dependencyContainer,
            this,
            ServicePriority.Highest
        );

        getLogger().info("核心服務註冊器 (ServiceRegistry, DependencyContainer) 已發布到Bukkit");
    }
}
