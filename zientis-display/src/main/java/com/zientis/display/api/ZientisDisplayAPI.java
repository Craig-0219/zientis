package com.zientis.display.api;

import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayUpdateType;
import com.zientis.display.discord.DiscordDisplayData;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 核心展示系統API
 */
public interface ZientisDisplayAPI {

    CompletableFuture<DisplayModel> createIslandDisplay(UUID islandId, Location center);

    CompletableFuture<DisplayModel> updateDisplayModel(UUID islandId, DisplayUpdateType updateType);

    CompletableFuture<Boolean> removeDisplay(UUID islandId);

    List<DisplayModel> getNearbyDisplays(Location center, int radius);

    DisplayModel getDisplayModel(UUID islandId);

    List<DisplayModel> getAllDisplays();

    CompletableFuture<Boolean> reloadDisplay(UUID islandId);

    CompletableFuture<Integer> batchUpdateDisplays(List<UUID> islandIds, DisplayUpdateType updateType);

    void setAutoUpdateInterval(String region, int intervalSeconds);

    DisplaySystemStats getSystemStats();

    // Discord Integration
    CompletableFuture<DiscordDisplayData> getDiscordDisplayData(UUID islandId);

    CompletableFuture<DiscordDisplayData> getDiscordDisplayDataByDiscordUser(String discordUserId);

    CompletableFuture<List<DiscordDisplayData>> getDiscordDisplayRanking(String criteria, int limit);

    CompletableFuture<List<DiscordDisplayData>> getDiscordDisplaysNeedingAttention();

    CompletableFuture<String> handleDiscordDisplayCommand(String command, String[] args, String discordUserId);

    CompletableFuture<Boolean> sendDiscordDisplayNotification(String eventType, UUID islandId, String message);

    CompletableFuture<String> getDiscordDisplayStats();

    /**
     * A data object holding statistics about the display system.
     */
    public static class DisplaySystemStats {

        private final int totalDisplays;
        private final int activeDisplays;
        private final long memoryUsage;
        private final double averageUpdateTime;

        public DisplaySystemStats(int totalDisplays, int activeDisplays, long memoryUsage, double averageUpdateTime) {
            this.totalDisplays = totalDisplays;
            this.activeDisplays = activeDisplays;
            this.memoryUsage = memoryUsage;
            this.averageUpdateTime = averageUpdateTime;
        }

        public int getTotalDisplays() {
            return totalDisplays;
        }

        public int getActiveDisplays() {
            return activeDisplays;
        }

        public long getMemoryUsage() {
            return memoryUsage;
        }

        public double getAverageUpdateTime() {
            return averageUpdateTime;
        }
    }
}
