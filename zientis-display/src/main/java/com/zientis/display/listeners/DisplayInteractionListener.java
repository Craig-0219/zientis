package com.zientis.display.listeners;

import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.display.data.DisplayModel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 展示互動監聽器
 * 
 * 處理玩家與島嶼展示的互動事件
 */
public class DisplayInteractionListener implements Listener {
    
    private static final Logger logger = Logger.getLogger(DisplayInteractionListener.class.getName());
    
    private final ZientisDisplayAPI displayAPI;
    
    // 玩家最後點擊時間 (防止重複點擊)
    private final Map<UUID, Long> lastClickTimes;
    
    // 點擊冷卻時間 (毫秒)
    private static final long CLICK_COOLDOWN = 1000;

    public DisplayInteractionListener(ZientisDisplayAPI displayAPI) {
        this.displayAPI = displayAPI;
        this.lastClickTimes = new HashMap<>();
    }

    /**
     * 處理玩家點擊事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // 只處理右鍵和左鍵點擊方塊
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && 
            event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        Location clickedLocation = event.getClickedBlock().getLocation();
        
        // 檢查是否點擊了展示區域
        DisplayModel clickedDisplay = findNearestDisplay(clickedLocation, 3.0);
        if (clickedDisplay == null) {
            return;
        }
        
        // 檢查點擊冷卻
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastClickTime = lastClickTimes.get(playerId);
        
        if (lastClickTime != null && (currentTime - lastClickTime) < CLICK_COOLDOWN) {
            return; // 冷卻中
        }
        
        lastClickTimes.put(playerId, currentTime);
        
        // 取消事件，防止破壞方塊
        event.setCancelled(true);
        
        // 根據點擊類型處理
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleRightClick(player, clickedDisplay);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleLeftClick(player, clickedDisplay);
        }
    }

    /**
     * 處理玩家移動事件 (用於展示LOD優化)
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 只在玩家實際移動時處理 (不是頭部轉動)
        if (event.getFrom().distance(event.getTo()) < 0.1) {
            return;
        }
        
        // TODO: 實現基於距離的LOD優化
        // displayRenderer.updateLODForPlayer(event.getPlayer());
    }

    /**
     * 處理右鍵點擊 - 打開詳細信息GUI
     */
    private void handleRightClick(Player player, DisplayModel display) {
        logger.info("玩家 " + player.getName() + " 右鍵點擊展示: " + display.getIslandId());
        
        // 發送展示詳細信息
        sendDisplayInfo(player, display);
        
        // 如果有權限，顯示管理選項
        if (player.hasPermission("zientis.display.manage")) {
            sendManagementOptions(player, display);
        }
    }

    /**
     * 處理左鍵點擊 - 快速傳送到島嶼
     */
    private void handleLeftClick(Player player, DisplayModel display) {
        logger.info("玩家 " + player.getName() + " 左鍵點擊展示: " + display.getIslandId());
        
        // 檢查傳送權限
        if (!player.hasPermission("zientis.display.teleport")) {
            player.sendMessage("§c你沒有權限傳送到其他島嶼");
            return;
        }
        
        // 執行傳送 (這裡需要與多世界系統整合)
        attemptTeleportToIsland(player, display);
    }

    /**
     * 查找最近的展示
     */
    private DisplayModel findNearestDisplay(Location location, double maxDistance) {
        List<DisplayModel> nearbyDisplays = displayAPI.getNearbyDisplays(location, (int) maxDistance + 1);
        
        DisplayModel nearest = null;
        double nearestDistance = maxDistance;
        
        for (DisplayModel display : nearbyDisplays) {
            double distance = location.distance(display.getCenterLocation());
            if (distance <= maxDistance && distance < nearestDistance) {
                nearest = display;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }

    /**
     * 發送展示信息
     */
    private void sendDisplayInfo(Player player, DisplayModel display) {
        player.sendMessage("§e=== 島嶼展示信息 ===");
        player.sendMessage("§7島嶼ID: §f" + display.getIslandId().toString().substring(0, 8) + "...");
        player.sendMessage("§7展示等級: §f" + display.getDisplayTier());
        player.sendMessage("§7狀態: §f" + getStatusDisplay(display.getStatus()));
        player.sendMessage("§7方塊數量: §f" + display.getBlockCount());
        player.sendMessage("");
        player.sendMessage("§a左鍵點擊傳送到島嶼");
        
        if (player.hasPermission("zientis.display.manage")) {
            player.sendMessage("§eShift+右鍵查看管理選項");
        }
    }

    /**
     * 發送管理選項
     */
    private void sendManagementOptions(Player player, DisplayModel display) {
        if (!player.isSneaking()) {
            return;
        }
        
        player.sendMessage("§c=== 管理員選項 ===");
        player.sendMessage("§7指令格式:");
        player.sendMessage("§f/display update " + display.getIslandId() + " FULL_REBUILD");
        player.sendMessage("§f/display update " + display.getIslandId() + " INCREMENTAL");
        player.sendMessage("§f/display remove " + display.getIslandId());
        player.sendMessage("§f/display info " + display.getIslandId());
    }

    /**
     * 嘗試傳送到島嶼
     */
    private void attemptTeleportToIsland(Player player, DisplayModel display) {
        player.sendMessage("§e正在傳送到島嶼...");
        
        // TODO: 與多世界系統整合，獲取島嶼傳送點
        // 暫時實現：傳送到展示位置上方
        Location teleportLocation = display.getCenterLocation().clone().add(0, 5, 0);
        
        player.teleport(teleportLocation);
        player.sendMessage("§a已傳送到島嶼展示區域");
        
        // 播放傳送音效
        player.playSound(player.getLocation(), "entity.enderman.teleport", 1.0f, 1.0f);
    }

    /**
     * 獲取狀態顯示文本
     */
    private String getStatusDisplay(com.zientis.display.data.DisplayStatus status) {
        switch (status) {
            case ACTIVE:
                return "§a活躍";
            case PAUSED:
                return "§7已暫停";
            case UPDATING:
                return "§e更新中";
            case ERROR:
                return "§c錯誤";
            case CREATING:
                return "§e創建中";
            default:
                return "§7未知";
        }
    }

    /**
     * 清理過期的點擊記錄
     */
    public void cleanupClickRecords() {
        long currentTime = System.currentTimeMillis();
        long cleanupThreshold = currentTime - (CLICK_COOLDOWN * 10); // 保留10倍冷卻時間的記錄
        
        lastClickTimes.entrySet().removeIf(entry -> entry.getValue() < cleanupThreshold);
    }
}