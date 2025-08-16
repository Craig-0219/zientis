package com.zientis.display.data;

/**
 * 表示微縮模型中方塊的相對位置
 * 
 * 這個類用於表示相對於展示中心的方塊位置
 * 使用相對坐標系統以提高性能和靈活性
 */
public class BlockPosition {
    
    private final int x;
    private final int y; 
    private final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * 從字符串創建 BlockPosition (格式: "x,y,z")
     */
    public static BlockPosition fromString(String positionString) {
        String[] parts = positionString.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid position string format: " + positionString);
        }
        
        try {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int z = Integer.parseInt(parts[2].trim());
            return new BlockPosition(x, y, z);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in position string: " + positionString);
        }
    }

    /**
     * 添加偏移量創建新的位置
     */
    public BlockPosition add(int deltaX, int deltaY, int deltaZ) {
        return new BlockPosition(x + deltaX, y + deltaY, z + deltaZ);
    }

    /**
     * 減去偏移量創建新的位置
     */
    public BlockPosition subtract(int deltaX, int deltaY, int deltaZ) {
        return new BlockPosition(x - deltaX, y - deltaY, z - deltaZ);
    }

    /**
     * 計算到另一個位置的距離
     */
    public double distance(BlockPosition other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 計算到另一個位置的曼哈頓距離
     */
    public int manhattanDistance(BlockPosition other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    /**
     * 檢查是否在指定範圍內
     */
    public boolean isWithinRange(BlockPosition center, int range) {
        return Math.abs(this.x - center.x) <= range &&
               Math.abs(this.y - center.y) <= range &&
               Math.abs(this.z - center.z) <= range;
    }

    // === Getters ===
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    // === 標準方法 ===
    
    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlockPosition other = (BlockPosition) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return ((x * 31) + y) * 31 + z;
    }
}