package com.zientis.economy;

import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceRegistry;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.economy.commands.BalanceCommand;
import com.zientis.economy.commands.EconomyCommand;
import com.zientis.economy.commands.PayCommand;
import com.zientis.economy.listener.EconomyDiscordListener;
import com.zientis.economy.listeners.EconomyEventListener;
import com.zientis.economy.manager.EconomyManager;
import com.zientis.economy.vault.ZientisVaultEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class ZientisEconomyPlugin extends JavaPlugin {

    private EconomyManager economyService;

    @Override
    public void onEnable() {
        try {
            // 1. Get Core Services from Bukkit
            ServiceRegistry serviceRegistry = getServer().getServicesManager().getRegistration(ServiceRegistry.class).getProvider();
            DependencyContainer dependencyContainer = getServer().getServicesManager().getRegistration(DependencyContainer.class).getProvider();

            if (serviceRegistry == null || dependencyContainer == null) {
                throw new IllegalStateException("ZientisCore is not loaded or failed to provide essential services.");
            }

            // 2. Create and Register our own service
            this.economyService = new EconomyManager(this);
            
            // Inject dependencies into our service
            dependencyContainer.inject(this.economyService);

            // Register with the central registry, which also calls onInitialize
            serviceRegistry.registerService(this.economyService);
            
            // Also register our API implementation for other services to inject
            serviceRegistry.registerImplementation(ZientisEconomyAPI.class, this.economyService);

            // 3. Setup Vault integration
            setupVaultIntegration();

            // 4. Register commands and listeners
            registerCommandsAndListeners(dependencyContainer);

            getLogger().info("Zientis Economy System enabled using the new Service Architecture!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable Zientis Economy System: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // The ServiceRegistry in ZientisCore will handle the shutdown of our service.
        // We just need to unregister our Vault service if it was registered.
        if (getServer().getPluginManager().getPlugin("Vault") != null && getServer().getServicesManager().isProvidedFor(Economy.class)) {
            try {
                getServer().getServicesManager().unregister(Economy.class, getServer().getServicesManager().getRegistration(Economy.class).getProvider());
            } catch (Exception e) {
                getLogger().warning("Failed to unregister Vault service: " + e.getMessage());
            }
        }
        getLogger().info("Zientis Economy System disabled.");
    }

    private void setupVaultIntegration() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found - Vault integration disabled.");
            return;
        }
        try {
            ZientisVaultEconomy vaultEconomy = new ZientisVaultEconomy(this.economyService, getLogger());
            getServer().getServicesManager().register(Economy.class, vaultEconomy, this, ServicePriority.Highest);
            getLogger().info("Vault integration enabled successfully!");
        } catch (Exception e) {
            getLogger().warning("Failed to setup Vault integration: " + e.getMessage());
        }
    }

    private void registerCommandsAndListeners(DependencyContainer container) {
        // Commands
        EconomyCommand economyCommand = new EconomyCommand(economyService);
        getCommand("economy").setExecutor(economyCommand);
        getCommand("economy").setTabCompleter(economyCommand);

        BalanceCommand balanceCommand = new BalanceCommand(economyService);
        getCommand("balance").setExecutor(balanceCommand);
        getCommand("bal").setExecutor(balanceCommand);

        PayCommand payCommand = new PayCommand(economyService);
        getCommand("pay").setExecutor(payCommand);
        getCommand("pay").setTabCompleter(payCommand);

        // Event Listeners
        EconomyEventListener economyEventListener = new EconomyEventListener(economyService);
        getServer().getPluginManager().registerEvents(economyEventListener, this);

        EconomyDiscordListener discordListener = new EconomyDiscordListener(this, economyService);
        container.inject(discordListener); // Inject dependencies into the listener
        getServer().getPluginManager().registerEvents(discordListener, this);

        getLogger().info("Commands and Event Listeners registered.");
    }
}
