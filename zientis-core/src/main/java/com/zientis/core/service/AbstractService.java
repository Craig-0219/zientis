package com.zientis.core.service;

import org.bukkit.plugin.Plugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 抽象服務基類
 * 提供服務的基本實現和生命週期管理
 */
public abstract class AbstractService implements Service {
    
    protected final Plugin plugin;
    protected final Logger logger;
    protected final AtomicBoolean running;
    private final String name;
    private final String version;
    
    protected AbstractService(Plugin plugin, String name, String version) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.running = new AtomicBoolean(false);
        this.name = name;
        this.version = version;
    }
    
    @Override
    public final String getName() {
        return name;
    }
    
    @Override
    public final String getVersion() {
        return version;
    }
    
    @Override
    public final void initialize() {
        if (running.compareAndSet(false, true)) {
            try {
                logger.info("正在初始化服務: " + name);
                onInitialize();
                logger.info("服務初始化完成: " + name);
            } catch (Exception e) {
                running.set(false);
                logger.severe("服務初始化失敗: " + name + " - " + e.getMessage());
                throw new RuntimeException("服務初始化失敗", e);
            }
        } else {
            logger.warning("服務已經在運行: " + name);
        }
    }
    
    @Override
    public final void shutdown() {
        if (running.compareAndSet(true, false)) {
            try {
                logger.info("正在關閉服務: " + name);
                onShutdown();
                logger.info("服務關閉完成: " + name);
            } catch (Exception e) {
                logger.severe("服務關閉時發生錯誤: " + name + " - " + e.getMessage());
            }
        } else {
            logger.warning("服務已經停止: " + name);
        }
    }
    
    @Override
    public final boolean isRunning() {
        return running.get();
    }
    
    @Override
    public String getStatus() {
        if (isRunning()) {
            return "運行中";
        } else {
            return "已停止";
        }
    }
    
    /**
     * 子類需要實現的初始化邏輯
     */
    protected abstract void onInitialize() throws Exception;
    
    /**
     * 子類需要實現的關閉邏輯
     */
    protected abstract void onShutdown() throws Exception;
    
    /**
     * 檢查服務是否已初始化
     */
    protected final void ensureInitialized() {
        if (!isRunning()) {
            throw new IllegalStateException("服務尚未初始化: " + name);
        }
    }
    
    /**
     * 安全執行操作（捕獲異常）
     */
    protected final void safeExecute(String operation, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.severe("執行操作失敗 (" + operation + "): " + e.getMessage());
        }
    }
}