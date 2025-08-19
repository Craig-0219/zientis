package com.zientis.core.config;

import java.util.List;
import java.util.Set;

/**
 * 配置區段接口
 * 提供統一的配置存取方法
 */
public interface ConfigSection {
    
    /**
     * 獲取字串值
     */
    String getString(String path, String defaultValue);
    
    /**
     * 獲取字串值
     */
    String getString(String path);
    
    /**
     * 獲取整數值
     */
    int getInt(String path, int defaultValue);
    
    /**
     * 獲取整數值
     */
    int getInt(String path);
    
    /**
     * 獲取長整數值
     */
    long getLong(String path, long defaultValue);
    
    /**
     * 獲取長整數值
     */
    long getLong(String path);
    
    /**
     * 獲取布林值
     */
    boolean getBoolean(String path, boolean defaultValue);
    
    /**
     * 獲取布林值
     */
    boolean getBoolean(String path);
    
    /**
     * 獲取雙精度浮點值
     */
    double getDouble(String path, double defaultValue);
    
    /**
     * 獲取雙精度浮點值
     */
    double getDouble(String path);
    
    /**
     * 獲取字串列表
     */
    List<String> getStringList(String path);
    
    /**
     * 獲取整數列表
     */
    List<Integer> getIntegerList(String path);
    
    /**
     * 獲取子配置區段
     */
    ConfigSection getSection(String path);
    
    /**
     * 設定值
     */
    void set(String path, Object value);
    
    /**
     * 檢查路徑是否存在
     */
    boolean contains(String path);
    
    /**
     * 獲取所有鍵
     */
    Set<String> getKeys(boolean deep);
    
    /**
     * 移除指定路徑
     */
    void remove(String path);
}