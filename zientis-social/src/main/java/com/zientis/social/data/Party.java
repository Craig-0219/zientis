package com.zientis.social.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 組隊數據類
 */
public class Party {
    
    private final UUID id;
    private final UUID leaderId;
    private String name;
    private final long createdTime;
    private final Map<UUID, PartyRole> members;
    private final Map<UUID, Long> memberJoinTimes;
    private PartySettings settings;
    private PartyStatus status;
    private int maxMembers;
    
    public Party(UUID leaderId, String name) {
        this.id = UUID.randomUUID();
        this.leaderId = leaderId;
        this.name = name;
        this.createdTime = System.currentTimeMillis();
        this.members = new ConcurrentHashMap<>();
        this.memberJoinTimes = new ConcurrentHashMap<>();
        this.settings = new PartySettings();
        this.status = PartyStatus.ACTIVE;
        this.maxMembers = 8; // 預設最大8人
        
        // 隊長自動加入
        members.put(leaderId, PartyRole.LEADER);
        memberJoinTimes.put(leaderId, createdTime);
    }
    
    /**
     * 添加成員
     */
    public boolean addMember(UUID playerId, PartyRole role) {
        if (members.size() >= maxMembers) {
            return false;
        }
        
        if (members.containsKey(playerId)) {
            return false;
        }
        
        members.put(playerId, role);
        memberJoinTimes.put(playerId, System.currentTimeMillis());
        return true;
    }
    
    /**
     * 移除成員
     */
    public boolean removeMember(UUID playerId) {
        if (leaderId.equals(playerId)) {
            return false; // 隊長不能被移除，只能轉讓或解散
        }
        
        members.remove(playerId);
        memberJoinTimes.remove(playerId);
        return true;
    }
    
    /**
     * 轉讓隊長
     */
    public boolean transferLeadership(UUID newLeaderId) {
        if (!members.containsKey(newLeaderId)) {
            return false;
        }
        
        // 舊隊長變為軍官
        members.put(leaderId, PartyRole.OFFICER);
        // 新隊長
        members.put(newLeaderId, PartyRole.LEADER);
        
        return true;
    }
    
    /**
     * 提升成員角色
     */
    public boolean promoteMember(UUID playerId, PartyRole newRole) {
        if (!members.containsKey(playerId) || newRole == PartyRole.LEADER) {
            return false;
        }
        
        members.put(playerId, newRole);
        return true;
    }
    
    /**
     * 檢查玩家是否為成員
     */
    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }
    
    /**
     * 檢查玩家是否有特定權限
     */
    public boolean hasPermission(UUID playerId, PartyPermission permission) {
        PartyRole role = members.get(playerId);
        if (role == null) {
            return false;
        }
        
        return role.hasPermission(permission);
    }
    
    /**
     * 獲取在線成員
     */
    public List<UUID> getOnlineMembers() {
        List<UUID> onlineMembers = new ArrayList<>();
        for (UUID memberId : members.keySet()) {
            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                onlineMembers.add(memberId);
            }
        }
        return onlineMembers;
    }
    
    /**
     * 獲取成員在組隊中的時間（小時）
     */
    public long getMembershipDuration(UUID playerId) {
        Long joinTime = memberJoinTimes.get(playerId);
        if (joinTime == null) {
            return 0;
        }
        
        return (System.currentTimeMillis() - joinTime) / (60 * 60 * 1000);
    }
    
    /**
     * 檢查組隊是否為空（只有隊長）
     */
    public boolean isEmpty() {
        return members.size() <= 1;
    }
    
    /**
     * 檢查組隊是否已滿
     */
    public boolean isFull() {
        return members.size() >= maxMembers;
    }
    
    /**
     * 獲取組隊存在時間（小時）
     */
    public long getPartyAge() {
        return (System.currentTimeMillis() - createdTime) / (60 * 60 * 1000);
    }
    
    // === Getters 和 Setters ===
    
    public UUID getId() {
        return id;
    }
    
    public UUID getLeaderId() {
        return leaderId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public Map<UUID, PartyRole> getMembers() {
        return new HashMap<>(members);
    }
    
    public Map<UUID, Long> getMemberJoinTimes() {
        return new HashMap<>(memberJoinTimes);
    }
    
    public PartySettings getSettings() {
        return settings;
    }
    
    public void setSettings(PartySettings settings) {
        this.settings = settings;
    }
    
    public PartyStatus getStatus() {
        return status;
    }
    
    public void setStatus(PartyStatus status) {
        this.status = status;
    }
    
    public int getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(int maxMembers) {
        this.maxMembers = Math.max(2, Math.min(maxMembers, 20)); // 限制在2-20之間
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Party party = (Party) obj;
        return id.equals(party.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Party{id=%s, name='%s', leader=%s, members=%d}", 
            id, name, leaderId, members.size());
    }
    
    /**
     * 組隊角色枚舉
     */
    public enum PartyRole {
        LEADER("隊長", Arrays.asList(PartyPermission.values())),
        OFFICER("軍官", Arrays.asList(
            PartyPermission.INVITE_MEMBERS,
            PartyPermission.KICK_MEMBERS,
            PartyPermission.MANAGE_SETTINGS,
            PartyPermission.SEND_MESSAGES
        )),
        MEMBER("成員", Arrays.asList(
            PartyPermission.SEND_MESSAGES
        ));
        
        private final String displayName;
        private final List<PartyPermission> permissions;
        
        PartyRole(String displayName, List<PartyPermission> permissions) {
            this.displayName = displayName;
            this.permissions = permissions;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean hasPermission(PartyPermission permission) {
            return permissions.contains(permission);
        }
        
        public List<PartyPermission> getPermissions() {
            return new ArrayList<>(permissions);
        }
    }
    
    /**
     * 組隊權限枚舉
     */
    public enum PartyPermission {
        INVITE_MEMBERS("邀請成員"),
        KICK_MEMBERS("踢出成員"),
        PROMOTE_MEMBERS("提升成員"),
        TRANSFER_LEADERSHIP("轉讓隊長"),
        DISBAND_PARTY("解散組隊"),
        MANAGE_SETTINGS("管理設定"),
        SEND_MESSAGES("發送訊息");
        
        private final String displayName;
        
        PartyPermission(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 組隊狀態枚舉
     */
    public enum PartyStatus {
        ACTIVE("活躍"),
        INACTIVE("不活躍"),
        DISBANDED("已解散");
        
        private final String displayName;
        
        PartyStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 組隊設定類
     */
    public static class PartySettings {
        private boolean publicParty = false;
        private boolean allowInvites = true;
        private boolean friendlyFire = false;
        private boolean shareExperience = true;
        private boolean shareLoot = false;
        private String joinMessage = "歡迎加入組隊！";
        
        public boolean isPublicParty() {
            return publicParty;
        }
        
        public void setPublicParty(boolean publicParty) {
            this.publicParty = publicParty;
        }
        
        public boolean isAllowInvites() {
            return allowInvites;
        }
        
        public void setAllowInvites(boolean allowInvites) {
            this.allowInvites = allowInvites;
        }
        
        public boolean isFriendlyFire() {
            return friendlyFire;
        }
        
        public void setFriendlyFire(boolean friendlyFire) {
            this.friendlyFire = friendlyFire;
        }
        
        public boolean isShareExperience() {
            return shareExperience;
        }
        
        public void setShareExperience(boolean shareExperience) {
            this.shareExperience = shareExperience;
        }
        
        public boolean isShareLoot() {
            return shareLoot;
        }
        
        public void setShareLoot(boolean shareLoot) {
            this.shareLoot = shareLoot;
        }
        
        public String getJoinMessage() {
            return joinMessage;
        }
        
        public void setJoinMessage(String joinMessage) {
            this.joinMessage = joinMessage;
        }
    }
}