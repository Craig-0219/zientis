package com.zientis.nations.data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 國家統計數據類
 * 
 * 記錄國家的各種統計信息和歷史數據
 * 同時作為API返回的統計快照
 */
public class NationStats {
    
    // 內部計數器 (用於記錄)
    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong totalTaxCollected = new AtomicLong(0);
    private double largestTransaction = 0.0;
    private LocalDateTime lastTransactionTime;
    
    private final AtomicInteger warsDeclared = new AtomicInteger(0);
    private final AtomicInteger warsWon = new AtomicInteger(0);
    private final AtomicInteger warsLost = new AtomicInteger(0);
    private final AtomicInteger warsSurrendered = new AtomicInteger(0);
    private LocalDateTime lastWarTime;
    
    private final AtomicInteger totalMembersJoined = new AtomicInteger(0);
    private final AtomicInteger totalMembersLeft = new AtomicInteger(0);
    private final AtomicInteger totalMembersKicked = new AtomicInteger(0);
    private int peakMemberCount = 0;
    private LocalDateTime peakMemberTime;
    
    private final AtomicInteger territoriesClaimed = new AtomicInteger(0);
    private final AtomicInteger territoriesLost = new AtomicInteger(0);
    private int peakTerritoryCount = 0;
    private LocalDateTime peakTerritoryTime;
    
    private final AtomicInteger alliancesFormed = new AtomicInteger(0);
    private final AtomicInteger alliancesBroken = new AtomicInteger(0);
    private final AtomicInteger peacesTreaties = new AtomicInteger(0);
    
    private final AtomicInteger upgradesCompleted = new AtomicInteger(0);
    private LocalDateTime lastUpgradeTime;
    private long totalOnlineTime = 0; // 以分鐘為單位
    
    private final AtomicLong discordMessages = new AtomicLong(0);
    private final AtomicInteger discordCommands = new AtomicInteger(0);
    private LocalDateTime lastDiscordActivity;

    // 快照數據 (用於API返回)
    private final UUID nationId;
    private final int memberCount;
    private final double treasury;
    private final int territoryCount;
    private final int allianceCount;
    private final int warCount;
    private final double powerScore;
    private final NationLevel level;
    
    // 帶參構造函數 (用於API返回統計快照)
    public NationStats(UUID nationId, int memberCount, double treasury, int territoryCount,
                       int allianceCount, int warCount, double powerScore, NationLevel level) {
        this.nationId = nationId;
        this.memberCount = memberCount;
        this.treasury = treasury;
        this.territoryCount = territoryCount;
        this.allianceCount = allianceCount;
        this.warCount = warCount;
        this.powerScore = powerScore;
        this.level = level;
    }
    
    // === 經濟統計方法 ===
    public void recordTransaction(double amount) {
        totalTransactions.incrementAndGet();
        lastTransactionTime = LocalDateTime.now();
        if (Math.abs(amount) > Math.abs(largestTransaction)) {
            largestTransaction = amount;
        }
    }
    public void recordTaxCollection(double amount) {
        totalTaxCollected.addAndGet((long) amount);
        lastTransactionTime = LocalDateTime.now();
    }
    
    // === 軍事統計方法 ===
    public void recordWarDeclaration() {
        warsDeclared.incrementAndGet();
        lastWarTime = LocalDateTime.now();
    }
    public void recordWarEnd(boolean victory) {
        if (victory) { warsWon.incrementAndGet(); } else { warsLost.incrementAndGet(); }
        lastWarTime = LocalDateTime.now();
    }
    public void recordWarSurrender() {
        warsSurrendered.incrementAndGet();
        warsLost.incrementAndGet();
        lastWarTime = LocalDateTime.now();
    }
    
    // === 成員統計方法 ===
    public void recordMemberJoin(int currentMemberCount) {
        totalMembersJoined.incrementAndGet();
        updatePeakMemberCount(currentMemberCount);
    }
    public void recordMemberLeave() { totalMembersLeft.incrementAndGet(); }
    public void recordMemberKick() {
        totalMembersKicked.incrementAndGet();
        totalMembersLeft.incrementAndGet();
    }
    private void updatePeakMemberCount(int currentCount) {
        if (currentCount > peakMemberCount) {
            peakMemberCount = currentCount;
            peakMemberTime = LocalDateTime.now();
        }
    }
    
    // === 領土統計方法 ===
    public void recordTerritoryClaim(int currentTerritoryCount) {
        territoriesClaimed.incrementAndGet();
        updatePeakTerritoryCount(currentTerritoryCount);
    }
    public void recordTerritoryLoss() { territoriesLost.incrementAndGet(); }
    private void updatePeakTerritoryCount(int currentCount) {
        if (currentCount > peakTerritoryCount) {
            peakTerritoryCount = currentCount;
            peakTerritoryTime = LocalDateTime.now();
        }
    }
    
    // === 外交統計方法 ===
    public void recordAllianceFormed() { alliancesFormed.incrementAndGet(); }
    public void recordAllianceBroken() { alliancesBroken.incrementAndGet(); }
    public void recordPeaceTreaty() { peacesTreaties.incrementAndGet(); }
    
    // === 發展統計方法 ===
    public void recordUpgrade() {
        upgradesCompleted.incrementAndGet();
        lastUpgradeTime = LocalDateTime.now();
    }
    public void addOnlineTime(long minutes) { totalOnlineTime += minutes; }
    
    // === Discord統計方法 ===
    public void recordDiscordMessage() { discordMessages.incrementAndGet(); lastDiscordActivity = LocalDateTime.now(); }
    public void recordDiscordCommand() { discordCommands.incrementAndGet(); lastDiscordActivity = LocalDateTime.now(); }
    
    // === 計算方法 ===
    public double getWinRate() {
        int totalWars = warsWon.get() + warsLost.get();
        return totalWars > 0 ? (double) warsWon.get() / totalWars * 100 : 0.0;
    }
    public double getMemberRetentionRate() {
        int totalJoined = totalMembersJoined.get();
        int totalLeft = totalMembersLeft.get();
        return totalJoined > 0 ? (double) (totalJoined - totalLeft) / totalJoined * 100 : 100.0;
    }
    public double getAverageDailyTax() {
        long daysSinceFoundation = Math.max(1, 30);
        return (double) totalTaxCollected.get() / daysSinceFoundation;
    }
    public double getMilitaryScore() {
        double warBonus = warsWon.get() * 100 - warsLost.get() * 50;
        double declarationPenalty = warsDeclared.get() * 10;
        return Math.max(0, warBonus - declarationPenalty);
    }
    public double getDiplomaticScore() {
        double allianceBonus = alliancesFormed.get() * 50;
        double peacefulBonus = peacesTreaties.get() * 30;
        double instabilityPenalty = alliancesBroken.get() * 25;
        return Math.max(0, allianceBonus + peacefulBonus - instabilityPenalty);
    }
    public double getTotalPowerScore() {
        double militaryScore = getMilitaryScore();
        double diplomaticScore = getDiplomaticScore();
        double economicScore = totalTaxCollected.get() * 0.01;
        double developmentScore = upgradesCompleted.get() * 100;
        return militaryScore + economicScore + developmentScore + diplomaticScore;
    }
    
    // === Getter 方法 (內部計數器)
    public long getTotalTransactions() { return totalTransactions.get(); }
    public long getTotalTaxCollected() { return totalTaxCollected.get(); }
    public double getLargestTransaction() { return largestTransaction; }
    public LocalDateTime getLastTransactionTime() { return lastTransactionTime; }
    public int getWarsDeclared() { return warsDeclared.get(); }
    public int getWarsWon() { return warsWon.get(); }
    public int getWarsLost() { return warsLost.get(); }
    public int getWarsSurrendered() { return warsSurrendered.get(); }
    public LocalDateTime getLastWarTime() { return lastWarTime; }
    public int getTotalMembersJoined() { return totalMembersJoined.get(); }
    public int getTotalMembersLeft() { return totalMembersLeft.get(); }
    public int getTotalMembersKicked() { return totalMembersKicked.get(); }
    public int getPeakMemberCount() { return peakMemberCount; }
    public LocalDateTime getPeakMemberTime() { return peakMemberTime; }
    public int getTerritoriesClaimed() { return territoriesClaimed.get(); }
    public int getTerritoriesLost() { return territoriesLost.get(); }
    public int getPeakTerritoryCount() { return peakTerritoryCount; }
    public LocalDateTime getPeakTerritoryTime() { return peakTerritoryTime; }
    public int getAlliancesFormed() { return alliancesFormed.get(); }
    public int getAlliancesBroken() { return alliancesBroken.get(); }
    public int getPeacesTreaties() { return peacesTreaties.get(); }
    public int getUpgradesCompleted() { return upgradesCompleted.get(); }
    public LocalDateTime getLastUpgradeTime() { return lastUpgradeTime; }
    public long getTotalOnlineTime() { return totalOnlineTime; }
    public long getDiscordMessages() { return discordMessages.get(); }
    public int getDiscordCommands() { return discordCommands.get(); }
    public LocalDateTime getLastDiscordActivity() { return lastDiscordActivity; }

    // === Getter 方法 (快照數據)
    public UUID getNationId() { return nationId; }
    public int getMemberCount() { return memberCount; }
    public double getTreasury() { return treasury; }
    public int getTerritoryCount() { return territoryCount; }
    public int getAllianceCount() { return allianceCount; }
    public int getWarCount() { return warCount; }
    public double getPower() { return powerScore; }
    public NationLevel getLevel() { return level; }
    
    @Override
    public String toString() {
        return String.format("NationStats{id=%s, members=%d, treasury=%.2f, territories=%d, level=%s, power=%.2f}", 
            nationId, memberCount, treasury, territoryCount, level, powerScore);
    }
}