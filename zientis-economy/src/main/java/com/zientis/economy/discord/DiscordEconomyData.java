package com.zientis.economy.discord;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Discord Bot API 經濟數據傳輸對象
 * 
 * 用於與Discord Bot進行經濟系統數據交換
 */
public class DiscordEconomyData {
    
    @JsonProperty("player_id")
    private final UUID playerId;
    
    @JsonProperty("balance")
    private final double balance;
    
    @JsonProperty("total_earned")
    private final double totalEarned;
    
    @JsonProperty("total_spent")
    private final double totalSpent;
    
    @JsonProperty("transaction_count")
    private final int transactionCount;
    
    @JsonProperty("last_transaction")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastTransaction;
    
    @JsonProperty("account_status")
    private final String accountStatus;
    
    @JsonProperty("wealth_rank")
    private final int wealthRank;
    
    @JsonProperty("recent_transactions")
    private final List<DiscordTransactionData> recentTransactions;
    
    public DiscordEconomyData(UUID playerId, double balance, double totalEarned, 
                            double totalSpent, int transactionCount, 
                            LocalDateTime lastTransaction, String accountStatus,
                            int wealthRank, List<DiscordTransactionData> recentTransactions) {
        this.playerId = playerId;
        this.balance = balance;
        this.totalEarned = totalEarned;
        this.totalSpent = totalSpent;
        this.transactionCount = transactionCount;
        this.lastTransaction = lastTransaction;
        this.accountStatus = accountStatus;
        this.wealthRank = wealthRank;
        this.recentTransactions = recentTransactions;
    }
    
    /**
     * 生成Discord嵌入式消息的描述
     */
    public String getDiscordDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append("💰 **餘額**: ").append(String.format("%.2f", balance)).append("\n");
        desc.append("📈 **總收入**: ").append(String.format("%.2f", totalEarned)).append("\n");
        desc.append("📉 **總支出**: ").append(String.format("%.2f", totalSpent)).append("\n");
        desc.append("🔄 **交易次數**: ").append(transactionCount).append("\n");
        desc.append("🏆 **財富排名**: #").append(wealthRank).append("\n");
        desc.append("📊 **帳戶狀態**: ").append(getStatusEmoji()).append(" ").append(accountStatus).append("\n");
        
        if (lastTransaction != null) {
            desc.append("🕒 **最後交易**: ").append(lastTransaction.toLocalDate()).append("\n");
        }
        
        return desc.toString();
    }
    
    /**
     * 生成簡化的Discord展示
     */
    public String getDiscordSummary() {
        return String.format("💰 **餘額**: %.2f | 🏆 排名: #%d | %s %s",
            balance, wealthRank, getStatusEmoji(), accountStatus);
    }
    
    /**
     * 獲取狀態表情符號
     */
    public String getStatusEmoji() {
        switch (accountStatus.toLowerCase()) {
            case "active": return "✅";
            case "frozen": return "🧊";
            case "suspended": return "⛔";
            case "vip": return "👑";
            default: return "⚪";
        }
    }
    
    /**
     * 獲取Discord顏色代碼 (十六進制)
     */
    public int getDiscordColor() {
        if (balance >= 1000000) {
            return 0xFFD700; // 金色 - 百萬富翁
        } else if (balance >= 100000) {
            return 0x9932CC; // 紫色 - 十萬富翁
        } else if (balance >= 10000) {
            return 0x1E90FF; // 藍色 - 萬元戶
        } else if (balance >= 1000) {
            return 0x32CD32; // 綠色 - 千元戶
        } else if (balance >= 0) {
            return 0xFFA500; // 橙色 - 正常
        } else {
            return 0xFF4500; // 紅色 - 負債
        }
    }
    
    /**
     * 生成財富排行榜條目
     */
    public String getRankingEntry(int rank, String playerName) {
        String medal = "";
        switch (rank) {
            case 1: medal = "🥇"; break;
            case 2: medal = "🥈"; break;
            case 3: medal = "🥉"; break;
            default: medal = String.format("#%d", rank); break;
        }
        
        return String.format("%s **%s** - %.2f\n📊 %d 筆交易 | %s %s",
            medal, playerName, balance, transactionCount, getStatusEmoji(), accountStatus);
    }
    
    // === Getter 方法 ===
    
    public UUID getPlayerId() { return playerId; }
    public double getBalance() { return balance; }
    public double getTotalEarned() { return totalEarned; }
    public double getTotalSpent() { return totalSpent; }
    public int getTransactionCount() { return transactionCount; }
    public LocalDateTime getLastTransaction() { return lastTransaction; }
    public String getAccountStatus() { return accountStatus; }
    public int getWealthRank() { return wealthRank; }
    public List<DiscordTransactionData> getRecentTransactions() { return recentTransactions; }
    
    /**
     * Discord交易數據內部類
     */
    public static class DiscordTransactionData {
        @JsonProperty("transaction_id")
        private final String transactionId;
        
        @JsonProperty("type")
        private final String type;
        
        @JsonProperty("amount")
        private final double amount;
        
        @JsonProperty("other_party")
        private final String otherParty;
        
        @JsonProperty("description")
        private final String description;
        
        @JsonProperty("timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime timestamp;
        
        public DiscordTransactionData(String transactionId, String type, double amount,
                                    String otherParty, String description, LocalDateTime timestamp) {
            this.transactionId = transactionId;
            this.type = type;
            this.amount = amount;
            this.otherParty = otherParty;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        /**
         * 生成Discord格式的交易條目
         */
        public String getDiscordEntry() {
            String emoji = getTypeEmoji();
            String amountStr = amount >= 0 ? String.format("+%.2f", amount) : String.format("%.2f", amount);
            String colorCode = amount >= 0 ? "```diff\n+" : "```diff\n";
            
            return String.format("%s **%s** %s\n%s %s```\n👤 %s | 🕒 %s",
                emoji, type, amountStr, colorCode, description, 
                otherParty != null ? otherParty : "系統", 
                timestamp.toLocalDate());
        }
        
        private String getTypeEmoji() {
            switch (type.toLowerCase()) {
                case "transfer": return "💸";
                case "deposit": return "💰";
                case "withdraw": return "💳";
                case "salary": return "💼";
                case "purchase": return "🛒";
                case "sale": return "💵";
                case "tax": return "🏛️";
                case "fine": return "⚖️";
                case "reward": return "🎁";
                default: return "💫";
            }
        }
        
        // Getter methods
        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getOtherParty() { return otherParty; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}