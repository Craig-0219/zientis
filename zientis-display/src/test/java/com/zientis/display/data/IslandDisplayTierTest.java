package com.zientis.display.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IslandDisplayTier 測試類
 */
class IslandDisplayTierTest {

    @Test
    void testFromIslandLevel() {
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(1));
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(5));
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(10));
        
        assertEquals(IslandDisplayTier.ENHANCED, IslandDisplayTier.fromIslandLevel(11));
        assertEquals(IslandDisplayTier.ENHANCED, IslandDisplayTier.fromIslandLevel(20));
        assertEquals(IslandDisplayTier.ENHANCED, IslandDisplayTier.fromIslandLevel(30));
        
        assertEquals(IslandDisplayTier.ADVANCED, IslandDisplayTier.fromIslandLevel(31));
        assertEquals(IslandDisplayTier.ADVANCED, IslandDisplayTier.fromIslandLevel(40));
        assertEquals(IslandDisplayTier.ADVANCED, IslandDisplayTier.fromIslandLevel(50));
        
        assertEquals(IslandDisplayTier.PREMIUM, IslandDisplayTier.fromIslandLevel(51));
        assertEquals(IslandDisplayTier.PREMIUM, IslandDisplayTier.fromIslandLevel(100));
        assertEquals(IslandDisplayTier.PREMIUM, IslandDisplayTier.fromIslandLevel(999));
    }

    @Test
    void testFromIslandLevelBoundaries() {
        // 測試邊界值
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(10));
        assertEquals(IslandDisplayTier.ENHANCED, IslandDisplayTier.fromIslandLevel(11));
        
        assertEquals(IslandDisplayTier.ENHANCED, IslandDisplayTier.fromIslandLevel(30));
        assertEquals(IslandDisplayTier.ADVANCED, IslandDisplayTier.fromIslandLevel(31));
        
        assertEquals(IslandDisplayTier.ADVANCED, IslandDisplayTier.fromIslandLevel(50));
        assertEquals(IslandDisplayTier.PREMIUM, IslandDisplayTier.fromIslandLevel(51));
    }

    @Test
    void testFromIslandLevelInvalid() {
        // 測試無效值，應該返回BASIC
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(0));
        assertEquals(IslandDisplayTier.BASIC, IslandDisplayTier.fromIslandLevel(-1));
    }

    @Test
    void testContainsLevel() {
        assertTrue(IslandDisplayTier.BASIC.containsLevel(1));
        assertTrue(IslandDisplayTier.BASIC.containsLevel(10));
        assertFalse(IslandDisplayTier.BASIC.containsLevel(11));
        
        assertTrue(IslandDisplayTier.ENHANCED.containsLevel(11));
        assertTrue(IslandDisplayTier.ENHANCED.containsLevel(30));
        assertFalse(IslandDisplayTier.ENHANCED.containsLevel(31));
        
        assertTrue(IslandDisplayTier.ADVANCED.containsLevel(31));
        assertTrue(IslandDisplayTier.ADVANCED.containsLevel(50));
        assertFalse(IslandDisplayTier.ADVANCED.containsLevel(51));
        
        assertTrue(IslandDisplayTier.PREMIUM.containsLevel(51));
        assertTrue(IslandDisplayTier.PREMIUM.containsLevel(1000));
    }

    @Test
    void testGetMaxRenderBlocks() {
        assertEquals(100, IslandDisplayTier.BASIC.getMaxRenderBlocks());
        assertEquals(250, IslandDisplayTier.ENHANCED.getMaxRenderBlocks());
        assertEquals(500, IslandDisplayTier.ADVANCED.getMaxRenderBlocks());
        assertEquals(1000, IslandDisplayTier.PREMIUM.getMaxRenderBlocks());
    }

    @Test
    void testGetParticleUpdateInterval() {
        assertEquals(5000, IslandDisplayTier.BASIC.getParticleUpdateInterval());
        assertEquals(3000, IslandDisplayTier.ENHANCED.getParticleUpdateInterval());
        assertEquals(2000, IslandDisplayTier.ADVANCED.getParticleUpdateInterval());
        assertEquals(1000, IslandDisplayTier.PREMIUM.getParticleUpdateInterval());
    }

    @Test
    void testGetHologramUpdateInterval() {
        assertEquals(30000, IslandDisplayTier.BASIC.getHologramUpdateInterval());
        assertEquals(20000, IslandDisplayTier.ENHANCED.getHologramUpdateInterval());
        assertEquals(15000, IslandDisplayTier.ADVANCED.getHologramUpdateInterval());
        assertEquals(10000, IslandDisplayTier.PREMIUM.getHologramUpdateInterval());
    }

    @Test
    void testSupportsDynamicEffects() {
        assertFalse(IslandDisplayTier.BASIC.supportsDynamicEffects());
        assertFalse(IslandDisplayTier.ENHANCED.supportsDynamicEffects());
        assertTrue(IslandDisplayTier.ADVANCED.supportsDynamicEffects());
        assertTrue(IslandDisplayTier.PREMIUM.supportsDynamicEffects());
    }

    @Test
    void testSupportsInteraction() {
        assertFalse(IslandDisplayTier.BASIC.supportsInteraction());
        assertTrue(IslandDisplayTier.ENHANCED.supportsInteraction());
        assertTrue(IslandDisplayTier.ADVANCED.supportsInteraction());
        assertTrue(IslandDisplayTier.PREMIUM.supportsInteraction());
    }

    @Test
    void testSupportsParticleEffects() {
        assertFalse(IslandDisplayTier.BASIC.supportsParticleEffects());
        assertTrue(IslandDisplayTier.ENHANCED.supportsParticleEffects());
        assertTrue(IslandDisplayTier.ADVANCED.supportsParticleEffects());
        assertTrue(IslandDisplayTier.PREMIUM.supportsParticleEffects());
    }

    @Test
    void testGetMaxHologramLines() {
        assertEquals(3, IslandDisplayTier.BASIC.getMaxHologramLines());
        assertEquals(5, IslandDisplayTier.ENHANCED.getMaxHologramLines());
        assertEquals(7, IslandDisplayTier.ADVANCED.getMaxHologramLines());
        assertEquals(10, IslandDisplayTier.PREMIUM.getMaxHologramLines());
    }

    @Test
    void testGetMinLevel() {
        assertEquals(1, IslandDisplayTier.BASIC.getMinLevel());
        assertEquals(11, IslandDisplayTier.ENHANCED.getMinLevel());
        assertEquals(31, IslandDisplayTier.ADVANCED.getMinLevel());
        assertEquals(51, IslandDisplayTier.PREMIUM.getMinLevel());
    }

    @Test
    void testGetMaxLevel() {
        assertEquals(10, IslandDisplayTier.BASIC.getMaxLevel());
        assertEquals(30, IslandDisplayTier.ENHANCED.getMaxLevel());
        assertEquals(50, IslandDisplayTier.ADVANCED.getMaxLevel());
        assertEquals(Integer.MAX_VALUE, IslandDisplayTier.PREMIUM.getMaxLevel());
    }

    @Test
    void testGetDisplayName() {
        assertEquals("基礎", IslandDisplayTier.BASIC.getDisplayName());
        assertEquals("增強", IslandDisplayTier.ENHANCED.getDisplayName());
        assertEquals("進階", IslandDisplayTier.ADVANCED.getDisplayName());
        assertEquals("頂級", IslandDisplayTier.PREMIUM.getDisplayName());
    }

    @Test
    void testGetDescription() {
        assertTrue(IslandDisplayTier.BASIC.getDescription().contains("簡單方塊材質"));
        assertTrue(IslandDisplayTier.ENHANCED.getDescription().contains("增強材質效果"));
        assertTrue(IslandDisplayTier.ADVANCED.getDescription().contains("複雜結構展示"));
        assertTrue(IslandDisplayTier.PREMIUM.getDescription().contains("頂級視覺效果"));
    }

    @Test
    void testToString() {
        String basicString = IslandDisplayTier.BASIC.toString();
        assertTrue(basicString.contains("基礎"));
        assertTrue(basicString.contains("等級 1-10"));
        
        String premiumString = IslandDisplayTier.PREMIUM.toString();
        assertTrue(premiumString.contains("頂級"));
        assertTrue(premiumString.contains("等級 51-999"));
    }

    @Test
    void testTierProgression() {
        // 驗證等級遞增時各項數值的邏輯性
        IslandDisplayTier[] tiers = {
            IslandDisplayTier.BASIC, 
            IslandDisplayTier.ENHANCED, 
            IslandDisplayTier.ADVANCED, 
            IslandDisplayTier.PREMIUM
        };
        
        for (int i = 1; i < tiers.length; i++) {
            IslandDisplayTier current = tiers[i];
            IslandDisplayTier previous = tiers[i - 1];
            
            // 更高等級應該有更多方塊渲染能力
            assertTrue(current.getMaxRenderBlocks() > previous.getMaxRenderBlocks());
            
            // 更高等級應該有更快的粒子更新
            assertTrue(current.getParticleUpdateInterval() < previous.getParticleUpdateInterval());
            
            // 更高等級應該有更快的全息圖更新
            assertTrue(current.getHologramUpdateInterval() < previous.getHologramUpdateInterval());
            
            // 更高等級應該有更多全息圖行數
            assertTrue(current.getMaxHologramLines() > previous.getMaxHologramLines());
        }
    }
}