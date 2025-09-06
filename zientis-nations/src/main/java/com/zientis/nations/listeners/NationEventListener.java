package com.zientis.nations.listeners;

import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.nations.data.Nation;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

/**
 * 國家系統事件監聽器
 */
public class NationEventListener implements Listener {
    
    private final ZientisNationsAPI nationsAPI;
    
    public NationEventListener(ZientisNationsAPI nationsAPI) {
        this.nationsAPI = nationsAPI;
    }
    
    /**
     * 玩家加入服務器時的處理
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 檢查玩家是否屬於某個國家
        Nation nation = nationsAPI.getPlayerNation(event.getPlayer().getUniqueId()).join();
        
        if (nation != null) {
            // 歡迎消息
            event.getPlayer().sendMessage(ChatColor.GOLD + "歡迎回到 " + nation.getName() + " 國家！");
            
            // 通知其他國家成員
            broadcastToNationMembers(nation, 
                ChatColor.GREEN + event.getPlayer().getName() + " 上線了！", 
                event.getPlayer().getUniqueId());
        }
    }
    
    /**
     * 玩家離開服務器時的處理
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 檢查玩家是否屬於某個國家
        Nation nation = nationsAPI.getPlayerNation(event.getPlayer().getUniqueId()).join();
        
        if (nation != null) {
            // 通知其他國家成員
            broadcastToNationMembers(nation, 
                ChatColor.GRAY + event.getPlayer().getName() + " 離線了", 
                event.getPlayer().getUniqueId());
        }
    }
    
    /**
     * 向國家成員廣播消息（排除指定玩家）
     */
    private void broadcastToNationMembers(Nation nation, String message, java.util.UUID excludePlayer) {
        for (java.util.UUID memberId : nation.getMembers().keySet()) {
            if (!memberId.equals(excludePlayer)) {
                org.bukkit.entity.Player member = org.bukkit.Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(message);
                }
            }
        }
    }
}