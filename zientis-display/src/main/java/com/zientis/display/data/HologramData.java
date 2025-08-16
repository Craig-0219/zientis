package com.zientis.display.data;

import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 全息圖數據類
 * 
 * 包含全息圖的所有配置和內容信息
 */
public class HologramData {
    
    private final List<String> lines;
    private final Location location;
    private final double lineSpacing;
    private final boolean visibleThroughWalls;
    private final double viewDistance;
    private LocalDateTime lastUpdated;

    public HologramData(Location location) {
        this.lines = new ArrayList<>();
        this.location = location.clone();
        this.lineSpacing = 0.25; // 默認行距
        this.visibleThroughWalls = false;
        this.viewDistance = 64.0; // 默認可見距離
        this.lastUpdated = LocalDateTime.now();
    }

    public HologramData(Location location, List<String> lines) {
        this.lines = new ArrayList<>(lines);
        this.location = location.clone();
        this.lineSpacing = 0.25;
        this.visibleThroughWalls = false;
        this.viewDistance = 64.0;
        this.lastUpdated = LocalDateTime.now();
    }

    public HologramData(Location location, List<String> lines, double lineSpacing, 
                       boolean visibleThroughWalls, double viewDistance) {
        this.lines = new ArrayList<>(lines);
        this.location = location.clone();
        this.lineSpacing = lineSpacing;
        this.visibleThroughWalls = visibleThroughWalls;
        this.viewDistance = viewDistance;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 添加一行文字到全息圖
     */
    public void addLine(String line) {
        lines.add(line);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 在指定位置插入一行文字
     */
    public void insertLine(int index, String line) {
        if (index >= 0 && index <= lines.size()) {
            lines.add(index, line);
            this.lastUpdated = LocalDateTime.now();
        }
    }

    /**
     * 移除指定行
     */
    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            this.lastUpdated = LocalDateTime.now();
        }
    }

    /**
     * 更新指定行的內容
     */
    public void updateLine(int index, String newLine) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, newLine);
            this.lastUpdated = LocalDateTime.now();
        }
    }

    /**
     * 清空所有行
     */
    public void clearLines() {
        lines.clear();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 設置所有行內容
     */
    public void setLines(List<String> newLines) {
        lines.clear();
        lines.addAll(newLines);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 獲取指定行的內容
     */
    public String getLine(int index) {
        if (index >= 0 && index < lines.size()) {
            return lines.get(index);
        }
        return "";
    }

    /**
     * 檢查是否為空
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * 獲取行數
     */
    public int getLineCount() {
        return lines.size();
    }

    /**
     * 標記為已更新
     */
    public void markAsUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    // === Getters ===
    
    public List<String> getLines() { 
        return new ArrayList<>(lines); 
    }
    
    public Location getLocation() { 
        return location.clone(); 
    }
    
    public double getLineSpacing() { 
        return lineSpacing; 
    }
    
    public boolean isVisibleThroughWalls() { 
        return visibleThroughWalls; 
    }
    
    public double getViewDistance() { 
        return viewDistance; 
    }
    
    public LocalDateTime getLastUpdated() { 
        return lastUpdated; 
    }

    /**
     * 創建副本
     */
    public HologramData clone() {
        return new HologramData(location, lines, lineSpacing, visibleThroughWalls, viewDistance);
    }

    @Override
    public String toString() {
        return String.format("HologramData{lines=%d, location=%s, spacing=%.2f, viewDistance=%.1f}", 
            lines.size(), location, lineSpacing, viewDistance);
    }
}