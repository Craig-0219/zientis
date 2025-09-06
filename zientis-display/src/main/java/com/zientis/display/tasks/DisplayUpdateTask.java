package com.zientis.display.tasks;

import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayStatus;
import com.zientis.display.data.DisplayUpdateType;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 展示更新定時任務
 * 
 * 負責定期檢查和更新島嶼展示
 */
public class DisplayUpdateTask extends BukkitRunnable {
    
    private static final Logger logger = Logger.getLogger(DisplayUpdateTask.class.getName());
    
    private final ZientisDisplayAPI displayAPI;
    
    // 配置參數
    private static final int AUTO_UPDATE_INTERVAL_HOURS = 6; // 自動更新間隔(小時)
    private static final int ERROR_RETRY_MINUTES = 30; // 錯誤重試間隔(分鐘)
    private static final int INACTIVE_CHECK_HOURS = 24; // 非活躍檢查間隔(小時)
    
    // 統計計數器
    private int taskRunCount = 0;
    private int updatesPerformed = 0;
    private int errorsFixed = 0;

    public DisplayUpdateTask(ZientisDisplayAPI displayAPI) {
        this.displayAPI = displayAPI;
    }

    @Override
    public void run() {
        try {
            taskRunCount++;
            logger.info("執行展示系統定時更新 #" + taskRunCount);
            
            long startTime = System.currentTimeMillis();
            
            // 1. 檢查並修復錯誤狀態的展示
            checkAndFixErrorDisplays();
            
            // 2. 執行定期自動更新
            performScheduledUpdates();
            
            // 3. 清理非活躍展示
            cleanupInactiveDisplays();
            
            // 4. 系統健康檢查
            performSystemHealthCheck();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("定時更新完成，耗時: " + duration + "ms");
            
        } catch (Exception e) {
            logger.severe("定時更新任務發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 檢查並修復錯誤狀態的展示
     */
    private void checkAndFixErrorDisplays() {
        List<DisplayModel> errorDisplays = displayAPI.getAllDisplays().stream()
            .filter(model -> model.getStatus() == DisplayStatus.ERROR)
            .collect(Collectors.toList());
        
        if (errorDisplays.isEmpty()) {
            return;
        }
        
        logger.info("發現 " + errorDisplays.size() + " 個錯誤狀態的展示，嘗試修復...");
        
        for (DisplayModel display : errorDisplays) {
            LocalDateTime lastUpdated = display.getLastUpdated();
            long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdated, LocalDateTime.now());
            
            // 如果錯誤狀態持續超過重試間隔，嘗試修復
            if (minutesSinceLastUpdate >= ERROR_RETRY_MINUTES) {
                attemptErrorRecovery(display);
            }
        }
    }

    /**
     * 執行定期自動更新
     */
    private void performScheduledUpdates() {
        List<DisplayModel> activeDisplays = displayAPI.getAllDisplays().stream()
            .filter(model -> model.getStatus() == DisplayStatus.ACTIVE)
            .collect(Collectors.toList());
        
        if (activeDisplays.isEmpty()) {
            return;
        }
        
        logger.info("檢查 " + activeDisplays.size() + " 個活躍展示的更新需求...");
        
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;
        
        for (DisplayModel display : activeDisplays) {
            LocalDateTime lastUpdated = display.getLastUpdated();
            long hoursSinceLastUpdate = ChronoUnit.HOURS.between(lastUpdated, now);
            
            // 如果超過自動更新間隔，執行增量更新
            if (hoursSinceLastUpdate >= AUTO_UPDATE_INTERVAL_HOURS) {
                scheduleDisplayUpdate(display, DisplayUpdateType.INCREMENTAL);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            logger.info("排程 " + updatedCount + " 個展示進行自動更新");
            updatesPerformed += updatedCount;
        }
    }

    /**
     * 清理非活躍展示
     */
    private void cleanupInactiveDisplays() {
        List<DisplayModel> inactiveDisplays = displayAPI.getAllDisplays().stream()
            .filter(model -> model.getStatus() == DisplayStatus.PAUSED || model.getStatus() == DisplayStatus.HIDDEN)
            .collect(Collectors.toList());
        
        if (inactiveDisplays.isEmpty()) {
            return;
        }
        
        logger.info("發現 " + inactiveDisplays.size() + " 個非活躍展示");
        
        LocalDateTime now = LocalDateTime.now();
        int cleanedCount = 0;
        
        for (DisplayModel display : inactiveDisplays) {
            LocalDateTime lastUpdated = display.getLastUpdated();
            long hoursSinceLastUpdate = ChronoUnit.HOURS.between(lastUpdated, now);
            
            // 如果非活躍狀態持續過久，考慮清理或重新激活
            if (hoursSinceLastUpdate >= INACTIVE_CHECK_HOURS) {
                // TODO: 實現更複雜的清理邏輯
                logger.info("非活躍展示需要處理: " + display.getIslandId());
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            logger.info("處理了 " + cleanedCount + " 個長期非活躍的展示");
        }
    }

    /**
     * 執行系統健康檢查
     */
    private void performSystemHealthCheck() {
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        
        // 記憶體使用檢查
        long memoryUsageMB = stats.getMemoryUsage() / 1024 / 1024;
        if (memoryUsageMB > 500) { // 超過 500MB
            logger.warning("展示系統記憶體使用過高: " + memoryUsageMB + " MB");
        }
        
        // 平均更新時間檢查
        if (stats.getAverageUpdateTime() > 5000) { // 超過 5 秒
            logger.warning("展示更新時間過長: " + stats.getAverageUpdateTime() + " ms");
        }
        
        // 錯誤率檢查
        int totalDisplays = stats.getTotalDisplays();
        int activeDisplays = stats.getActiveDisplays();
        
        if (totalDisplays > 0) {
            double errorRate = (double) (totalDisplays - activeDisplays) / totalDisplays;
            if (errorRate > 0.1) { // 錯誤率超過 10%
                logger.warning("展示系統錯誤率過高: " + String.format("%.1f%%", errorRate * 100));
            }
        }
        
        // 每10次任務執行輸出一次詳細統計
        if (taskRunCount % 10 == 0) {
            logger.info("=== 展示系統統計 ===");
            logger.info("任務執行次數: " + taskRunCount);
            logger.info("累計更新次數: " + updatesPerformed);
            logger.info("錯誤修復次數: " + errorsFixed);
            logger.info("總展示數: " + stats.getTotalDisplays());
            logger.info("活躍展示: " + stats.getActiveDisplays());
            logger.info("記憶體使用: " + memoryUsageMB + " MB");
        }
    }

    /**
     * 嘗試錯誤恢復
     */
    private void attemptErrorRecovery(DisplayModel display) {
        logger.info("嘗試修復錯誤展示: " + display.getIslandId());
        
        CompletableFuture.supplyAsync(() -> {
            try {
                // 嘗試強制刷新
                return displayAPI.updateDisplayModel(display.getIslandId(), DisplayUpdateType.FORCE_REFRESH).join();
            } catch (Exception e) {
                logger.warning("修復展示失敗: " + display.getIslandId() + " - " + e.getMessage());
                return null;
            }
        }).thenAccept(result -> {
            if (result != null && result.getStatus() == DisplayStatus.ACTIVE) {
                logger.info("成功修復展示: " + display.getIslandId());
                errorsFixed++;
            } else {
                logger.warning("無法修復展示: " + display.getIslandId());
            }
        });
    }

    /**
     * 排程展示更新
     */
    private void scheduleDisplayUpdate(DisplayModel display, DisplayUpdateType updateType) {
        CompletableFuture.supplyAsync(() -> {
            return displayAPI.updateDisplayModel(display.getIslandId(), updateType).join();
        }).thenAccept(result -> {
            if (result != null) {
                logger.fine("自動更新完成: " + display.getIslandId() + " (" + updateType + ")");
            }
        }).exceptionally(throwable -> {
            logger.warning("自動更新失敗: " + display.getIslandId() + " - " + throwable.getMessage());
            return null;
        });
    }

    /**
     * 停止任務
     */
    public void stop() {
        try {
            cancel();
            logger.info("展示更新定時任務已停止");
        } catch (IllegalStateException e) {
            // 任務已經被取消
        }
    }

    /**
     * 獲取任務統計
     */
    public TaskStats getTaskStats() {
        return new TaskStats(taskRunCount, updatesPerformed, errorsFixed);
    }

    /**
     * 任務統計類
     */
    public static class TaskStats {
        private final int taskRunCount;
        private final int updatesPerformed;
        private final int errorsFixed;
        
        public TaskStats(int taskRunCount, int updatesPerformed, int errorsFixed) {
            this.taskRunCount = taskRunCount;
            this.updatesPerformed = updatesPerformed;
            this.errorsFixed = errorsFixed;
        }
        
        public int getTaskRunCount() { return taskRunCount; }
        public int getUpdatesPerformed() { return updatesPerformed; }
        public int getErrorsFixed() { return errorsFixed; }
        
        @Override
        public String toString() {
            return String.format("TaskStats{runs=%d, updates=%d, fixes=%d}", 
                taskRunCount, updatesPerformed, errorsFixed);
        }
    }
}