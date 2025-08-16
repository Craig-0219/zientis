package com.zientis.display.data;

import org.bukkit.Location;

/**
 * 邊界框類
 * 
 * 表示展示模型的3D邊界範圍
 */
public class BoundingBox {
    
    private final Location min;
    private final Location max;

    public BoundingBox(Location min, Location max) {
        this.min = min.clone();
        this.max = max.clone();
        
        // 確保min實際上是最小值，max是最大值
        normalize();
    }

    /**
     * 標準化邊界框，確保min/max的正確性
     */
    private void normalize() {
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY());
        double maxY = Math.max(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        min.setX(minX);
        min.setY(minY);
        min.setZ(minZ);
        max.setX(maxX);
        max.setY(maxY);
        max.setZ(maxZ);
    }

    /**
     * 檢查位置是否在邊界框內
     */
    public boolean contains(Location location) {
        return location.getX() >= min.getX() && location.getX() <= max.getX() &&
               location.getY() >= min.getY() && location.getY() <= max.getY() &&
               location.getZ() >= min.getZ() && location.getZ() <= max.getZ();
    }

    /**
     * 檢查是否與另一個邊界框重疊
     */
    public boolean overlaps(BoundingBox other) {
        return !(other.max.getX() < this.min.getX() || other.min.getX() > this.max.getX() ||
                other.max.getY() < this.min.getY() || other.min.getY() > this.max.getY() ||
                other.max.getZ() < this.min.getZ() || other.min.getZ() > this.max.getZ());
    }

    /**
     * 獲取邊界框中心點
     */
    public Location getCenter() {
        return new Location(min.getWorld(),
            (min.getX() + max.getX()) / 2.0,
            (min.getY() + max.getY()) / 2.0,
            (min.getZ() + max.getZ()) / 2.0);
    }

    /**
     * 獲取邊界框體積
     */
    public double getVolume() {
        return getWidth() * getHeight() * getDepth();
    }

    /**
     * 獲取寬度 (X軸)
     */
    public double getWidth() {
        return max.getX() - min.getX();
    }

    /**
     * 獲取高度 (Y軸)
     */
    public double getHeight() {
        return max.getY() - min.getY();
    }

    /**
     * 獲取深度 (Z軸)
     */
    public double getDepth() {
        return max.getZ() - min.getZ();
    }

    /**
     * 擴展邊界框
     */
    public BoundingBox expand(double amount) {
        return new BoundingBox(
            min.clone().subtract(amount, amount, amount),
            max.clone().add(amount, amount, amount)
        );
    }

    /**
     * 收縮邊界框
     */
    public BoundingBox contract(double amount) {
        return expand(-amount);
    }

    // === Getters ===
    
    public Location getMin() { return min.clone(); }
    public Location getMax() { return max.clone(); }

    @Override
    public String toString() {
        return String.format("BoundingBox{min=(%.1f,%.1f,%.1f), max=(%.1f,%.1f,%.1f), volume=%.2f}", 
            min.getX(), min.getY(), min.getZ(), 
            max.getX(), max.getY(), max.getZ(), 
            getVolume());
    }
}