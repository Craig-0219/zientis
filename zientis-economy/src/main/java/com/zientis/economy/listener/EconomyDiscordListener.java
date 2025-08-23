package com.zientis.economy.listener;

import com.zientis.economy.manager.EconomyManager;
import com.zientis.core.discord.DiscordIntegrationService;
import com.zientis.core.discord.model.GameEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 經濟系統Discord事件監聽器
 * 處理玩家加入/離開事件並同步經濟數據到Discord
 */
public class EconomyDiscordListener implements Listener {
    
    private final Plugin plugin;
    private final Logger logger;
    private final EconomyManager economyManager;
    private DiscordIntegrationService discordIntegrationService;
    
    public EconomyDiscordListener(Plugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.economyManager = economyManager;
    }
    
    /**
     * 設定Discord整合服務
     */
    public void setDiscordIntegrationService(DiscordIntegrationService discordIntegrationService) {
        this.discordIntegrationService = discordIntegrationService;
        logger.info("經濟系統Discord整合已啟用");
    }
    
    /**
     * 玩家加入事件 - 同步經濟數據並發送Discord通知
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 異步處理以避免阻塞主線程
        CompletableFuture.runAsync(() -> {
            try {
                // 確保玩家有經濟帳戶
                economyManager.getOrCreateAccount(playerId).join();
                
                // 如果Discord整合已啟用，發送加入事件並同步數據
                if (discordIntegrationService != null && discordIntegrationService.isRunning()) {
                    // 發送玩家加入事件
                    GameEvent joinEvent = GameEvent.playerJoin(player.getName(), playerId.toString());
                    discordIntegrationService.sendGameEvent(
                        joinEvent.getEventType().getCode(),
                        joinEvent.toApiFormat()
                    );
                    
                    // 延遲同步經濟數據，確保玩家完全加載
                    CompletableFuture.delayedExecutor(2, java.util.concurrent.TimeUnit.SECONDS)
                        .execute(() -> {
                            economyManager.syncToDiscord(playerId);
                        });
                    
                    logger.info("已同步玩家 " + player.getName() + " 的經濟數據到Discord");
                }
                
            } catch (Exception e) {
                logger.warning("處理玩家加入的Discord同步失敗: " + e.getMessage());
            }
        });
    }
    
    /**
     * 玩家離開事件 - 同步最終經濟數據並發送Discord通知
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 異步處理以避免阻塞主線程
        CompletableFuture.runAsync(() -> {
            try {
                // 如果Discord整合已啟用，同步最終數據並發送離開事件
                if (discordIntegrationService != null && discordIntegrationService.isRunning()) {
                    // 先同步最終經濟數據
                    economyManager.syncToDiscord(playerId);
                    
                    // 發送玩家離開事件
                    GameEvent leaveEvent = GameEvent.playerLeave(player.getName(), playerId.toString());
                    discordIntegrationService.sendGameEvent(
                        leaveEvent.getEventType().getCode(),
                        leaveEvent.toApiFormat()
                    );
                    
                    logger.info("已同步玩家 " + player.getName() + " 的最終經濟數據到Discord");
                }
                
            } catch (Exception e) {
                logger.warning("處理玩家離開的Discord同步失敗: " + e.getMessage());
            }
        });
    }
    
    /**
     * 手動觸發所有在線玩家的Discord同步
     */
    public CompletableFuture<Integer> syncAllOnlinePlayersToDiscord() {
        return CompletableFuture.supplyAsync(() -> {
            if (discordIntegrationService == null || !discordIntegrationService.isRunning()) {
                logger.warning("Discord整合服務未啟用，無法同步玩家數據");
                return 0;
            }
            
            int syncCount = 0;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                try {
                    economyManager.syncToDiscord(player.getUniqueId()).join();
                    syncCount++;
                } catch (Exception e) {
                    logger.warning("同步玩家 " + player.getName() + " 到Discord失敗: " + e.getMessage());
                }
            }
            
            logger.info("已同步 " + syncCount + " 個在線玩家的經濟數據到Discord");
            return syncCount;
        });
    }
    
    /**
     * 批次同步經濟交易事件到Discord
     */
    public void sendBatchEconomyUpdate() {
        if (discordIntegrationService == null || !discordIntegrationService.isRunning()) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                // 發送伺服器經濟統計更新
                GameEvent statsEvent = new GameEvent(GameEvent.EventType.CUSTOM_EVENT)
                    .addData("event_subtype", "economy_batch_update")
                    .addData("online_players", plugin.getServer().getOnlinePlayers().size())
                    .addData("message", "伺服器經濟數據批次更新")
                    .setServerName(plugin.getServer().getName());
                
                discordIntegrationService.sendGameEvent(
                    statsEvent.getEventType().getCode(),
                    statsEvent.toApiFormat()
                );
                
            } catch (Exception e) {
                logger.warning("發送批次經濟更新到Discord失敗: " + e.getMessage());
            }
        });
    }
}