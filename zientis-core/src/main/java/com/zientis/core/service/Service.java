package com.zientis.core.service;

/**
 * 核心服務接口
 * 所有Zientis服務必須實現此接口
 */
public interface Service {
    
    /**
     * 獲取服務名稱
     */
    String getName();
    
    /**
     * 獲取服務版本
     */
    String getVersion();
    
    /**
     * 服務初始化
     */
    void initialize();
    
    /**
     * 服務關閉
     */
    void shutdown();
    
    /**
     * 檢查服務是否正在運行
     */
    boolean isRunning();
    
    /**
     * 獲取服務狀態描述
     */
    String getStatus();
}