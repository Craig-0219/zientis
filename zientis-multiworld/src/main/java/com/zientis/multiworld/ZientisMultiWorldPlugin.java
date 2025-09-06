package com.zientis.multiworld;

import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceRegistry;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.multiworld.commands.IslandCommand;
import com.zientis.multiworld.listeners.PlayerJoinListener;
import com.zientis.multiworld.manager.WorldManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class ZientisMultiWorldPlugin extends JavaPlugin {

    private WorldManager worldService;

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
            this.worldService = new WorldManager(this);
            
            // Inject dependencies into our service
            dependencyContainer.inject(this.worldService);

            // Register with the central registry
            serviceRegistry.registerService(this.worldService);
            
            // Also register our API implementation
            serviceRegistry.registerImplementation(ZientisMultiWorldAPI.class, this.worldService);

            // 3. Register commands and listeners
            registerCommandsAndListeners(dependencyContainer);

            getLogger().info("Zientis Multi-World System enabled using the new Service Architecture!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable Zientis Multi-World System: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // The ServiceRegistry in ZientisCore will handle the shutdown of registered services.
        getLogger().info("Zientis Multi-World System disabled.");
    }

    private void registerCommandsAndListeners(DependencyContainer container) {
        // Commands
        IslandCommand islandCommand = new IslandCommand(worldService);
        container.inject(islandCommand); // Inject dependencies into the command
        getCommand("island").setExecutor(islandCommand);
        getCommand("island").setTabCompleter(islandCommand);

        // Event Listeners
        PlayerJoinListener playerJoinListener = new PlayerJoinListener(worldService);
        container.inject(playerJoinListener); // Inject dependencies into the listener
        getServer().getPluginManager().registerEvents(playerJoinListener, this);

        getLogger().info("Commands and Event Listeners registered.");
    }
}
