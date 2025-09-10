package com.zientis.nations.data;

import com.zientis.nations.data.NationPermission;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 國家數據模型
 */
public class Nation {
    
    private final UUID id;
    private String name;
    private final UUID founderId;
    private final LocalDateTime createdTime;
    
    private String description;
    private NationLevel level;
    
    private final Map<UUID, NationRole> members;
    private int maxMembers;
    
    private final Set<UUID> territories;
    private UUID capitalIsland;
    
    private double treasury;
    
    public Nation(UUID id, String name, UUID founderId) {
        this(id, name, founderId, "");
    }

    public Nation(UUID id, String name, UUID founderId, String description) {
        this.id = id;
        this.name = name;
        this.founderId = founderId;
        this.description = description;
        this.createdTime = LocalDateTime.now();
        
        this.members = new ConcurrentHashMap<>();
        this.territories = ConcurrentHashMap.newKeySet();
        this.level = NationLevel.SETTLEMENT;
        this.maxMembers = 5;
        this.treasury = 0.0;
        
        addMember(founderId, NationRole.KING);
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getFounderId() { return founderId; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public NationLevel getLevel() { return level; }
    public Set<UUID> getMembers() { return members.keySet(); }
    public Map<UUID, NationRole> getMemberRoles() { return Map.copyOf(members); }
    public double getTreasury() { return treasury; }
    public int getTerritoryCount() { return territories.size(); }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLevel(NationLevel level) { this.level = level; }

    // --- Member Management ---
    public void addMember(UUID playerId, NationRole role) {
        members.put(playerId, role);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public boolean hasPermission(UUID playerId, NationPermission permission) {
        NationRole role = members.get(playerId);
        return role != null && role.hasPermission(permission);
    }

    public boolean setMemberRole(UUID playerId, NationRole role) {
        if (members.containsKey(playerId)) {
            members.put(playerId, role);
            return true;
        }
        return false;
    }

    // --- Treasury Management ---
    public void addToTreasury(double amount) {
        if (amount > 0) {
            this.treasury += amount;
        }
    }

    public boolean removeFromTreasury(double amount) {
        if (amount > 0 && this.treasury >= amount) {
            this.treasury -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nation nation = (Nation) o;
        return id.equals(nation.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * 檢查國家是否活躍
     */
    public boolean isActive() {
        // Assuming lastActivity field exists and is updated elsewhere
        // For now, a simple placeholder logic
        return true; // Placeholder
    }
}