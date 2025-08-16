package com.zientis.nations.data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 國家統計數據類
 * 
 * 記錄國家的各種統計信息和歷史數據
 */
public class NationStats {
    
    // 經濟統計
    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong totalTaxCollected = new AtomicLong(0);
    private double largestTransaction = 0.0;
    private LocalDateTime lastTransactionTime;
    
    // 軍事統計
    private final AtomicInteger warsDeclared = new AtomicInteger(0);
    private final AtomicInteger warsWon = new AtomicInteger(0);
    private final AtomicInteger warsLost = new AtomicInteger(0);
    private final AtomicInteger warsSurrendered = new AtomicInteger(0);
    private LocalDateTime lastWarTime;
    
    // 成員統計
    private final AtomicInteger totalMembersJoined = new AtomicInteger(0);
    private final AtomicInteger totalMembersLeft = new AtomicInteger(0);
    private final AtomicInteger totalMembersKicked = new AtomicInteger(0);
    private int peakMemberCount = 0;
    private LocalDateTime peakMemberTime;
    
    // 領土統計
    private final AtomicInteger territoriesClaimed = new AtomicInteger(0);
    private final AtomicInteger territoriesLost = new AtomicInteger(0);
    private int peakTerritoryCount = 0;
    private LocalDateTime peakTerritoryTime;
    
    // 外交統計
    private final AtomicInteger alliancesFormed = new AtomicInteger(0);
    private final AtomicInteger alliancesBroken = new AtomicInteger(0);
    private final AtomicInteger peacesTreaties = new AtomicInteger(0);
    
    // 發展統計
    private final AtomicInteger upgradesCompleted = new AtomicInteger(0);
    private LocalDateTime lastUpgradeTime;
    private long totalOnlineTime = 0; // 以分鐘為單位
    
    // Discord統計 (預留)
    private final AtomicLong discordMessages = new AtomicLong(0);
    private final AtomicInteger discordCommands = new AtomicInteger(0);
    private LocalDateTime lastDiscordActivity;
    
    public NationStats() {
        // 初始化時間戳
        LocalDateTime now = LocalDateTime.now();
        this.lastTransactionTime = now;
        this.lastWarTime = now;
        this.peakMemberTime = now;
        this.peakTerritoryTime = now;
        this.lastUpgradeTime = now;
        this.lastDiscordActivity = now;
    }
    
    // === 經濟統計方法 ===
    
    /**
     * 記錄交易
     */
    public void recordTransaction(double amount) {
        totalTransactions.incrementAndGet();
        lastTransactionTime = LocalDateTime.now();
        
        if (Math.abs(amount) > Math.abs(largestTransaction)) {
            largestTransaction = amount;
        }
    }
    
    /**
     * 記錄稅收收取
     */
    public void recordTaxCollection(double amount) {
        totalTaxCollected.addAndGet((long) amount);
        lastTransactionTime = LocalDateTime.now();
    }
    
    // === 軍事統計方法 ===
    
    /**
     * 記錄戰爭宣告
     */
    public void recordWarDeclaration() {
        warsDeclared.incrementAndGet();
        lastWarTime = LocalDateTime.now();
    }
    
    /**
     * 記錄戰爭結束
     */
    public void recordWarEnd(boolean victory) {
        if (victory) {
            warsWon.incrementAndGet();
        } else {
            warsLost.incrementAndGet();
        }
        lastWarTime = LocalDateTime.now();
    }
    
    /**
     * 記錄戰爭投降
     */
    public void recordWarSurrender() {
        warsSurrendered.incrementAndGet();
        warsLost.incrementAndGet(); // 投降也算失敗
        lastWarTime = LocalDateTime.now();
    }
    
    // === 成員統計方法 ===
    
    /**
     * 記錄成員加入
     */
    public void recordMemberJoin(int currentMemberCount) {
        totalMembersJoined.incrementAndGet();
        updatePeakMemberCount(currentMemberCount);
    }
    
    /**
     * 記錄成員離開
     */
    public void recordMemberLeave() {
        totalMembersLeft.incrementAndGet();
    }
    
    /**
     * 記錄成員被踢出
     */
    public void recordMemberKick() {
        totalMembersKicked.incrementAndGet();
        totalMembersLeft.incrementAndGet(); // 被踢也算離開
    }
    
    /**
     * 更新成員峰值
     */
    private void updatePeakMemberCount(int currentCount) {
        if (currentCount > peakMemberCount) {
            peakMemberCount = currentCount;
            peakMemberTime = LocalDateTime.now();
        }
    }
    
    // === 領土統計方法 ===
    
    /**
     * 記錄領土聲稱
     */
    public void recordTerritoryClaim(int currentTerritoryCount) {
        territoriesClaimed.incrementAndGet();
        updatePeakTerritoryCount(currentTerritoryCount);
    }
    
    /**
     * 記錄領土失去
     */
    public void recordTerritoryLoss() {
        territoriesLost.incrementAndGet();
    }
    
    /**
     * 更新領土峰值
     */
    private void updatePeakTerritoryCount(int currentCount) {
        if (currentCount > peakTerritoryCount) {
            peakTerritoryCount = currentCount;
            peakTerritoryTime = LocalDateTime.now();
        }
    }
    
    // === 外交統計方法 ===
    
    /**
     * 記錄聯盟建立
     */
    public void recordAllianceFormed() {
        alliancesFormed.incrementAndGet();
    }
    
    /**
     * 記錄聯盟破裂
     */
    public void recordAllianceBroken() {
        alliancesBroken.incrementAndGet();
    }
    
    /**
     * 記錄和平條約
     */
    public void recordPeaceTreaty() {
        peacesTreaties.incrementAndGet();
    }
    
    // === 發展統計方法 ===
    
    /**
     * 記錄升級完成
     */
    public void recordUpgrade() {
        upgradesCompleted.incrementAndGet();
        lastUpgradeTime = LocalDateTime.now();
    }
    
    /**
     * 記錄在線時間 (分鐘)
     */
    public void addOnlineTime(long minutes) {
        totalOnlineTime += minutes;
    }
    
    // === Discord統計方法 ===
    
    /**
     * 記錄Discord消息
     */
    public void recordDiscordMessage() {
        discordMessages.incrementAndGet();
        lastDiscordActivity = LocalDateTime.now();
    }
    
    /**
     * 記錄Discord指令
     */
    public void recordDiscordCommand() {
        discordCommands.incrementAndGet();
        lastDiscordActivity = LocalDateTime.now();
    }
    
    // === 計算方法 ===
    
    /**
     * 計算勝率
     */
    public double getWinRate() {
        int totalWars = warsWon.get() + warsLost.get();
        return totalWars > 0 ? (double) warsWon.get() / totalWars * 100 : 0.0;
    }
    
    /**
     * 計算成員留存率
     */
    public double getMemberRetentionRate() {
        int totalJoined = totalMembersJoined.get();
        int totalLeft = totalMembersLeft.get();
        return totalJoined > 0 ? (double) (totalJoined - totalLeft) / totalJoined * 100 : 100.0;
    }
    
    /**
     * 計算平均每日稅收 (假設建國30天)
     */
    public double getAverageDailyTax() {
        long daysSinceFoundation = Math.max(1, 30); // 簡化計算
        return (double) totalTaxCollected.get() / daysSinceFoundation;
    }
    
    /**
     * 計算軍事實力評分
     */
    public double getMilitaryScore() {
        double warBonus = warsWon.get() * 100 - warsLost.get() * 50;
        double declarationPenalty = warsDeclared.get() * 10; // 好戰懲罰
        return Math.max(0, warBonus - declarationPenalty);
    }
    
    /**
     * 計算外交實力評分
     */
    public double getDiplomaticScore() {
        double allianceBonus = alliancesFormed.get() * 50;
        double peacefulBonus = peacesTreaties.get() * 30;
        double instabilityPenalty = alliancesBroken.get() * 25;
        return Math.max(0, allianceBonus + peacefulBonus - instabilityPenalty);
    }
    
    /**
     * 計算總體實力評分
     */
    public double getTotalPowerScore() {
        double militaryScore = getMilitaryScore();
        double diplomaticScore = getDiplomaticScore();
        double economicScore = totalTaxCollected.get() * 0.01;
        double developmentScore = upgradesCompleted.get() * 100;
        
        return militaryScore + diplomaticScore + economicScore + developmentScore;
    }
    
    // === Getter 方法 ===
    
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
    
    @Override
    public String toString() {
        return String.format("NationStats{transactions=%d, wars=%d/%d, members=%d, territories=%d, powerScore=%.2f}", 
            totalTransactions.get(), warsWon.get(), warsLost.get(), 
            totalMembersJoined.get(), territoriesClaimed.get(), getTotalPowerScore());
    }
}