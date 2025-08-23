package com.zientis.economy;

import com.zientis.core.api.ZientisAPI;
import com.zientis.core.discord.DiscordIntegrationService;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.commands.EconomyCommand;
import com.zientis.economy.commands.BalanceCommand;
import com.zientis.economy.commands.PayCommand;
import com.zientis.economy.listeners.EconomyEventListener;
import com.zientis.economy.listener.EconomyDiscordListener;
import com.zientis.economy.manager.EconomyManager;
import com.zientis.economy.vault.ZientisVaultEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Zientis Economy System
 * Integrates with Vault API and provides economy functionality
 */
public class ZientisEconomyPlugin extends JavaPlugin {
    
    private EconomyManager economyManager;
    private ZientisVaultEconomy vaultEconomy;
    private EconomyDiscordListener discordListener;
    private DiscordIntegrationService discordIntegrationService;
    
    @Override
    public void onLoad() {
        getLogger().info("Loading Zientis Economy System...");
    }
    
    @Override
    public void onEnable() {
        try {
            // Initialize economy manager
            economyManager = new EconomyManager(this);
            
            // Register API service
            getServer().getServicesManager().register(
                ZientisEconomyAPI.class, 
                economyManager, 
                this, 
                ServicePriority.Normal
            );
            
            // Setup Vault integration if available
            setupVaultIntegration();
            
            // Register commands
            registerCommands();
            
            // Register event listeners
            registerEventListeners();
            
            // Register with ZientisAPI if available
            registerWithZientisAPI();
            
            // Setup Discord integration
            setupDiscordIntegration();
            
            getLogger().info("Zientis Economy System enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable Zientis Economy System: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // Shutdown economy manager
            if (economyManager != null) {
                economyManager.shutdown();
            }
            
            // Unregister Vault service
            if (vaultEconomy != null) {
                getServer().getServicesManager().unregister(Economy.class, vaultEconomy);
            }
            
            getLogger().info("Zientis Economy System disabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup Vault integration if Vault is available
     */
    private void setupVaultIntegration() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found - Vault integration disabled");
            return;
        }
        
        try {
            // Create Vault economy provider
            vaultEconomy = new ZientisVaultEconomy(economyManager, getLogger());
            
            // Register with Vault
            getServer().getServicesManager().register(
                Economy.class,
                vaultEconomy,
                this,
                ServicePriority.Highest  // High priority to override other economy plugins
            );
            
            getLogger().info("Vault integration enabled successfully!");
            
        } catch (Exception e) {
            getLogger().warning("Failed to setup Vault integration: " + e.getMessage());
        }
    }
    
    /**
     * Register plugin commands
     */
    private void registerCommands() {
        try {
            // Economy admin command
            EconomyCommand economyCommand = new EconomyCommand(economyManager);
            getCommand("economy").setExecutor(economyCommand);
            getCommand("economy").setTabCompleter(economyCommand);
            
            // Balance command
            BalanceCommand balanceCommand = new BalanceCommand(economyManager);
            getCommand("balance").setExecutor(balanceCommand);
            getCommand("bal").setExecutor(balanceCommand);
            
            // Pay command
            PayCommand payCommand = new PayCommand(economyManager);
            getCommand("pay").setExecutor(payCommand);
            getCommand("pay").setTabCompleter(payCommand);
            
            getLogger().info("Commands registered successfully!");
            
        } catch (Exception e) {
            getLogger().warning("Failed to register commands: " + e.getMessage());
        }
    }
    
    /**
     * Register event listeners
     */
    private void registerEventListeners() {
        try {
            EconomyEventListener listener = new EconomyEventListener(economyManager);
            getServer().getPluginManager().registerEvents(listener, this);
            
            // 註冊Discord監聽器
            discordListener = new EconomyDiscordListener(this, economyManager);
            getServer().getPluginManager().registerEvents(discordListener, this);
            
            getLogger().info("Event listeners registered successfully!");
            
        } catch (Exception e) {
            getLogger().warning("Failed to register event listeners: " + e.getMessage());
        }
    }
    
    /**
     * Register with ZientisAPI if available
     */
    private void registerWithZientisAPI() {
        try {
            RegisteredServiceProvider<ZientisAPI> rsp = 
                getServer().getServicesManager().getRegistration(ZientisAPI.class);
            
            if (rsp != null) {
                ZientisAPI zientisAPI = rsp.getProvider();
                // TODO: Register economy system with main API
                getLogger().info("Registered with ZientisAPI successfully!");
            } else {
                getLogger().info("ZientisAPI not found - running standalone");
            }
            
        } catch (Exception e) {
            getLogger().warning("Failed to register with ZientisAPI: " + e.getMessage());
        }
    }
    
    /**
     * Get the economy manager instance
     * @return Economy manager
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    /**
     * Get the Vault economy provider
     * @return Vault economy provider
     */
    public ZientisVaultEconomy getVaultEconomy() {
        return vaultEconomy;
    }
    
    /**
     * 設定Discord整合
     */
    private void setupDiscordIntegration() {
        try {
            // 嘗試從ZientisAPI獲取Discord整合服務
            RegisteredServiceProvider<DiscordIntegrationService> rsp = 
                getServer().getServicesManager().getRegistration(DiscordIntegrationService.class);
            
            if (rsp != null) {
                discordIntegrationService = rsp.getProvider();
                
                // 設定經濟管理器的Discord整合
                economyManager.setDiscordIntegrationService(discordIntegrationService);
                
                // 設定Discord監聽器的整合服務
                if (discordListener != null) {
                    discordListener.setDiscordIntegrationService(discordIntegrationService);
                }
                
                getLogger().info("Discord整合已啟用！");
            } else {
                getLogger().info("Discord整合服務未找到 - 經濟系統將獨立運行");
            }
            
        } catch (Exception e) {
            getLogger().warning("設定Discord整合失敗: " + e.getMessage());
        }
    }
    
    /**
     * 手動觸發所有在線玩家的Discord同步
     */
    public void syncAllPlayersToDiscord() {
        if (discordListener != null) {
            discordListener.syncAllOnlinePlayersToDiscord().thenAccept(count -> {
                getLogger().info("已同步 " + count + " 個玩家的經濟數據到Discord");
            });
        }
    }
    
    /**
     * Check if Vault integration is enabled
     * @return True if Vault is integrated
     */
    public boolean isVaultEnabled() {
        return vaultEconomy != null;
    }
    
    /**
     * Check if Discord integration is enabled
     * @return True if Discord is integrated
     */
    public boolean isDiscordEnabled() {
        return discordIntegrationService != null;
    }
    
    /**
     * Get the Discord integration service
     * @return Discord integration service or null if not available
     */
    public DiscordIntegrationService getDiscordIntegrationService() {
        return discordIntegrationService;
    }
}