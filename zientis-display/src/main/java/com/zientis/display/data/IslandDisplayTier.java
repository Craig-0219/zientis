package com.zientis.display.data;

/**
 * Represents the visual and functional tier of an island display.
 */
public enum IslandDisplayTier {
    BASIC(10000),
    ENHANCED(25000),
    ADVANCED(50000),
    PREMIUM(100000);

    private final int maxRenderBlocks;

    IslandDisplayTier(int maxRenderBlocks) {
        this.maxRenderBlocks = maxRenderBlocks;
    }

    public int getMaxRenderBlocks() {
        return maxRenderBlocks;
    }
}
