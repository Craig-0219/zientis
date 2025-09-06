package com.zientis.nations.api;

import com.zientis.core.api.ZientisAPI;
import com.zientis.economy.api.ZientisEconomyAPI;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import com.zientis.nations.data.*;
import com.zientis.nations.manager.NationManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Zientis國家系統API實作
 */
public class ZientisNationsAPIImpl implements ZientisNationsAPI {
    
    private final NationManager nationManager;
    private ZientisAPI coreAPI;
    private ZientisEconomyAPI economyAPI;
    private ZientisMultiWorldAPI multiWorldAPI;
    
    public ZientisNationsAPIImpl(NationManager nationManager) {
        this.nationManager = nationManager;
    }
    
    // === 基本國家操作 ===
    
    @Override
    public CompletableFuture<Nation> createNation(String nationName, UUID founderId) {
        return CompletableFuture.supplyAsync(() -> {
            // 檢查國家名稱是否已存在
            if (nationManager.getNationByName(nationName).isPresent()) {
                throw new IllegalArgumentException("國家名稱已存在: " + nationName);
            }
            
            // 檢查玩家是否已經有國家
            if (nationManager.getNationByMember(founderId).isPresent()) {
                throw new IllegalArgumentException("玩家已經屬於一個國家");
            }
            
            // 創建新國家
            Nation nation = new Nation(UUID.randomUUID(), nationName, founderId);
            // The founder is already added in the constructor with the LEADER role
            
            // 保存到數據庫
            nationManager.saveNation(nation);
            
            return nation;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> dissolveNation(UUID nationId, UUID dissolverId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查權限（只有創建者可以刪除）
            if (!nation.getFounderId().equals(dissolverId)) {
                throw new SecurityException("只有國家創建者可以解散國家");
            }
            
            // 執行刪除
            return nationManager.deleteNation(nationId);
        });
    }
    
    @Override
    public CompletableFuture<Nation> getNation(UUID nationId) {
        return CompletableFuture.supplyAsync(() -> nationManager.getNationById(nationId).orElse(null));
    }
    
    @Override
    public CompletableFuture<Nation> getNationByName(String name) {
        return CompletableFuture.supplyAsync(() -> nationManager.getNationByName(name).orElse(null));
    }
    
    @Override
    public CompletableFuture<Nation> getPlayerNation(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> nationManager.getNationByMember(playerId).orElse(null));
    }
    
    @Override
    public CompletableFuture<List<Nation>> getAllNations() {
        return CompletableFuture.supplyAsync(nationManager::getAllNations);
    }
    
    @Override
    public CompletableFuture<List<Nation>> getActiveNations() {
        return CompletableFuture.supplyAsync(nationManager::getActiveNations);
    }
    
    // === 成員管理 ===
    
    @Override
    public CompletableFuture<Boolean> invitePlayer(UUID nationId, UUID inviterId, UUID targetId, NationRole role) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查邀請者權限
            if (!nation.hasPermission(inviterId, NationPermission.INVITE_MEMBERS)) {
                throw new SecurityException("沒有邀請成員的權限");
            }
            
            // 檢查目標玩家是否已經在其他國家
            if (nationManager.getNationByMember(targetId).isPresent()) {
                return false;
            }
            
            // 發送邀請
            return nationManager.sendInvitation(nationId, inviterId, targetId, role);
        });
    }
    
    @Override
    public CompletableFuture<Boolean> joinNation(UUID nationId, UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            // 驗證邀請
            if (!nationManager.hasValidInvitation(playerId, nationId)) {
                return false;
            }
            
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            NationRole role = nationManager.getInvitationRole(playerId, nationId);
            
            // 加入國家
            nation.addMember(playerId, role != null ? role : NationRole.CITIZEN);
            nationManager.saveNation(nation);
            
            // 清除邀請
            nationManager.clearInvitation(playerId, nationId);
            
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> kickMember(UUID nationId, UUID kickerId, UUID targetId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查權限
            if (!nation.hasPermission(kickerId, NationPermission.KICK_MEMBERS)) {
                throw new SecurityException("沒有踢出成員的權限");
            }
            
            // 不能踢出創建者
            if (nation.getFounderId().equals(targetId)) {
                throw new IllegalArgumentException("不能踢出國家創建者");
            }
            
            // 移除成員
            nation.removeMember(targetId);
            nationManager.saveNation(nation);
            
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> setMemberRole(UUID nationId, UUID setId, UUID targetId, NationRole role) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查權限
            if (!nation.hasPermission(setId, NationPermission.MANAGE_ROLES)) {
                throw new SecurityException("沒有管理角色的權限");
            }
            
            // 設置新角色
            nation.setMemberRole(targetId, role);
            nationManager.saveNation(nation);
            
            return true;
        });
    }
    
    // === 經濟管理 ===
    
    
    
    @Override
    public CompletableFuture<Boolean> depositToTreasury(UUID nationId, UUID playerId, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查玩家是否是成員
            if (!nation.isMember(playerId)) {
                return false;
            }
            
            // 使用經濟API扣除玩家金錢
            if (economyAPI != null && economyAPI.deposit(playerId, java.math.BigDecimal.valueOf(amount), "Nation deposit").join() != null) {
                nation.addToTreasury(amount);
                nationManager.saveNation(nation);
                return true;
            }
            
            return false;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> withdrawFromTreasury(UUID nationId, UUID playerId, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return false;
            }
            
            Nation nation = nationOpt.get();
            
            // 檢查權限
            if (!nation.hasPermission(playerId, NationPermission.MANAGE_TREASURY)) {
                throw new SecurityException("沒有管理國庫的權限");
            }
            
            // 檢查國庫餘額
            if (nation.getTreasury() < amount) {
                return false;
            }
            
            // 提取金錢
            if (nation.removeFromTreasury(amount)) {
                if (economyAPI != null) {
                    economyAPI.withdraw(playerId, java.math.BigDecimal.valueOf(amount), "Nation withdraw");
                }
                nationManager.saveNation(nation);
                return true;
            }
            
            return false;
        });
    }
    
    // === 外交系統 ===
    
    @Override
    public CompletableFuture<Boolean> setDiplomaticRelation(UUID nationId1, UUID nationId2, 
                                                          DiplomaticRelation relationType) {
        return CompletableFuture.supplyAsync(() -> {
            return nationManager.setDiplomaticRelation(nationId1, nationId2, relationType);
        });
    }
    
    @Override
    public CompletableFuture<DiplomaticRelation> getDiplomaticRelation(UUID nationId1, UUID nationId2) {
        return CompletableFuture.supplyAsync(() -> nationManager.getDiplomaticRelation(nationId1, nationId2).orElse(null));
    }
    
    
    
    // === 戰爭系統 ===
    
    @Override
    public CompletableFuture<Boolean> declareWar(UUID attackerNationId, UUID defenderNationId, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            // 檢查兩個國家都存在
            Optional<Nation> attackerOpt = nationManager.getNationById(attackerNationId);
            Optional<Nation> defenderOpt = nationManager.getNationById(defenderNationId);
            
            if (attackerOpt.isEmpty() || defenderOpt.isEmpty()) {
                return false;
            }
            
            // 設置戰爭關係
            return nationManager.setDiplomaticRelation(
                attackerNationId, defenderNationId, DiplomaticRelation.WAR
            );
        });
    }
    
    
    
    // === 統計和信息 ===
    
    @Override
    public CompletableFuture<NationStats> getNationStats(UUID nationId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Nation> nationOpt = nationManager.getNationById(nationId);
            if (nationOpt.isEmpty()) {
                return null;
            }
            
            return nationManager.calculateNationStats(nationOpt.get());
        });
    }
    
    @Override
    public CompletableFuture<NationSystemStats> getSystemStats() {
        return CompletableFuture.supplyAsync(() -> {
            List<Nation> allNations = nationManager.getAllNations();
            List<Nation> activeNations = nationManager.getActiveNations();
            int totalMembers = allNations.stream().mapToInt(n -> n.getMembers().size()).sum();
            int totalTerritories = allNations.stream().mapToInt(Nation::getTerritoryCount).sum();
            double totalTreasury = allNations.stream().mapToDouble(Nation::getTreasury).sum();
            int activeWars = nationManager.getActiveWarsCount();
            long averageOnlineTime = 0; // Placeholder
            return new NationSystemStats(allNations.size(), activeNations.size(), totalMembers, totalTerritories, totalTreasury, activeWars, averageOnlineTime);
        });
    }
    
    // === Setter 方法 ===
    
    public void setCoreAPI(ZientisAPI coreAPI) {
        this.coreAPI = coreAPI;
    }
    
    public void setEconomyAPI(ZientisEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }
    
    public void setMultiWorldAPI(ZientisMultiWorldAPI multiWorldAPI) {
        this.multiWorldAPI = multiWorldAPI;
    }

    @Override
    public CompletableFuture<String> performMaintenance() {
        return CompletableFuture.supplyAsync(() -> {
            nationManager.saveAllNations();
            return "Nation maintenance complete.";
        });
    }

    @Override
    public CompletableFuture<Boolean> reloadConfig() {
        return CompletableFuture.supplyAsync(() -> {
            nationManager.reloadNations();
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> sendDiscordNotification(String eventType, UUID nationId, String message) {
        // TODO: Implement Discord notification
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<String> handleDiscordCommand(String command, String[] args, String discordUserId) {
        // TODO: Implement Discord command handling
        return CompletableFuture.completedFuture("Unknown command");
    }
}