package com.zientis.discord.service;

import com.zientis.discord.dto.VoiceChannelConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Discord 語音頻道狀態更新服務
 * 提供動態更新語音頻道名稱以顯示伺服器即時狀態的功能
 */
@Service
public class VoiceChannelStatusService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoiceChannelStatusService.class);
    private static final DecimalFormat TPS_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,###");
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, VoiceChannelConfig> channelConfigs = new HashMap<>();
    private final Map<String, String> lastChannelNames = new HashMap<>();
    private final Map<String, AtomicInteger> failureCounters = new HashMap<>();
    
    private JDA jda;
    private boolean enabled = false;
    private int updateInterval = 120;
    private int maxRetries = 3;
    private int retryInterval = 30;
    private int maxFailures = 10;
    private boolean cacheEnabled = true;
    private int cacheDuration = 60;
    private boolean updateOnlyOnChange = true;
    
    /**
     * 初始化語音頻道狀態服務
     */
    public void initialize(JDA jda, Map<String, Object> config) {
        this.jda = jda;
        loadConfiguration(config);
        
        if (enabled && !channelConfigs.isEmpty()) {
            startStatusUpdateTask();
            logger.info("語音頻道狀態更新服務已啟動，更新間隔: {} 秒", updateInterval);
        } else {
            logger.info("語音頻道狀態更新服務已停用");
        }
    }
    
    /**
     * 載入配置設定
     */
    @SuppressWarnings("unchecked")
    private void loadConfiguration(Map<String, Object> config) {
        this.enabled = (Boolean) config.getOrDefault("enabled", false);
        this.updateInterval = (Integer) config.getOrDefault("update-interval", 120);
        
        // 載入錯誤處理設定
        Map<String, Object> errorHandling = (Map<String, Object>) config.getOrDefault("error-handling", new HashMap<>());
        this.maxRetries = (Integer) errorHandling.getOrDefault("max-retries", 3);
        this.retryInterval = (Integer) errorHandling.getOrDefault("retry-interval", 30);
        this.maxFailures = (Integer) errorHandling.getOrDefault("disable-after-failures", 10);
        
        // 載入快取設定
        Map<String, Object> cache = (Map<String, Object>) config.getOrDefault("cache", new HashMap<>());
        this.cacheEnabled = (Boolean) cache.getOrDefault("enabled", true);
        this.cacheDuration = (Integer) cache.getOrDefault("duration", 60);
        this.updateOnlyOnChange = (Boolean) cache.getOrDefault("update-only-on-change", true);
        
        // 載入頻道設定
        Map<String, Object> channels = (Map<String, Object>) config.getOrDefault("channels", new HashMap<>());
        loadChannelConfigs(channels);
    }
    
    /**
     * 載入頻道配置
     */
    @SuppressWarnings("unchecked")
    private void loadChannelConfigs(Map<String, Object> channels) {
        channelConfigs.clear();
        
        for (Map.Entry<String, Object> entry : channels.entrySet()) {
            String channelType = entry.getKey();
            Map<String, Object> channelData = (Map<String, Object>) entry.getValue();
            
            boolean channelEnabled = (Boolean) channelData.getOrDefault("enabled", false);
            String channelId = (String) channelData.getOrDefault("channel-id", "");
            String format = (String) channelData.getOrDefault("format", "");
            
            if (channelEnabled && !channelId.isEmpty() && !format.isEmpty()) {
                VoiceChannelConfig config = new VoiceChannelConfig();
                config.setChannelId(channelId);
                config.setFormat(format);
                config.setEnabled(true);
                
                channelConfigs.put(channelType, config);
                failureCounters.put(channelType, new AtomicInteger(0));
                
                logger.info("載入語音頻道配置: {} -> {}", channelType, channelId);
            }
        }
    }
    
    /**
     * 啟動狀態更新任務
     */
    private void startStatusUpdateTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAllChannelStatuses();
            } catch (Exception e) {
                logger.error("語音頻道狀態更新任務執行失敗", e);
            }
        }, 30, updateInterval, TimeUnit.SECONDS);
    }
    
    /**
     * 更新所有頻道狀態
     */
    private void updateAllChannelStatuses() {
        for (Map.Entry<String, VoiceChannelConfig> entry : channelConfigs.entrySet()) {
            String channelType = entry.getKey();
            VoiceChannelConfig config = entry.getValue();
            
            // 檢查是否因過多失敗而停用
            if (failureCounters.get(channelType).get() >= maxFailures) {
                continue;
            }
            
            CompletableFuture.runAsync(() -> updateChannelStatus(channelType, config));
        }
    }
    
    /**
     * 更新單個頻道狀態
     */
    private void updateChannelStatus(String channelType, VoiceChannelConfig config) {
        try {
            String newChannelName = generateChannelName(channelType, config.getFormat());
            
            // 如果啟用了快取且名稱沒有變化，則跳過更新
            if (updateOnlyOnChange && newChannelName.equals(lastChannelNames.get(channelType))) {
                return;
            }
            
            VoiceChannel channel = jda.getVoiceChannelById(config.getChannelId());
            if (channel == null) {
                logger.warn("找不到語音頻道: {}", config.getChannelId());
                return;
            }
            
            channel.getManager()
                    .setName(newChannelName)
                    .queue(
                            success -> {
                                lastChannelNames.put(channelType, newChannelName);
                                failureCounters.get(channelType).set(0);
                                logger.debug("成功更新頻道 {} 名稱為: {}", channelType, newChannelName);
                            },
                            new ErrorHandler()
                                    .handle(ErrorResponse.MISSING_PERMISSIONS, e -> {
                                        logger.warn("缺少權限更新頻道 {}: {}", channelType, e.getMessage());
                                        handleUpdateFailure(channelType);
                                    })
                                    .handle(ErrorResponse.RATE_LIMITED, e -> {
                                        logger.warn("更新頻道 {} 遇到速率限制，將稍後重試", channelType);
                                        scheduleRetry(channelType, config);
                                    })
                                    .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> {
                                        logger.error("頻道 {} 不存在: {}", channelType, config.getChannelId());
                                        handleUpdateFailure(channelType);
                                    })
                    );
                    
        } catch (Exception e) {
            logger.error("更新頻道 {} 狀態時發生錯誤", channelType, e);
            handleUpdateFailure(channelType);
        }
    }
    
    /**
     * 生成頻道名稱
     */
    private String generateChannelName(String channelType, String format) {
        Map<String, String> variables = new HashMap<>();
        
        // 通用變數
        variables.put("{server}", Bukkit.getServerName());
        variables.put("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        variables.put("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        
        // 伺服器狀態變數
        double tps = getTPS();
        variables.put("{tps}", TPS_FORMAT.format(tps));
        variables.put("{status}", tps > 18 ? "良好" : tps > 15 ? "普通" : "緩慢");
        
        // 記憶體使用變數
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        variables.put("{memory}", usedMemory + "/" + maxMemory + "MB");
        variables.put("{memory-percent}", String.valueOf((usedMemory * 100) / maxMemory) + "%");
        
        // 經濟變數 (需要經濟插件支援)
        try {
            double totalMoney = getTotalEconomyMoney();
            variables.put("{total-money}", "$" + MONEY_FORMAT.format(totalMoney));
        } catch (Exception e) {
            variables.put("{total-money}", "N/A");
        }
        
        // 自訂變數
        variables.put("{custom1}", getCustomVariable("custom1"));
        variables.put("{custom2}", getCustomVariable("custom2"));
        variables.put("{custom3}", getCustomVariable("custom3"));
        
        // 替換變數
        String result = format;
        for (Map.Entry<String, String> var : variables.entrySet()) {
            result = result.replace(var.getKey(), var.getValue());
        }
        
        return result;
    }
    
    /**
     * 獲取伺服器 TPS
     */
    private double getTPS() {
        try {
            // 使用反射獲取 TPS，相容不同版本
            Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
            return Math.min(20.0, recentTps[0]);
        } catch (Exception e) {
            logger.debug("無法獲取 TPS，返回預設值", e);
            return 20.0;
        }
    }
    
    /**
     * 獲取總經濟金額
     */
    private double getTotalEconomyMoney() {
        // 這裡需要與經濟插件整合
        // 暫時返回模擬數據
        return 1000000.0;
    }
    
    /**
     * 獲取自訂變數
     */
    private String getCustomVariable(String variableName) {
        // 這裡可以實作自訂變數邏輯
        return "N/A";
    }
    
    /**
     * 處理更新失敗
     */
    private void handleUpdateFailure(String channelType) {
        int failures = failureCounters.get(channelType).incrementAndGet();
        
        if (failures >= maxFailures) {
            logger.error("頻道 {} 更新失敗次數達到上限 ({})，已停用此頻道更新", channelType, maxFailures);
        } else {
            logger.warn("頻道 {} 更新失敗，失敗次數: {}/{}", channelType, failures, maxFailures);
        }
    }
    
    /**
     * 排程重試
     */
    private void scheduleRetry(String channelType, VoiceChannelConfig config) {
        scheduler.schedule(() -> {
            updateChannelStatus(channelType, config);
        }, retryInterval, TimeUnit.SECONDS);
    }
    
    /**
     * 手動更新指定頻道
     */
    public CompletableFuture<Boolean> updateChannel(String channelType) {
        VoiceChannelConfig config = channelConfigs.get(channelType);
        if (config == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateChannelStatus(channelType, config);
                return true;
            } catch (Exception e) {
                logger.error("手動更新頻道 {} 失敗", channelType, e);
                return false;
            }
        });
    }
    
    /**
     * 重置失敗計數器
     */
    public void resetFailureCount(String channelType) {
        AtomicInteger counter = failureCounters.get(channelType);
        if (counter != null) {
            counter.set(0);
            logger.info("已重置頻道 {} 的失敗計數器", channelType);
        }
    }
    
    /**
     * 獲取頻道更新狀態
     */
    public Map<String, Object> getChannelStatus() {
        Map<String, Object> status = new HashMap<>();
        
        for (String channelType : channelConfigs.keySet()) {
            Map<String, Object> channelStatus = new HashMap<>();
            channelStatus.put("enabled", true);
            channelStatus.put("failures", failureCounters.get(channelType).get());
            channelStatus.put("lastUpdate", lastChannelNames.get(channelType));
            channelStatus.put("disabled", failureCounters.get(channelType).get() >= maxFailures);
            
            status.put(channelType, channelStatus);
        }
        
        return status;
    }
    
    /**
     * 關閉服務
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                logger.info("語音頻道狀態更新服務已關閉");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}