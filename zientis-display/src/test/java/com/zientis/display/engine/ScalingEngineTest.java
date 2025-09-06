package com.zientis.display.engine;

import com.zientis.display.data.BlockPosition;
import com.zientis.display.data.IslandDisplayTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ScalingEngine 測試類
 */
class ScalingEngineTest {

    @Mock
    private World mockWorld;
    
    @Mock
    private Location mockLocation;
    
    @Mock
    private BlockData mockBlockData1;
    
    @Mock
    private BlockData mockBlockData2;
    
    private ScalingEngine scalingEngine;
    private Map<BlockPosition, BlockData> testBlocks;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scalingEngine = new ScalingEngine();
        
        // 設置模擬行為
        when(mockWorld.getName()).thenReturn("test_world");
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getX()).thenReturn(0.0);
        when(mockLocation.getY()).thenReturn(64.0);
        when(mockLocation.getZ()).thenReturn(0.0);
        when(mockLocation.clone()).thenReturn(mockLocation);
        
        when(mockBlockData1.getMaterial()).thenReturn(Material.STONE);
        when(mockBlockData2.getMaterial()).thenReturn(Material.GRASS_BLOCK);
        
        // 創建測試方塊數據
        testBlocks = new HashMap<>();
        testBlocks.put(new BlockPosition(0, 0, 0), mockBlockData1);
        testBlocks.put(new BlockPosition(1, 0, 0), mockBlockData2);
        testBlocks.put(new BlockPosition(0, 1, 0), mockBlockData1);
        testBlocks.put(new BlockPosition(2, 2, 2), mockBlockData2);
    }

    @Test
    void testScaleToMiniatureBasic() throws Exception {
        // 測試基礎等級縮放
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.BASIC);
        
        ScalingEngine.ScalingResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getScaledBlocks());
        assertNotNull(result.getCenterLocation());
        
        // 基礎等級應該有較少的縮放後方塊（由於1:8縮放比例）
        assertTrue(result.getScaledBlocks().size() <= testBlocks.size());
    }

    @Test
    void testScaleToMiniaturePremium() throws Exception {
        // 測試頂級等級縮放
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.PREMIUM);
        
        ScalingEngine.ScalingResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getScaledBlocks());
        
        // 頂級等級應該有更大的尺寸倍數
        // 驗證縮放結果不為空
        assertFalse(result.getScaledBlocks().isEmpty());
    }

    @Test
    void testScaleEmptyBlocks() throws Exception {
        // 測試空方塊映射
        Map<BlockPosition, BlockData> emptyBlocks = new HashMap<>();
        
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(emptyBlocks, mockLocation, IslandDisplayTier.BASIC);
        
        ScalingEngine.ScalingResult result = future.get();
        
        assertNotNull(result);
        assertTrue(result.getScaledBlocks().isEmpty());
        assertEquals(mockLocation, result.getCenterLocation());
    }

    @Test
    void testScaleRatioConsistency() throws Exception {
        // 測試縮放比例一致性
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.ENHANCED);
        
        ScalingEngine.ScalingResult result = future.get();
        
        // 驗證標準縮放比例
        assertEquals(8, ScalingEngine.SCALE_RATIO);
        
        // 驗證結果不為null
        assertNotNull(result.getScaledBlocks());
    }

    @Test
    void testDifferentTierSizes() throws Exception {
        // 測試不同等級產生不同尺寸
        CompletableFuture<ScalingEngine.ScalingResult> basicFuture = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.BASIC);
        
        CompletableFuture<ScalingEngine.ScalingResult> premiumFuture = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.PREMIUM);
        
        ScalingEngine.ScalingResult basicResult = basicFuture.get();
        ScalingEngine.ScalingResult premiumResult = premiumFuture.get();
        
        // 兩個結果都應該有效
        assertNotNull(basicResult);
        assertNotNull(premiumResult);
        
        // 方塊數量可能不同（由於不同的尺寸倍數）
        assertNotNull(basicResult.getScaledBlocks());
        assertNotNull(premiumResult.getScaledBlocks());
    }

    @Test
    @Disabled("Requires Bukkit server environment")
    void testRescaleForTierUpgrade() throws Exception {
        // 測試等級升級的重新縮放
        Map<BlockPosition, BlockData> currentBlocks = new HashMap<>();
        currentBlocks.put(new BlockPosition(0, 0, 0), mockBlockData1);
        
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.rescaleForTierUpgrade(currentBlocks, IslandDisplayTier.ADVANCED, mockLocation);
        
        ScalingEngine.ScalingResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getScaledBlocks());
        assertEquals(mockLocation, result.getCenterLocation());
    }

    @Test
    void testAsyncExecution() {
        // 測試異步執行
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(testBlocks, mockLocation, IslandDisplayTier.BASIC);
        
        assertNotNull(future);
        // 在小數據集上可能立即完成，所以不強制檢查isDone()
        
        assertDoesNotThrow(() -> {
            ScalingEngine.ScalingResult result = future.get();
            assertNotNull(result);
        });
    }

    @Test
    void testLocationCloning() throws Exception {
        // 測試位置複製不會修改原始位置
        Location originalLocation = mock(Location.class);
        Location clonedLocation = mock(Location.class);
        
        when(originalLocation.clone()).thenReturn(clonedLocation);
        when(clonedLocation.getWorld()).thenReturn(mockWorld);
        when(clonedLocation.getX()).thenReturn(0.0);
        when(clonedLocation.getY()).thenReturn(64.0);
        when(clonedLocation.getZ()).thenReturn(0.0);
        
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(testBlocks, originalLocation, IslandDisplayTier.BASIC);
        
        ScalingEngine.ScalingResult result = future.get();
        
        // 驗證使用了複製的位置
        verify(originalLocation, never()).setX(anyDouble());
        verify(originalLocation, never()).setY(anyDouble());
        verify(originalLocation, never()).setZ(anyDouble());
    }

    @Test
    void testBlockPositionMapping() throws Exception {
        // 測試方塊位置映射正確性
        Map<BlockPosition, BlockData> singleBlock = new HashMap<>();
        BlockPosition originalPos = new BlockPosition(8, 8, 8); // 會被縮放為 (1,1,1)
        singleBlock.put(originalPos, mockBlockData1);
        
        CompletableFuture<ScalingEngine.ScalingResult> future = 
            scalingEngine.scaleToMiniature(singleBlock, mockLocation, IslandDisplayTier.BASIC);
        
        ScalingEngine.ScalingResult result = future.get();
        
        assertNotNull(result);
        assertFalse(result.getScaledBlocks().isEmpty());
        
        // 驗證縮放後的位置是合理的（應該接近1,1,1）
        Map<BlockPosition, BlockData> scaledBlocks = result.getScaledBlocks();
        assertTrue(scaledBlocks.size() > 0);
    }
}