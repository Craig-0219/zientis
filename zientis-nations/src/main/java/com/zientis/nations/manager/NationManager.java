package com.zientis.nations.manager;

import com.zientis.nations.ZientisNationsPlugin;
import com.zientis.nations.data.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 國家管理器
 * 負責管理所有國家數據和操作
 */
public class NationManager {
    
    private static final Logger logger = Logger.getLogger(NationManager.class.getName());
    
    private final ZientisNationsPlugin plugin;
    
    // 數據緩存
    private final Map<UUID, Nation> nationsById = new ConcurrentHashMap<>();
    private final Map<String, UUID> nationsByName = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> memberToNation = new ConcurrentHashMap<>();
    private final Map<String, DiplomaticRelation> diplomaticRelations = new ConcurrentHashMap<>();
    private final Map<String, NationRole> pendingInvitations = new ConcurrentHashMap<>();
    
    public NationManager(ZientisNationsPlugin plugin) {
        this.plugin = plugin;
        loadAllNations();
    }
    
    // === 基本國家操作 ===
    
    /**
     * 保存國家數據
     */
    public void saveNation(Nation nation) {
        try {
            // 更新緩存
            nationsById.put(nation.getId(), nation);
            nationsByName.put(nation.getName().toLowerCase(), nation.getId());
            
            // 更新成員映射
            for (UUID memberId : nation.getMembers()) {
                memberToNation.put(memberId, nation.getId());
            }
            
            // 保存到配置文件（簡單實現）
            saveNationToConfig(nation);
            
            logger.info("已保存國家: " + nation.getName());
            
        } catch (Exception e) {
            logger.severe("保存國家失敗: " + e.getMessage());
        }
    }
    
    /**
     * 刪除國家
     */
    public boolean deleteNation(UUID nationId) {
        try {
            Nation nation = nationsById.get(nationId);
            if (nation == null) {
                return false;
            }
            
            // 從緩存移除
            nationsById.remove(nationId);
            nationsByName.remove(nation.getName().toLowerCase());
            
            // 移除成員映射
            for (UUID memberId : nation.getMembers()) {
                memberToNation.remove(memberId);
            }
            
            // TODO: Refactor diplomatic relations to properly associate with nations
            // diplomaticRelations.entrySet().removeIf(entry -> 
            //     entry.getValue().getNation1Id().equals(nationId) || 
            //     entry.getValue().getNation2Id().equals(nationId)
            // );
            
            // 從配置文件移除
            removeNationFromConfig(nation);
            
            logger.info("已刪除國家: " + nation.getName());
            return true;
            
        } catch (Exception e) {
            logger.severe("刪除國家失敗: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根據ID獲取國家
     */
    public Optional<Nation> getNationById(UUID nationId) {
        return Optional.ofNullable(nationsById.get(nationId));
    }
    
    /**
     * 根據名稱獲取國家
     */
    public Optional<Nation> getNationByName(String name) {
        UUID nationId = nationsByName.get(name.toLowerCase());
        return nationId != null ? Optional.ofNullable(nationsById.get(nationId)) : Optional.empty();
    }
    
    /**
     * 根據成員獲取國家
     */
    public Optional<Nation> getNationByMember(UUID memberId) {
        UUID nationId = memberToNation.get(memberId);
        return nationId != null ? Optional.ofNullable(nationsById.get(nationId)) : Optional.empty();
    }
    
    /**
     * 獲取所有國家
     */
    public List<Nation> getAllNations() {
        return new ArrayList<>(nationsById.values());
    }
    
    /**
     * 獲取活躍國家
     */
    public List<Nation> getActiveNations() {
        return nationsById.values().stream()
            .filter(Nation::isActive)
            .collect(Collectors.toList());
    }
    
    // === 邀請系統 ===
    
    /**
     * 發送邀請
     */
    public boolean sendInvitation(UUID nationId, UUID inviterId, UUID targetId, NationRole role) {
        String key = targetId + ":" + nationId;
        pendingInvitations.put(key, role);
        
        logger.info(String.format("玩家 %s 邀請 %s 加入國家 %s，角色為 %s", 
            inviterId, targetId, nationId, role));
        
        return true;
    }
    
    /**
     * 檢查是否有有效邀請
     */
    public boolean hasValidInvitation(UUID targetId, UUID nationId) {
        String key = targetId + ":" + nationId;
        return pendingInvitations.containsKey(key);
    }
    
    public NationRole getInvitationRole(UUID targetId, UUID nationId) {
        String key = targetId + ":" + nationId;
        return pendingInvitations.get(key);
    }

    /**
     * 清除邀請
     */
    public void clearInvitation(UUID targetId, UUID nationId) {
        String key = targetId + ":" + nationId;
        pendingInvitations.remove(key);
    }
    
    // === 外交系統 ===
    
    /**
     * 設置外交關係
     */
    public boolean setDiplomaticRelation(UUID nationId1, UUID nationId2, 
                                       DiplomaticRelation relation) {
        try {
            String key = getDiplomaticKey(nationId1, nationId2);
            // Enums are not instantiated with new. We just use the passed value.
            diplomaticRelations.put(key, relation);
            
            logger.info(String.format("設置外交關係: %s <-> %s = %s", 
                nationId1, nationId2, relation));
            
            return true;
            
        } catch (Exception e) {
            logger.severe("設置外交關係失敗: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 獲取外交關係
     */
    public Optional<DiplomaticRelation> getDiplomaticRelation(UUID nationId1, UUID nationId2) {
        String key = getDiplomaticKey(nationId1, nationId2);
        return Optional.ofNullable(diplomaticRelations.get(key));
    }
    
    /**
     * 獲取指定國家的所有外交關係
     */
    public List<DiplomaticRelation> getAllDiplomaticRelations(UUID nationId) {
        // TODO: Refactor diplomatic relations to properly associate with nations
        return new ArrayList<>(diplomaticRelations.values());
    }
    
    /**
     * 獲取活躍戰爭數量
     */
    public int getActiveWarsCount() {
        return (int) diplomaticRelations.values().stream()
            .filter(relation -> relation == DiplomaticRelation.WAR)
            .count();
    }
    
    // === 統計計算 ===
    
    /**
     * 計算國家統計信息
     */
    public NationStats calculateNationStats(Nation nation) {
        try {
            int memberCount = nation.getMembers().size();
            double treasury = nation.getTreasury();
            int territoryCount = nation.getTerritoryCount();
            int allianceCount = (int) getAllDiplomaticRelations(nation.getId()).stream()
                .filter(rel -> rel == DiplomaticRelation.ALLIANCE)
                .count();
            int warCount = (int) getAllDiplomaticRelations(nation.getId()).stream()
                .filter(rel -> rel == DiplomaticRelation.WAR)
                .count();
            
            return new NationStats(
                nation.getId(),
                memberCount,
                treasury,
                territoryCount,
                allianceCount,
                warCount,
                calculateNationPower(nation),
                nation.getLevel()
            );
            
        } catch (Exception e) {
            logger.severe("計算國家統計失敗: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 計算國家實力
     */
    private double calculateNationPower(Nation nation) {
        // 簡單的實力計算：成員數 * 10 + 財富 / 1000 + 領土數 * 5
        return nation.getMembers().size() * 10.0 + 
               nation.getTreasury() / 1000.0 + 
               nation.getTerritoryCount() * 5.0;
    }
    
    // === 數據持久化 ===
    
    /**
     * 載入所有國家數據
     */
    private void loadAllNations() {
        try {
            ConfigurationSection nationsSection = plugin.getConfig().getConfigurationSection("nations");
            if (nationsSection == null) {
                logger.info("沒有找到國家數據，從空白開始");
                return;
            }
            
            for (String nationIdStr : nationsSection.getKeys(false)) {
                try {
                    UUID nationId = UUID.fromString(nationIdStr);
                    Nation nation = loadNationFromConfig(nationId);
                    if (nation != null) {
                        nationsById.put(nation.getId(), nation);
                        nationsByName.put(nation.getName().toLowerCase(), nation.getId());
                        
                        // 更新成員映射
                        for (UUID memberId : nation.getMembers().keySet()) {
                            memberToNation.put(memberId, nation.getId());
                        }
                    }
                } catch (Exception e) {
                    logger.warning("載入國家失敗: " + nationIdStr + " - " + e.getMessage());
                }
            }
            
            logger.info("已載入 " + nationsById.size() + " 個國家");
            
        } catch (Exception e) {
            logger.severe("載入國家數據失敗: " + e.getMessage());
        }
    }
    
    /**
     * 從配置載入單個國家
     */
    private Nation loadNationFromConfig(UUID nationId) {
        try {
            ConfigurationSection nationSection = plugin.getConfig()
                .getConfigurationSection("nations." + nationId);
            if (nationSection == null) {
                return null;
            }
            
            String name = nationSection.getString("name");
            UUID founderId = UUID.fromString(nationSection.getString("founder"));
            String description = nationSection.getString("description", "");
            
            Nation nation = new Nation(nationId, name, founderId, description);

            // Load treasury
            if (nationSection.contains("treasury")) {
                nation.addToTreasury(nationSection.getDouble("treasury"));
            }
            
            // 載入成員
            ConfigurationSection membersSection = nationSection.getConfigurationSection("members");
            if (membersSection != null) {
                for (String memberIdStr : membersSection.getKeys(false)) {
                    UUID memberId = UUID.fromString(memberIdStr);
                    String roleStr = membersSection.getString(memberIdStr);
                    NationRole role = NationRole.valueOf(roleStr);
                    nation.addMember(memberId, role);
                }
            }
            
            return nation;
            
        } catch (Exception e) {
            logger.severe("從配置載入國家失敗: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 保存國家到配置
     */
    private void saveNationToConfig(Nation nation) {
        try {
            String basePath = "nations." + nation.getId();
            
            plugin.getConfig().set(basePath + ".name", nation.getName());
            plugin.getConfig().set(basePath + ".founder", nation.getFounderId().toString());
            plugin.getConfig().set(basePath + ".description", nation.getDescription());
            plugin.getConfig().set(basePath + ".treasury", nation.getTreasury());
            plugin.getConfig().set(basePath + ".created-time", nation.getCreatedTime().toString());
            
            // 保存成員
            for (Map.Entry<UUID, NationRole> entry : nation.getMemberRoles().entrySet()) {
                plugin.getConfig().set(basePath + ".members." + entry.getKey(), 
                    entry.getValue().name());
            }
            
            plugin.saveConfig();
            
        } catch (Exception e) {
            logger.severe("保存國家到配置失敗: " + e.getMessage());
        }
    }
    
    /**
     * 從配置移除國家
     */
    private void removeNationFromConfig(Nation nation) {
        try {
            plugin.getConfig().set("nations." + nation.getId(), null);
            plugin.saveConfig();
        } catch (Exception e) {
            logger.severe("從配置移除國家失敗: " + e.getMessage());
        }
    }
    
    // === 維護操作 ===
    
    /**
     * 保存所有國家
     */
    public void saveAllNations() {
        logger.info("正在保存所有國家數據...");
        for (Nation nation : nationsById.values()) {
            saveNationToConfig(nation);
        }
        logger.info("所有國家數據保存完成");
    }
    
    /**
     * 重新載入國家數據
     */
    public void reloadNations() {
        logger.info("重新載入國家數據...");
        
        // 清空緩存
        clearCache();
        
        // 重新載入
        loadAllNations();
        
        logger.info("國家數據重載完成");
    }
    
    /**
     * 清理緩存
     */
    public void clearCache() {
        nationsById.clear();
        nationsByName.clear();
        memberToNation.clear();
        diplomaticRelations.clear();
        pendingInvitations.clear();
    }
    
    // === 工具方法 ===
    
    /**
     * 生成外交關係鍵值
     */
    private String getDiplomaticKey(UUID nationId1, UUID nationId2) {
        // 確保鍵值的一致性
        if (nationId1.compareTo(nationId2) < 0) {
            return nationId1 + ":" + nationId2;
        } else {
            return nationId2 + ":" + nationId1;
        }
    }
}