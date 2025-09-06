package com.zientis.nations.tasks;

import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.nations.data.Nation;
import com.zientis.nations.data.NationLevel;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;

/**
 * 國家系統維護任務
 * 定期執行國家系統的維護工作
 */
public class NationMaintenanceTask extends BukkitRunnable {
    
    private static final Logger logger = Logger.getLogger(NationMaintenanceTask.class.getName());
    
    private final ZientisNationsAPI nationsAPI;
    
    public NationMaintenanceTask(ZientisNationsAPI nationsAPI) {
        this.nationsAPI = nationsAPI;
    }
    
    @Override
    public void run() {
        try {
            logger.info("開始執行國家系統維護...");
            
            // 獲取所有國家
            List<Nation> allNations = nationsAPI.getAllNations().join();
            
            // 執行各種維護任務
            performInactivityCheck(allNations);
            performEconomicMaintenance(allNations);
            performWarMaintenance(allNations);
            performLevelCalculation(allNations);
            
            logger.info("國家系統維護完成，處理了 " + allNations.size() + " 個國家");
            
        } catch (Exception e) {
            logger.severe("執行國家維護時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 檢查不活躍國家
     */
    private void performInactivityCheck(List<Nation> nations) {
        try {
            long currentTime = System.currentTimeMillis();
            long inactivityThreshold = 30L * 24 * 60 * 60 * 1000; // 30天
            
            for (Nation nation : nations) {
                if (isNationInactive(nation, currentTime, inactivityThreshold)) {
                    handleInactiveNation(nation);
                }
            }
            
        } catch (Exception e) {
            logger.warning("檢查不活躍國家時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 檢查國家是否不活躍
     */
    private boolean isNationInactive(Nation nation, long currentTime, long threshold) {
        // 檢查創建者和成員的最後上線時間
        // 這裡需要與核心系統集成來獲取玩家活動數據
        // 暫時使用簡單的時間檢查
        return (currentTime - nation.getCreatedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) > threshold && 
               nation.getMembers().size() <= 1;
    }
    
    /**
     * 處理不活躍國家
     */
    private void handleInactiveNation(Nation nation) {
        logger.info("發現不活躍國家: " + nation.getName());
        
        // 可以執行以下操作：
        // 1. 發出警告
        // 2. 降級國家
        // 3. 凍結國庫
        // 4. 在更長時間後自動解散
        
        // 這裡暫時只記錄日誌
        logger.warning("國家 " + nation.getName() + " 可能需要管理員關注（不活躍）");
    }
    
    /**
     * 執行經濟維護
     */
    private void performEconomicMaintenance(List<Nation> nations) {
        try {
            for (Nation nation : nations) {
                performNationEconomicMaintenance(nation);
            }
            
        } catch (Exception e) {
            logger.warning("執行經濟維護時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 單個國家的經濟維護
     */
    private void performNationEconomicMaintenance(Nation nation) {
        // 計算維護費用
        double maintenanceCost = calculateMaintenanceCost(nation);
        
        if (maintenanceCost > 0) {
            if (nation.getTreasury() >= maintenanceCost) {
                // 扣除維護費用
                nation.removeFromTreasury(maintenanceCost);
                logger.fine("國家 " + nation.getName() + " 支付維護費用: " + maintenanceCost);
            } else {
                // 國庫不足，可能需要懲罰
                handleInsufficientTreasury(nation, maintenanceCost);
            }
        }
    }
    
    /**
     * 計算國家維護費用
     */
    private double calculateMaintenanceCost(Nation nation) {
        // 基礎費用 + 成員費用 + 領土費用
        double baseCost = 100.0; // 基礎費用
        double memberCost = nation.getMembers().size() * 10.0; // 每個成員10
        double territoryCost = nation.getTerritoryCount() * 20.0; // 每個領土20
        
        return baseCost + memberCost + territoryCost;
    }
    
    /**
     * 處理國庫不足的情況
     */
    private void handleInsufficientTreasury(Nation nation, double requiredAmount) {
        logger.warning("國家 " + nation.getName() + " 國庫不足，無法支付維護費用: " + requiredAmount);
        
        // 可能的處理方式：
        // 1. 暫停國家某些功能
        // 2. 降低國家等級
        // 3. 通知國家成員
        
        // 這裡暫時只記錄警告
    }
    
    /**
     * 戰爭維護
     */
    private void performWarMaintenance(List<Nation> nations) {
        try {
            // 檢查戰爭狀態
            // 計算戰爭消耗
            // 更新戰爭統計
            
            logger.fine("戰爭維護檢查完成");
            
        } catch (Exception e) {
            logger.warning("執行戰爭維護時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 計算並更新國家等級
     */
    private void performLevelCalculation(List<Nation> nations) {
        try {
            for (Nation nation : nations) {
                int newLevel = calculateNationLevel(nation);
                NationLevel currentLevel = nation.getLevel();
                NationLevel calculatedLevel = NationLevel.fromLevel(newLevel);
                if (calculatedLevel != currentLevel) {
                    nation.setLevel(calculatedLevel);
                    logger.info("國家 " + nation.getName() + " 等級更新為: " + calculatedLevel.getDisplayName());
                    
                    // 通知國家成員等級變化
                    notifyNationLevelChange(nation, newLevel);
                }
            }
            
        } catch (Exception e) {
            logger.warning("計算國家等級時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 計算國家等級
     */
    private int calculateNationLevel(Nation nation) {
        // 根據多種因素計算國家等級
        int memberCount = nation.getMembers().size();
        double treasury = nation.getTreasury();
        int territoryCount = nation.getTerritoryCount();
        
        // 簡單的等級計算邏輯
        int level = 1;
        
        if (memberCount >= 5) level++;
        if (memberCount >= 10) level++;
        if (memberCount >= 20) level++;
        
        if (treasury >= 10000) level++;
        if (treasury >= 50000) level++;
        if (treasury >= 100000) level++;
        
        if (territoryCount >= 3) level++;
        if (territoryCount >= 5) level++;
        if (territoryCount >= 10) level++;
        
        return Math.min(level, 10); // 最大等級為10
    }
    
    /**
     * 通知國家成員等級變化
     */
    private void notifyNationLevelChange(Nation nation, int newLevel) {
        // 向所有在線成員發送通知
        String message = "§6國家等級提升至: §e" + newLevel + "§6！";
        
        for (java.util.UUID memberId : nation.getMembers()) {
            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}